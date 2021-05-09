package com.labijie.application.iot.configuration

import java.time.Duration


data class MqttV5Features(
    var willMessage: Boolean = true,
    var cleanStart: Boolean = true,
    var sessionExpiryInterval: Duration = Duration.ZERO
)