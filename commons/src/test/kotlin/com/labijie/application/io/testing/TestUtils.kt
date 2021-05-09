package com.labijie.application.io.testing

import com.labijie.application.iot.MqttVersion
import com.labijie.application.iot.configuration.IotProperties

object TestUtils {
    var defaultMqttUser = ""
    var defaultMqttPassword = ""
    var defaultVerneMQHttpUrl = "http://47.100.188.60:8888"
    var defaultVerneMQApiKey = "2a6gDMIx3CZamjtJoP4P1WNGSvyGBQfO"


    fun createTestIotProperties(
        mqttUser: String = defaultMqttUser,
        mqttPassword: String = defaultMqttPassword,
        version: MqttVersion = MqttVersion.V5_0
    ): IotProperties {
        return IotProperties().apply {
            this.mqtt.version = version
            this.mqtt.user = mqttUser
            this.mqtt.password = mqttPassword
            this.mqtt.vernemq.httpUrl = defaultVerneMQHttpUrl
            this.mqtt.vernemq.apiKey = defaultVerneMQApiKey
        }
    }

}