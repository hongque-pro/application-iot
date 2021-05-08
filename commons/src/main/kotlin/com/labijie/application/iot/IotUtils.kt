package com.labijie.application.iot

import java.net.URI
import kotlin.random.Random

object IotUtils {
    const val LISTENER_PROTOCOL_MQTT = "mqtt"
    const val LISTENER_PROTOCOL_MQTT_WEB_SOCKET = "mqttws"

    fun isSupportProtocol(protocol: String): Boolean {
        return when (protocol) {
            LISTENER_PROTOCOL_MQTT, LISTENER_PROTOCOL_MQTT -> true
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