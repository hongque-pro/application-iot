package com.labijie.application.iot.exception

class MqttClientConnectTimeoutException(message: String = "Mqtt client connect timeout", cause: Throwable? = null) : MqttClientConnectionException(message, cause)