# Application IOT

![maven central version](https://img.shields.io/maven-central/v/com.labijie.application/iot-commons?style=flat-square)
![workflow status](https://img.shields.io/github/workflow/status/hongque-pro/application-iot/Gradle%20Build%20And%20Release?label=CI%20publish&style=flat-square)
![license](https://img.shields.io/github/license/hongque-pro/application-iot?style=flat-square)

封装常用 IOT 技术, 目前实现:

- [x] MQTT Client (自动断线重连，自动切换不可用节点)

## Commons

```groovy
dependencies {
    api "com.labijie.application:iot-commons:<version>"
}
```

### MQTT Client 

简单配置 (VerneMQ 服务器)：

```yaml
application:
  iot:
    mqtt:
      user: u
      password: p
      version: 5.0
      keep-alive: 1m
      vernemq:
        http-url: http://192.168.1.10:8888, http://192.168.1.11:8888, http://192.168.1.12:8888
        api-key: XXXXXXXXX
```

直接通过 Spring 注入 **IMqttClient**

```kotlin
interface IMqttClient {
    val isConnected: Boolean
    fun connect(timeout: Duration? = null): CompletableFuture<Void>
    fun disconnect()
    fun getClusterNodes(): List<MqttNode>
    fun publish(topic: String, payload: ByteArray, qos: MqttQos = MqttQos.AT_MOST_ONCE): CompletableFuture<Void>
    fun subscribe(subscriber: ISubscriber): CompletableFuture<Void>
}
```
