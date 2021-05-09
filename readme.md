# Application IOT 文档目录

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

直接通过 Spring 注入 **IMqttClient**

```kotlin
interface IMqttClient {
    val isConnected: Boolean
    fun connect(timeout: Duration? = null): CompletableFuture<Void>
    fun disconnect()
    fun getClusterNodes(): List<MqttNode>
    fun pushMessage(topic: String, payload: ByteArray, qos: MqttQos = MqttQos.AT_MOST_ONCE): CompletableFuture<Void>
    fun subscribe(subscriber: ISubscriber): CompletableFuture<Void>
}
```
