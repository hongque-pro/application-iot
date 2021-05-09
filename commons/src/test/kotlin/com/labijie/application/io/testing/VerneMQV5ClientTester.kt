package com.labijie.application.io.testing

import com.labijie.application.iot.exception.MqttClientConnectionException
import com.labijie.application.iot.mqtt.subscribe
import com.labijie.application.iot.mqtt.vernmq.VerneMQV5Client
import com.labijie.infra.spring.configuration.NetworkConfig
import com.labijie.infra.utils.recurseCause
import org.junit.jupiter.api.Assertions
import org.springframework.boot.web.client.RestTemplateBuilder
import java.time.Duration
import java.util.concurrent.ExecutionException
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.test.Test

class VerneMQV5ClientTester {

    private val template = RestTemplateBuilder().setConnectTimeout(Duration.ofSeconds(3)).build()

    private fun createMQV5Client(): VerneMQV5Client {
        val properties = TestUtils.createTestIotProperties()
        val client = VerneMQV5Client(NetworkConfig(null), properties.mqtt, template)
        return client
    }

    @Test
    fun testGetVerneMqNodes() {
        val client = createMQV5Client()

        client.use {
            val nodes = it.getClusterNodes()

            Assertions.assertTrue(nodes.isNotEmpty())
            Assertions.assertTrue(nodes.first().listeners.isNotEmpty())
        }
    }


    @Test
    fun testConnectVerneMq() {
        createMQV5Client().use {
            try {
                val r = it.connect(Duration.ofSeconds(5))
                r.get()

            } catch (e: ExecutionException) {
                val ex = e.recurseCause(MqttClientConnectionException::class)
                Assertions.assertTrue(ex is MqttClientConnectionException)
            }
        }
    }


    @Test
    fun testPublishVerneMq() {
        createMQV5Client().use {
            try {
                it.connect(Duration.ofSeconds(5)).get()
                it.publish("/test", "test-message".toByteArray(Charsets.UTF_8)).get()

            } catch (e: ExecutionException) {
                val ex = e.recurseCause(MqttClientConnectionException::class)
                Assertions.assertTrue(ex is MqttClientConnectionException)
            }
        }
    }

    @Test
    fun testSubVerneMq() {
        val sendTopic = "/test"
        val sendData = "REDAFVDAWAER#@$%#%$#"
        val s = Semaphore(1)
        s.acquire()

        createMQV5Client().use {
            try {
                var receivedTopic: String = ""
                var receivedData: String = ""
                it.subscribe("/test") { t, data ->
                    s.release()
                    receivedTopic = t
                    receivedData = data.toString(Charsets.UTF_8)
                }
                it.connect(Duration.ofSeconds(5)).get()
                it.publish(sendTopic, sendData.toByteArray(Charsets.UTF_8)).get()

                s.tryAcquire(3, TimeUnit.SECONDS)

                Assertions.assertEquals(sendTopic, receivedTopic)
                Assertions.assertEquals(sendData, receivedData)

            } catch (e: ExecutionException) {
                val ex = e.recurseCause(MqttClientConnectionException::class)
                Assertions.assertTrue(ex is MqttClientConnectionException)
            }
        }
    }

}