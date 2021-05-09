package com.labijie.application.iot.mqtt

import com.fasterxml.jackson.annotation.JsonIgnore
import com.labijie.application.iot.IotUtils

class MqttNode(
    var host: String = "",
    var clientsCount: Long = 0,
    var subscriptionCount: Long = 0,
    var serverProperties: Map<String, Any?> = mapOf(),
    var listeners: List<MqttNodeListener> = listOf(),
    var online: Boolean = true
) {
    @get:JsonIgnore
    val hasMqttEndpoint: Boolean
        get() = this.listeners.any { it.protocol == IotUtils.LISTENER_PROTOCOL_MQTT }

    @get:JsonIgnore
    val hasMqttWebSocketEndpoint: Boolean
        get() = this.listeners.any { it.protocol == IotUtils.LISTENER_PROTOCOL_MQTT_WS }

    override fun toString(): String {
         return this.listeners.map {
             "${it.protocol}://$host:${it.port}"
         }.joinToString(", ")
    }
}

data class MqttNodeListener(
    /**
     * Transport protocol for mqtt broker, support mqtt, mqttws
     * @see IotUtils.LISTENER_PROTOCOL_MQTT
     *
     * @see IotUtils.LISTENER_PROTOCOL_MQTT_WS
     */
    var protocol: String = IotUtils.LISTENER_PROTOCOL_MQTT,
    var port: Int = 0
)