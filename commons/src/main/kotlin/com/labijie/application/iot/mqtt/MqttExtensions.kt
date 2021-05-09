package com.labijie.application.iot.mqtt.vernmq

import com.hivemq.client.mqtt.MqttClientBuilder
import com.hivemq.client.mqtt.MqttClientBuilderBase
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import com.labijie.application.iot.configuration.MqttProperties

fun MqttClientBuilder.buildMqtt5Client(
        properties: MqttProperties? = null,
        configure: ((client: MqttClientBuilderBase<*>) -> Unit)? = null
): Mqtt5BlockingClient {
    val username = properties?.user
    val password = properties?.password

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


fun MqttClientBuilder.buildMqtt3Client(
        properties: MqttProperties? = null,
        configure: ((client: MqttClientBuilderBase<*>) -> Unit)? = null
): Mqtt3BlockingClient {
    val username = properties?.user
    val password = properties?.password

    return this.useMqttVersion3().apply {
        if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
            this.simpleAuth()
                    .username(username)
                    .password(password.toByteArray(Charsets.UTF_8))
                    .applySimpleAuth()

        }
        configure?.invoke(this)
    }.buildBlocking()
}