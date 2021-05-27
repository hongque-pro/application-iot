package com.labijie.application.iot

import com.hivemq.client.mqtt.datatypes.MqttQos

enum class IotMqttQos {
  /**
   * QoS for at most once delivery according to the capabilities of the underlying network. AT_MOST_ONCE
   */
  QoS0,

  /**
   * QoS for ensuring at least once delivery. AT_LEAST_ONCE
   */
  QoS1,

  /**
   * QoS for ensuring exactly once delivery. EXACTLY_ONCE
   */
  QoS2;


  open fun toMqttQos(): MqttQos {
    return MqttQos.values()[this.ordinal]
  }
}