package com.labijie.application.iot.mqtt

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttClientState
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import com.labijie.application.iot.IotUtils
import com.labijie.application.iot.LoopList
import com.labijie.application.iot.LoopPolicy
import com.labijie.application.iot.WebApiInvocationException
import com.labijie.application.iot.configuration.VerneMQProperties
import com.labijie.application.iot.mqtt.vernmq.*
import com.labijie.application.toMap
import com.labijie.infra.spring.configuration.NetworkConfig
import com.labijie.infra.utils.logger
import com.labijie.infra.utils.throwIfNecessary
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.net.URL
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

class VerneMQCluster(
    private val networkConfig: NetworkConfig,
    private val restTemplate: RestTemplate,
    private val serverProperties: VerneMQProperties
) : IMqttCluster, AutoCloseable {

    @Volatile
    private var stopped: Boolean = false

    private val connectSemaphore = Semaphore(1)

    private var client: Mqtt5BlockingClient? = null

    companion object {
        fun ResponseEntity<*>.validateVerneMQResponse(requestUrl: String?, throwIfInvalid: Boolean = false): Boolean {
            if (this.statusCode != HttpStatus.OK) {
                if (throwIfInvalid) {
                    throw WebApiInvocationException(
                        requestUrl,
                        this.statusCode,
                        "Invoke verneMQ status api fault."
                    )
                }
                return false
            }

            if (!this.hasBody()) {
                if (throwIfInvalid) {
                    throw WebApiInvocationException(
                        requestUrl,
                        this.statusCode,
                        "VerneMQ status api response empty body."
                    )
                }
                return false
            }
            return true
        }
    }

    private fun getNodeAddress(nodeName: String): String {
        return if (nodeName.startsWith("VerneMQ@")) {
            //Official docker node name
            nodeName.removePrefix("VerneMQ@")
        } else {
            nodeName
        }
    }

    private val statusUrl by lazy {
        val list = IotUtils.getUriList(serverProperties.httpUri) {
            URL(URL(it), "/status.json").toString()
        }
        LoopList(list, LoopPolicy.Sequence)
    }

    private val statusResponseType: ParameterizedTypeReference<Map<String, VerneMQNodeStatus>> by lazy {
        object :
            ParameterizedTypeReference<Map<String, VerneMQNodeStatus>>() {
        }
    }

    private fun connectAsync() : CompletableFuture<Boolean> {
        var f = CompletableFuture<Boolean>()
        val retryIntervalSeconds = 10L
        if (connectSemaphore.tryAcquire()) {
            thread {
                try {
                    loop@ while (client == null && !this.stopped) {
                        var currentEndpoint: String = ""
                        try {
                            val nodes = this.getNodes().sortedBy { it.clientsCount }
                            if (nodes.isEmpty()) {
                                logger.warn("No available mqtt broker (verneMQ), retry after $retryIntervalSeconds seconds")
                            } else {
                                val c = LoopList(nodes).loop {
                                    val listener =
                                        it.listeners.firstOrNull { l -> l.protocol == IotUtils.LISTENER_PROTOCOL_MQTT }
                                    if (listener === null) {
                                        null
                                    } else {
                                        currentEndpoint = " '${listener.protocol}://${it.host}:${listener.port}'"
                                        val client = buildMqttClient(it, listener)
                                        client.connect()
                                        client
                                    }
                                }
                                if (c != null) {
                                    this.client = c
                                    break@loop
                                }
                            }
                        } catch (e: Exception) {
                            e.throwIfNecessary()
                            logger.error("connect verneMQ broker${currentEndpoint} fault.")
                        }
                        Thread.sleep(Duration.ofSeconds(retryIntervalSeconds).toMillis())
                    }
                }finally {
                    f.complete(this.client?.state == MqttClientState.CONNECTED)
                    connectSemaphore.release()
                }
            }
        }else{
            f.complete(this.client?.state == MqttClientState.CONNECTED)
        }
        return f
    }

    private fun buildMqttClient(
        it: MqttNode,
        listener: MqttNode.Listener
    ): Mqtt5BlockingClient {
        return MqttClient.builder()
            .serverHost(it.host)
            .serverPort(listener.port)
            .automaticReconnectWithDefaultConfig()
            .identifier("iot_svr_${networkConfig.getIPAddress()}")
            .buildWithProperties(serverProperties)
    }

    override fun getNodes(): Collection<MqttNode> {
        return this.statusUrl.loop { url ->
            val resp = restTemplate.exchange(url, HttpMethod.GET, null, statusResponseType)
            val succeed = resp.validateVerneMQResponse(url)
            if (!succeed) {
                null
            }

            resp.body!!.map {
                MqttNode(
                    host = getNodeAddress(it.key),
                    clientsCount = it.value.onlineCount,
                    subscriptionCount = it.value.subscriptionCount,
                    properties = it.value.toMap(),
                    listeners = it.value.listeners.filter { l ->
                        IotUtils.isSupportProtocol(l.type)
                    }.map { l ->
                        MqttNode.Listener(
                            l.type,
                            l.port
                        )
                    }
                )
            }
        } ?: listOf()
    }

    private fun checkState(){
        if(this.stopped){
            throw VerneMQException("cluster client instance was shutdown")
        }
        if(this.client?.state != MqttClientState.CONNECTED){
            throw VerneMQException("Mqtt client state is: ${this.client?.state?.toString() ?: "<null>"}")
        }

        this.client?.publish()
    }

    override fun pushMessage() {
    }

    override fun close() {
        this.client?.disconnect()
    }


}