package com.labijie.application.iot.mqtt

import com.hivemq.client.mqtt.datatypes.MqttQos
import java.time.Duration
import java.util.concurrent.CompletableFuture

interface IMqttClient {
    val isConnected: Boolean
    fun connect(timeout: Duration? = null): CompletableFuture<Void>
    fun disconnect()
    fun getClusterNodes(): List<MqttNode>
    fun pushMessage(topic: String, payload: ByteArray, qos: MqttQos = MqttQos.AT_MOST_ONCE): CompletableFuture<Void>
}