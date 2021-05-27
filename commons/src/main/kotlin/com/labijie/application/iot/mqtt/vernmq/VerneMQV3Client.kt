package com.labijie.application.iot.mqtt.vernmq

import com.hivemq.client.mqtt.MqttClientBuilder
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import com.labijie.application.iot.IotMqttQos
import com.labijie.application.iot.configuration.MqttProperties
import com.labijie.application.iot.exception.MqttClientException
import com.labijie.application.iot.mqtt.ISubscriber
import com.labijie.infra.spring.configuration.NetworkConfig
import org.springframework.web.client.RestTemplate
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

class VerneMQV3Client(networkConfig: NetworkConfig, serverProperties: MqttProperties, restTemplate: RestTemplate)
    : AbstractVerneMQClient<Mqtt3BlockingClient>(networkConfig, serverProperties, restTemplate) {
    override fun onBuildMqttClient(builder: MqttClientBuilder): Mqtt3BlockingClient {
        return builder.buildMqtt3Client(this.serverProperties)
    }

    override fun disconnect(client: Mqtt3BlockingClient) {
        client.disconnect()
    }

    override fun connect(client: Mqtt3BlockingClient) {
        val c = Mqtt3Connect
            .builder()
            .cleanSession(this.serverProperties.v3.cleanSession)
            .keepAlive(serverProperties.keepAlive.seconds.coerceAtMost(65535).toInt())
            .build()
        client.connect(c)
    }

    override fun pub(client: Mqtt3BlockingClient, topic: String, payload: ByteArray, qos: IotMqttQos): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()

        client.toAsync().publishWith()
            .topic(topic)
            .payload(payload)
            .qos(qos.toMqttQos())
            .send().whenComplete { _, throwable ->
                if (throwable != null) {
                    future.completeExceptionally(throwable)
                } else {
                    future.complete(null)
                }
            }
        return future
    }

    override fun sub(client: Mqtt3BlockingClient, subscriber: ISubscriber): CompletableFuture<Void> {
        val f = CompletableFuture<Void>()

        client.toAsync()
            .subscribeWith()
            .topicFilter(subscriber.topicFilter.trim())
            .callback(SubConsumer(subscriber))
            .send().whenComplete { ack, err ->
                if (err != null) {
                    val sb = StringBuilder()
                        .appendLine("subscription topic '${subscriber.topicFilter.trim()}' fault.")
                    ack?.run {
                        sb.appendLine(this)
                    }
                    f.completeExceptionally(MqttClientException(sb.toString(), err))
                } else {
                    f.complete(null)
                }
            }

        return f
    }

    class SubConsumer(private val sub: ISubscriber) : Consumer<Mqtt3Publish> {
        override fun accept(t: Mqtt3Publish) {
            sub.onSub(t.topic.toString(), t.payloadAsBytes)
        }

    }
}