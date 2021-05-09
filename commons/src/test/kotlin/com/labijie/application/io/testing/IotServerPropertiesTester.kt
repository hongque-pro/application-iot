package com.labijie.application.io.testing

import com.labijie.application.iot.MqttVersion
import com.labijie.application.iot.configuration.IotCommonsAutoConfiguration
import com.labijie.application.iot.configuration.IotProperties
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.Test


@SpringBootTest
@ContextConfiguration(classes = [IotCommonsAutoConfiguration::class])
@ActiveProfiles("test")
class IotServerPropertiesTester {

    @Autowired
    private lateinit var iotProperties: IotProperties

    @Test
    fun testMqttVersionConverter() {
        Assertions.assertEquals(MqttVersion.V3_1_1, this.iotProperties.mqtt.version)
    }
}