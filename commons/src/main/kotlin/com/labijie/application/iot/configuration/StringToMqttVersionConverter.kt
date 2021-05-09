package com.labijie.application.iot.configuration

import com.labijie.application.getEnumFromString
import com.labijie.application.iot.MqttVersion
import org.springframework.core.convert.converter.Converter

class StringToMqttVersionConverter : Converter<String, MqttVersion> {
    override fun convert(source: String): MqttVersion? {
        val e = getEnumFromString(MqttVersion::class.java, source, true) as? MqttVersion
        return e
                ?: when (source) {
                    "5.0" -> MqttVersion.V5_0
                    "3.1.1" -> MqttVersion.V3_1_1
                    else -> throw IllegalArgumentException("Unable to convert '$source' to MqttVersion enum, support value is 5.0, 3.1.1")
                }
    }
}