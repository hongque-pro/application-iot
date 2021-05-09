package com.labijie.application.iot.configuration

import com.labijie.application.iot.IotUtils
import com.labijie.application.iot.MqttVersion
import org.springframework.boot.context.properties.NestedConfigurationProperty


data class MqttProperties(
        var enabled: Boolean = true,
        /**
         * mqtt server provider
         */
        var serverProvider: String = IotUtils.MQTT_SERVER_PROVIDER_VERNEMQ,
        var user: String = "",
        var password: String = "",
        var version: MqttVersion = MqttVersion.V5_0,
        @NestedConfigurationProperty
        var vernemq: VerneMQProperties = VerneMQProperties()
)