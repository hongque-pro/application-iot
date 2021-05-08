package com.labijie.application.iot

import com.labijie.application.ApplicationRuntimeException
import com.labijie.infra.utils.ifNullOrBlank
import org.springframework.http.HttpStatus
import java.lang.StringBuilder

class WebApiInvocationException(
    private val requestUrl: String? = null,
    private val httpStatus: HttpStatus? = null,
    message: String? = null,
) : ApplicationRuntimeException(message) {

    override val message: String?
        get() {
            val sb = StringBuilder().appendLine(super.message.ifNullOrBlank { "Invoke web api fault." })
            if(!this.requestUrl.isNullOrBlank()){
                sb.appendLine("url: $requestUrl")
            }
            if(this.httpStatus != null){
               sb.appendLine("http status: ${httpStatus.value()}")
            }
            return sb.toString()
        }
}