package com.labijie.application.io.testing

import com.labijie.application.iot.exception.MqttClientConnectionException
import com.labijie.application.iot.exception.MqttClientException
import com.labijie.application.iot.mqtt.vernmq.VerneMQV5Client
import com.labijie.infra.spring.configuration.NetworkConfig
import com.labijie.infra.utils.recurseCause
import okhttp3.internal.wait
import org.junit.jupiter.api.Assertions
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import java.time.Duration
import java.util.concurrent.ExecutionException
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
                val r = it.connect(Duration.ofSeconds(5)).thenApply {
//                    while (true){
//                        Thread.sleep(100)
//                    }
                }
                r.get()

            }catch (e:ExecutionException){
                val ex = e.recurseCause(MqttClientConnectionException::class)
                Assertions.assertTrue(ex is MqttClientConnectionException)
            }
        }
    }


}