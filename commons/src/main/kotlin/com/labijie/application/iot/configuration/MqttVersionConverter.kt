package com.labijie.application.iot.configuration

import com.labijie.application.getEnumFromString
import com.labijie.application.iot.MqttVersion
import org.springframework.core.convert.converter.Converter

class MqttVersionConverter : Converter<Any, MqttVersion> {
    override fun convert(source: Any): MqttVersion? {
        var v: MqttVersion? = null

        if (source is String) {
            val e = getEnumFromString(MqttVersion::class.java, source, true) as? MqttVersion
            v = e
                ?: when (source) {
                    "5.0" -> MqttVersion.V5_0
                    "3.1.1" -> MqttVersion.V3_1_1
                    else -> null
                }
        }

        if (source is Double) {
            v = when (source) {
                5.0 -> MqttVersion.V5_0
                else -> null
            }
        }

        return v
            ?: throw IllegalArgumentException("Unable to convert '$source' to MqttVersion enum, support value is 5.0, 3.1.1")
    }
}