package com.labijie.application.iot.configuration

import com.labijie.application.iot.IotUtils
import com.labijie.application.iot.MqttVersion
import org.springframework.boot.context.properties.NestedConfigurationProperty
import java.time.Duration


data class MqttProperties(
        var enabled: Boolean = true,
        /**
         * mqtt server provider
         */
        var serverProvider: String = IotUtils.MQTT_SERVER_PROVIDER_VERNEMQ,
        var user: String = "",
        var password: String = "",
        var version: MqttVersion = MqttVersion.V5_0,
        var keepAlive: Duration = Duration.ofMinutes(1),

        @NestedConfigurationProperty
        val v5: MqttV5Features = MqttV5Features(),
        @NestedConfigurationProperty
        val v3: MqttV3Features = MqttV3Features(),

        @NestedConfigurationProperty
        val vernemq: VerneMQProperties = VerneMQProperties()
)