package com.labijie.application.iot

object IotUtils {
    const val LISTENER_PROTOCOL_MQTT = "mqtt"
    const val LISTENER_PROTOCOL_MQTT_WS = "mqttws"


    const val MQTT_SERVER_PROVIDER_VERNEMQ = "vernemq"

    fun isSupportProtocol(protocol: String): Boolean {
        return when (protocol) {
            LISTENER_PROTOCOL_MQTT, LISTENER_PROTOCOL_MQTT_WS -> true
            else -> false
        }
    }

    fun getUriList(uri: String, mapper: ((item: String) -> String)? = null): List<String> {
        if (uri.isBlank()) {
            return listOf()
        }
        return uri.split(",").filter {
            it.isNotBlank()
        }.map {
            mapper?.invoke(it.trim()) ?: it.trim()
        }
    }
}