package com.labijie.application.iot.configuration

import com.labijie.application.configuration.ApplicationCoreAutoConfiguration
import com.labijie.application.iot.IotUtils
import com.labijie.application.iot.mqtt.IMqttClient
import com.labijie.application.iot.mqtt.vernmq.VerneMQV3Client
import com.labijie.application.iot.mqtt.vernmq.VerneMQV5Client
import com.labijie.infra.spring.configuration.NetworkConfig
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(ApplicationCoreAutoConfiguration::class, IotCommonsAutoConfiguration::class)
@ConditionalOnProperty(prefix = IotProperties.MQTT_PROPERTIES_PREFIX, name = ["enabled"], havingValue = "true", matchIfMissing = false)
class MQTTAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = IotProperties.MQTT_PROPERTIES_PREFIX, name = ["server-provider"], havingValue = IotUtils.MQTT_SERVER_PROVIDER_VERNEMQ, matchIfMissing = false)
    protected class VerneMQAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean(IMqttClient::class)
        @ConditionalOnProperty(prefix = IotProperties.MQTT_PROPERTIES_PREFIX, name = ["version"], havingValue = "5.0", matchIfMissing = true)
        fun verneMQV5Client(
                networkConfig: NetworkConfig,
                iotServerProperties: IotProperties,
                restTemplate: RestTemplate): VerneMQV5Client {
            return VerneMQV5Client(networkConfig, iotServerProperties.mqtt, restTemplate)
        }


        @Bean
        @ConditionalOnMissingBean(IMqttClient::class)
        @ConditionalOnProperty(prefix = IotProperties.MQTT_PROPERTIES_PREFIX, name = ["version"], havingValue = "3.1.1", matchIfMissing = false)
        fun verneMQV3Client(
                networkConfig: NetworkConfig,
                iotServerProperties: IotProperties,
                restTemplate: RestTemplate): VerneMQV3Client {
            return VerneMQV3Client(networkConfig, iotServerProperties.mqtt, restTemplate)
        }
    }
}