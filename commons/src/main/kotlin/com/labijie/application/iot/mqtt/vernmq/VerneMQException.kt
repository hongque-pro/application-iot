package com.labijie.application.iot.mqtt.vernmq

import com.labijie.application.ApplicationRuntimeException

class VerneMQException(message:String, cause:Throwable? = null)
    : ApplicationRuntimeException(message, cause)