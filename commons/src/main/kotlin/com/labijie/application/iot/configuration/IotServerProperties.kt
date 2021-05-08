package com.labijie.application.iot.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("application.iot.server")
class IotServerProperties {
    val mqtt = MqttClusterProperties()
}