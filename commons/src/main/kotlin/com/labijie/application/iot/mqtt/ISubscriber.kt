package com.labijie.application.iot.mqtt

interface ISubscriber {
    val topicFilter: String
    fun onSub(topic: String, payload: ByteArray)
}