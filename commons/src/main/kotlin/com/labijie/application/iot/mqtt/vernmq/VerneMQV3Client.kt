package com.labijie.application.iot.mqtt.vernmq

import com.hivemq.client.mqtt.MqttClientBuilder
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient
import com.labijie.application.iot.configuration.MqttProperties
import com.labijie.infra.spring.configuration.NetworkConfig
import org.springframework.web.client.RestTemplate
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

class VerneMQV3Client(networkConfig: NetworkConfig, serverProperties: MqttProperties, restTemplate: RestTemplate)
    : AbstractVerneMQClient<Mqtt3BlockingClient>(networkConfig, serverProperties, restTemplate) {
    override fun onBuildMqttClient(builder: MqttClientBuilder): Mqtt3BlockingClient {
        return builder.buildMqtt3Client(this.serverProperties)
    }

    override fun disconnect(client: Mqtt3BlockingClient) {
        client.disconnect()
    }

    override fun connect(client: Mqtt3BlockingClient) {
        client.connect()
    }

    override fun publish(client: Mqtt3BlockingClient, topic: String, payload: ByteArray, qos: MqttQos): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()

        client.toAsync().publishWith()
            .topic(topic)
            .payload(payload)
            .qos(qos)
            .send().whenComplete { _, throwable ->
                if (throwable != null) {
                    future.completeExceptionally(throwable)
                } else {
                    future.complete(null)
                }
            }
        return future
    }
}