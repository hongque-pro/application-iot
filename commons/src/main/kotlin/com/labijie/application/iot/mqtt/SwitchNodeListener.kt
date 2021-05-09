package com.labijie.application.iot.mqtt

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.lifecycle.MqttClientAutoReconnect
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

internal class SwitchNodeListener<T : MqttClient>(
    private val client: AbstractMqttClientBase<T>
) : MqttClientDisconnectedListener {

    val DEFAULT_START_DELAY_NANOS = TimeUnit.SECONDS.toNanos(MqttClientAutoReconnect.DEFAULT_START_DELAY_S)
    val DEFAULT_MAX_DELAY_NANOS = TimeUnit.SECONDS.toNanos(MqttClientAutoReconnect.DEFAULT_MAX_DELAY_S)


    companion object {
        private val logger: Logger by lazy {
            LoggerFactory.getLogger(SwitchNodeListener::class.java)
        }
    }

    override fun onDisconnected(context: MqttClientDisconnectedContext) {
        if (client.isStopped() || context.source == MqttDisconnectSource.USER) {
            context.reconnector.reconnect(false)
            return
        }

        val delay = Math.min(
            DEFAULT_START_DELAY_NANOS * Math.pow(2.0, context.reconnector.attempts.toDouble()),
            DEFAULT_MAX_DELAY_NANOS.toDouble()
        ).toLong()

        val randomDelay =
            (delay / 4.0 / Int.MAX_VALUE * ThreadLocalRandom.current().nextInt()).toLong()

        if (context.source != MqttDisconnectSource.USER && !client.isStopped()) {
            if (context.reconnector.attempts >= 5) {
                logger.warn("Attempt to reconnect '${context.reconnector.transportConfig.serverAddress}' 5 times was unsuccessful and try to switch another broker.")
                if(switchNode()){
                    context.reconnector.reconnect(false)
                    return
                }
            }
            logger.warn("Mqtt client disconnected by ${context.source.toString().toLowerCase()} and will reconnect.")
        }

        context.reconnector.reconnect(true).delay(delay + randomDelay, TimeUnit.NANOSECONDS);
    }

    private fun switchNode(): Boolean {
        if (client.isStopped()) {
            return true
        }

        return try {
            val nodes = client.getClusterNodes()
            if (nodes.isNotEmpty()) {
                val n = nodes.random()
                return client.useNode(n) != null
            } else {
                false
            }
        } catch (e: Exception) {
            logger.error("Switch mqtt node fault.")
            false
        }
    }
}