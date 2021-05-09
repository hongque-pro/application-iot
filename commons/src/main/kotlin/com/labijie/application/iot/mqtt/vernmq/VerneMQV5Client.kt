package com.labijie.application.iot.mqtt.vernmq

import com.hivemq.client.mqtt.MqttClientBuilder
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5ReasonCode
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect.NO_SESSION_EXPIRY
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.labijie.application.iot.configuration.MqttProperties
import com.labijie.application.iot.exception.MqttClientException
import com.labijie.application.iot.mqtt.ISubscriber
import com.labijie.infra.spring.configuration.NetworkConfig
import org.springframework.web.client.RestTemplate
import java.lang.StringBuilder
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

class VerneMQV5Client(networkConfig: NetworkConfig, serverProperties: MqttProperties, restTemplate: RestTemplate) :
    AbstractVerneMQClient<Mqtt5BlockingClient>(networkConfig, serverProperties, restTemplate) {
    override fun onBuildMqttClient(builder: MqttClientBuilder): Mqtt5BlockingClient {
        return builder.buildMqtt5Client(this.serverProperties)
    }

    override fun disconnect(client: Mqtt5BlockingClient) {
        if (this.serverProperties.v5.willMessage) {
            val dis = Mqtt5Disconnect.builder()
                .reasonCode(Mqtt5DisconnectReasonCode.DISCONNECT_WITH_WILL_MESSAGE)
                .build()
            client.disconnect(dis)
        } else {
            client.disconnect()
        }
    }

    override fun connect(client: Mqtt5BlockingClient) {
        val c = Mqtt5Connect
            .builder()
            .cleanStart(this.serverProperties.v5.cleanStart)
            .sessionExpiryInterval(this.serverProperties.v5.sessionExpiryInterval.seconds.coerceAtMost(NO_SESSION_EXPIRY))
            .keepAlive(serverProperties.keepAlive.seconds.coerceAtMost(65535).toInt())
            .build()
        client.connect(c)
    }

    override fun pub(
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
                    future.completeExceptionally(
                        MqttClientException(
                            "Publish message to mqtt topic '$topic' fault.",
                            throwable
                        )
                    )
                } else {
                    future.complete(null)
                }
            }
        return future
    }

    override fun sub(client: Mqtt5BlockingClient, subscriber: ISubscriber): CompletableFuture<Void> {
        val f = CompletableFuture<Void>()

        client.toAsync()
            .subscribeWith()
            .topicFilter(subscriber.topicFilter.trim())
            .callback(SubConsumer(subscriber))
            .send()
            .whenComplete { ack, err ->
                if (err != null) {
                    val sb = StringBuilder()
                        .appendLine("Subscribe mqtt topic '${subscriber.topicFilter.trim()}' fault.")
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

    class SubConsumer(private val sub: ISubscriber) : Consumer<Mqtt5Publish> {
        override fun accept(t: Mqtt5Publish) {
            sub.onSub(t.topic.toString(), t.payloadAsBytes)
        }

    }

}