package com.labijie.application.iot.mqtt.vernmq

import com.fasterxml.jackson.annotation.JsonProperty

/*
       VerneMQ 返回格式：
       [
           {
               "VerneMQ@172.19.222.248":{
                   "num_online":0,
                   "num_offline":4,
                   "msg_in":0,
                   "msg_out":0,
                   "queue_in":83472,
                   "queue_out":0,
                   "queue_drop":0,
                   "queue_unhandled":0,
                   "num_subscriptions":2,
                   "num_retained":0,
                   "matches_local":83472,
                   "matches_remote":0,
                   "mystatus":[
                       {
                           "VerneMQ@172.19.222.248":true
                       }
                   ],
                   "listeners":[
                       {
                           "type":"http",
                           "status":"running",
                           "ip":"127.0.0.1",
                           "port":8888,
                           "mountpoint":[

                           ],
                           "max_conns":10000
                       },
                       {
                           "type":"http",
                           "status":"running",
                           "ip":"172.19.222.248",
                           "port":8888,
                           "mountpoint":[

                           ],
                           "max_conns":10000
                       },
                       {
                           "type":"vmq",
                           "status":"running",
                           "ip":"172.19.222.248",
                           "port":44053,
                           "mountpoint":[

                           ],
                           "max_conns":10000
                       },
                       {
                           "type":"mqttws",
                           "status":"running",
                           "ip":"172.19.222.248",
                           "port":8080,
                           "mountpoint":[

                           ],
                           "max_conns":10000
                       },
                       {
                           "type":"mqtt",
                           "status":"running",
                           "ip":"172.19.222.248",
                           "port":1883,
                           "mountpoint":[

                           ],
                           "max_conns":10000
                       }
                   ],
                   "version":"1.11.0"
               }
           }
       ]

       */

data class VerneMQNodeStatus(
    @get:JsonProperty("num_online")
    var onlineCount: Long = 0,
    @get:JsonProperty("num_offline")
    var offlineCount: Long = 0,
    @get:JsonProperty("msg_in")
    var messageIn: Long = 0,
    @get:JsonProperty("msg_out")
    var messageOut: Long = 0,
    @get:JsonProperty("queue_in")
    var queueIn: Long = 0,
    @get:JsonProperty("queue_out")
    var queueOut: Long = 0,
    @get:JsonProperty("queue_drop")
    var queueDrop: Long = 0,
    @get:JsonProperty("queue_unhandled")
    var queueUnhandled: Long = 0,
    @get:JsonProperty("num_subscriptions")
    var subscriptionCount: Long = 0,
    @get:JsonProperty("num_retained")
    var retainedCount: Long = 0,
    @get:JsonProperty("version")
    var version: String = "",
    @get:JsonProperty("listeners")
    var listeners: List<VerneMQListener> = mutableListOf()
)