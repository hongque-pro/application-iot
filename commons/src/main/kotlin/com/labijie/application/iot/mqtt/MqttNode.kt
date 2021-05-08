package com.labijie.application.iot.mqtt

import com.labijie.application.iot.IotUtils

class MqttNode(
    var host:String = "",
    var clientsCount: Long = 0,
    var subscriptionCount: Long = 0,
    var properties: Map<String, Any?> = mapOf(),
    var listeners: List<Listener> = listOf()
) {

    data class Listener(
        /**
         * Transport protocol for mqtt broker, support mqtt, mqttws
         * @see IotUtils.LISTENER_PROTOCOL_MQTT
         *
         * @see IotUtils.LISTENER_PROTOCOL_MQTT_WEB_SOCKET
         */
        var protocol: String = IotUtils.LISTENER_PROTOCOL_MQTT,
        var port: Int = 0
    )
}