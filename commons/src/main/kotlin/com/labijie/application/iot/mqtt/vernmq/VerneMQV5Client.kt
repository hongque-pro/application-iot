package com.labijie.application.iot.mqtt.vernmq

import com.hivemq.client.mqtt.MqttClientBuilder
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import com.labijie.application.iot.configuration.MqttProperties
import com.labijie.infra.spring.configuration.NetworkConfig
import org.springframework.web.client.RestTemplate
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

class VerneMQV5Client(networkConfig: NetworkConfig, serverProperties: MqttProperties, restTemplate: RestTemplate) :
    AbstractVerneMQClient<Mqtt5BlockingClient>(networkConfig, serverProperties, restTemplate) {
    override fun onBuildMqttClient(builder: MqttClientBuilder): Mqtt5BlockingClient {
        return builder.buildMqtt5Client(this.serverProperties)
    }

    override fun disconnect(client: Mqtt5BlockingClient) {
        client.disconnect()
    }

    override fun connect(client: Mqtt5BlockingClient) {
        client.connect()
    }

    override fun publish(
        client: Mqtt5BlockingClient,
        topic: String,
        payload: ByteArray,
        qos: MqttQos
    ): CompletableFuture<Void> {
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

//    fun subscripe(client: Mqtt5BlockingClient){
//        client.subscribeWith().addSubscription().top
//    }

}