package com.labijie.application.iot.exception

import com.labijie.application.ApplicationRuntimeException

open class MqttClientException(message:String, cause:Throwable? = null)
    : ApplicationRuntimeException(message, cause)