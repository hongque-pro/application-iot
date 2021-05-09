package com.labijie.application.iot.exception

open class MqttClientConnectionException(message: String, cause: Throwable? = null) : MqttClientException(message, cause)