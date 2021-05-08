package com.labijie.application.iot.mqtt.vernmq

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttClientBuilder
import com.hivemq.client.mqtt.MqttClientBuilderBase
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import com.labijie.application.iot.configuration.VerneMQProperties
import org.jetbrains.annotations.NotNull

fun MqttClientBuilder.buildWithProperties(
    properties: VerneMQProperties? = null,
    configure: ((client: MqttClientBuilderBase<*>) -> Unit)? = null
): Mqtt5BlockingClient {
    val username = properties?.mqttUser
    val password = properties?.mqttPassword

    return this.useMqttVersion5().apply {
        if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
            this.simpleAuth()
                .username(username)
                .password(password.toByteArray(Charsets.UTF_8))
                .applySimpleAuth()

        }
        configure?.invoke(this)
    }.buildBlocking()
}