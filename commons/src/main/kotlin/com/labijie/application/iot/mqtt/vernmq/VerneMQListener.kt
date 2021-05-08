package com.labijie.application.iot.mqtt.vernmq

import com.fasterxml.jackson.annotation.JsonProperty

/*
VerneMQ 返回结构

{
   "type":"http",
   "status":"running",
   "ip":"127.0.0.1",
   "port":8888,
   "mountpoint":[

   ],
   "max_conns":10000
}

*/

data class VerneMQListener(
    var type: String = "",
    var status: String = "",
    var ip: String = "",
    var port: Int = 0,
    @get:JsonProperty("max_conns")
    var maxConns: Int = 0
)