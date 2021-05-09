package com.labijie.application.iot.configuration

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(IotProperties::class)
class IotCommonsAutoConfiguration {

    @Bean
    @ConfigurationPropertiesBinding
    fun stringToMqttVersionConverter(): StringToMqttVersionConverter {
        return StringToMqttVersionConverter()
    }

}