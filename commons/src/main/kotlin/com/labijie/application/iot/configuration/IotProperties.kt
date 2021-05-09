package com.labijie.application.iot.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.context.annotation.Configuration

@ConfigurationProperties("application.iot")
class IotProperties {
    companion object{
        const val MQTT_PROPERTIES_PREFIX = "application.iot.mqtt"
        const val VERNEMQ_PROPERTIES_PREFIX = "application.iot.mqtt.vernemq"
    }

    @NestedConfigurationProperty
    val mqtt = MqttProperties()
}