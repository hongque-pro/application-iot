package com.labijie.application.iot.mqtt

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttClientBuilder
import com.hivemq.client.mqtt.MqttClientState
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.labijie.application.iot.IotUtils
import com.labijie.application.iot.LoopList
import com.labijie.application.iot.configuration.MqttProperties
import com.labijie.application.iot.exception.MqttClientConnectTimeoutException
import com.labijie.application.iot.exception.MqttClientException
import com.labijie.infra.spring.configuration.NetworkConfig
import com.labijie.infra.utils.ifNullOrBlank
import com.labijie.infra.utils.throwIfNecessary
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

abstract class AbstractMqttClientBase<T : MqttClient>(
    protected val networkConfig: NetworkConfig,
    protected val serverProperties: MqttProperties,
) : IMqttClient, AutoCloseable {
    private val verneMQProperties = serverProperties.vernemq

    @Volatile
    private var stopped: Boolean = false

    private val connectSemaphore = Semaphore(1)

    private var client: T? = null

    protected val logger by lazy {
        LoggerFactory.getLogger(this::class.java)
    }

    private fun getNodeAddress(nodeName: String): String {
        return if (nodeName.startsWith("VerneMQ@")) {
            //Official docker node name
            nodeName.removePrefix("VerneMQ@")
        } else {
            nodeName
        }
    }

    override fun disconnect() {
        client?.run {
            disconnect(this)
        }
        client = null
    }

    override val isConnected: Boolean
        get() = this.client?.state == MqttClientState.CONNECTED

    fun isStopped(): Boolean = stopped


    override fun connect(timeout: Duration?): CompletableFuture<Void> {
        var f = CompletableFuture<Void>()
        val retryIntervalSeconds = 5L

        val startTime = System.currentTimeMillis()
        val timeoutMills = timeout?.toMillis() ?: Long.MAX_VALUE

        if (connectSemaphore.tryAcquire()) {
            thread(isDaemon = true) {
                try {
                    loop@ while (client == null && !this.stopped) {
                        var currentNode: MqttNode? = null
                        try {
                            val nodes = this.getClusterNodes().sortedBy { it.clientsCount }
                            if (nodes.isEmpty()) {
                                logger.warn("No available mqtt broker (verneMQ), retry after $retryIntervalSeconds seconds")
                            } else {
                                val c = LoopList(nodes).loop {
                                    currentNode = it
                                    useNode(it)
                                }
                                if (c != null) {
                                    break@loop
                                }
                            }
                        } catch (e: Throwable) {
                            e.throwIfNecessary()
                            val endpoint: String = if (currentNode != null) " ($currentNode)" else ""
                            logger.error("connect verneMQ broker$endpoint fault.", e)

                        }
                        if((System.currentTimeMillis() - startTime) > timeoutMills){
                            throw MqttClientConnectTimeoutException()
                        }

                        Thread.sleep(Duration.ofSeconds(retryIntervalSeconds).toMillis())
                    }
                    f.complete(null)
                }
                catch (throwable: Throwable){
                    f.completeExceptionally(throwable)
                }
                finally {
                    connectSemaphore.release()
                }
            }
        } else {
            f.completeExceptionally(MqttClientException("Concurrent mqtt client connect method call, another thread is connecting."))
        }
        return f
    }

    fun useNode(it: MqttNode): T? {
        val listener =
            it.listeners.firstOrNull { l -> l.protocol == IotUtils.LISTENER_PROTOCOL_MQTT }

        return if (listener === null) {
            null
        } else {
            val c = buildMqttClient(it, listener)
            connect(c)
            logger.info("Connect to mqtt broker '${it.host.ifNullOrBlank { "<unknown>" }}' succeed.")
            this.client = c
            c
        }
    }


    private fun buildMqttClient(
        it: MqttNode,
        listener: MqttNodeListener
    ): T {
        val builder = MqttClient.builder()
            .serverHost(it.host)
            .serverPort(listener.port)
//            .automaticReconnectWithDefaultConfig()
            .addDisconnectedListener(SwitchNodeListener(this))
            .identifier("iot_svr_${networkConfig.getIPAddress()}")
        return onBuildMqttClient(builder)

    }

    protected abstract fun onBuildMqttClient(builder: MqttClientBuilder): T

    protected abstract fun disconnect(client: T)

    protected abstract fun connect(client: T)

    protected fun mustConnected(): T {
        val c = this.client
        if (this.stopped) {
            throw MqttClientException("Mqtt client instance was shutdown")
        }
        if (c?.state != MqttClientState.CONNECTED) {
            throw MqttClientException("Mqtt client state is: ${this.client?.state?.toString() ?: "<null>"}")
        }
        return c
    }

    final override fun pushMessage(topic: String, payload: ByteArray, qos: MqttQos): CompletableFuture<Void> {
        val c = this.mustConnected()
        return this.publish(c, topic, payload, qos)
    }

    protected abstract fun publish(client: T, topic: String, payload: ByteArray, qos: MqttQos) : CompletableFuture<Void>

    override fun close() {
        this.disconnect()
    }
}