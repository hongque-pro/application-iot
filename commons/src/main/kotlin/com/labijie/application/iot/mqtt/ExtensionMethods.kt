package com.labijie.application.iot.mqtt

import com.hivemq.client.mqtt.MqttClient

internal class DelegateSubscriber(
    override val topicFilter: String,
    private val callback: (topic: String, payload: ByteArray) -> Unit
) : ISubscriber {
    override fun onSub(topic: String, payload: ByteArray) {
        this.callback.invoke(topic, payload)
    }

}

fun IMqttClient.subscribe(topicFilter: String,  onSub: (topic: String, payload: ByteArray) -> Unit) {
    this.subscribe(DelegateSubscriber(topicFilter, onSub))
}