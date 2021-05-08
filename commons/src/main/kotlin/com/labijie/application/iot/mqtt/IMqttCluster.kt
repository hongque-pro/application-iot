package com.labijie.application.iot.mqtt

interface IMqttCluster {
    fun getNodes(): Collection<MqttNode>
    fun pushMessage(): Unit
}