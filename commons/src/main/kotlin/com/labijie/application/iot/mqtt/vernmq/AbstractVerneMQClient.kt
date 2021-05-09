package com.labijie.application.iot.mqtt.vernmq

import com.hivemq.client.mqtt.MqttClient
import com.labijie.application.iot.IotUtils
import com.labijie.application.iot.LoopList
import com.labijie.application.iot.LoopPolicy
import com.labijie.application.iot.WebApiInvocationException
import com.labijie.application.iot.configuration.MqttProperties
import com.labijie.application.iot.mqtt.AbstractMqttClientBase
import com.labijie.application.iot.mqtt.MqttNode
import com.labijie.application.iot.mqtt.MqttNodeListener
import com.labijie.application.toMap
import com.labijie.infra.spring.configuration.NetworkConfig
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.net.URL

abstract class AbstractVerneMQClient<T : MqttClient>(
        networkConfig: NetworkConfig,
        serverProperties: MqttProperties,
        private val restTemplate: RestTemplate,
) : AbstractMqttClientBase<T>(networkConfig, serverProperties) {

    companion object {
        fun ResponseEntity<*>.validateVerneMQResponse(requestUrl: String?, throwIfInvalid: Boolean = false): Boolean {
            if (this.statusCode != HttpStatus.OK) {
                if (throwIfInvalid) {
                    throw WebApiInvocationException(
                            requestUrl,
                            this.statusCode,
                            "Invoke verneMQ status api fault."
                    )
                }
                return false
            }

            if (!this.hasBody()) {
                if (throwIfInvalid) {
                    throw WebApiInvocationException(
                            requestUrl,
                            this.statusCode,
                            "VerneMQ status api response empty body."
                    )
                }
                return false
            }
            return true
        }
    }

    private fun getNodeAddress(nodeName: String): String {
        return if (nodeName.startsWith("VerneMQ@")) {
            //Official docker node name
            nodeName.removePrefix("VerneMQ@")
        } else {
            nodeName
        }
    }

    private val statusUrl by lazy {
        val list = IotUtils.getUriList(serverProperties.vernemq.httpUrl) {
            URL(URL(it), "/status.json").toString()
        }
        LoopList(list, LoopPolicy.Sequence)
    }

    private val statusResponseType: ParameterizedTypeReference<List<Map<String, VerneMQNodeStatus>>> by lazy {
        object :
            ParameterizedTypeReference<List<Map<String, VerneMQNodeStatus>>>() {
        }
    }

    final override fun getClusterNodes(): List<MqttNode> {
        return this.statusUrl.loop { url ->
            val resp = restTemplate.exchange(url, HttpMethod.GET, null, statusResponseType)
            val succeed = resp.validateVerneMQResponse(url)
            if (!succeed) {
                null
            }else {
                resp.body!!.map {
                    val nodeItem = it.entries.first()
                    val listeners = nodeItem.value.listeners.filter { l ->
                        IotUtils.isSupportProtocol(l.type) && l.status == "running"
                    }.map { l ->
                        MqttNodeListener(
                                l.type,
                                l.port
                        )
                    }

                    MqttNode(
                            host = getNodeAddress(nodeItem.key),
                            clientsCount = nodeItem.value.onlineCount,
                            subscriptionCount = nodeItem.value.subscriptionCount,
                            serverProperties = nodeItem.value.toMap(),
                            listeners = listeners
                    )
                }
            }
        } ?: listOf()
    }
}