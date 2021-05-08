package com.labijie.application.iot.configuration

data class MqttClusterProperties(
    var enabled: Boolean = true,
    var clusterProvider: String = "vernemq",
    var vernemq: VerneMQProperties = VerneMQProperties()
)