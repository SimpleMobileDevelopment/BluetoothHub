package com.simple.bluetoothhub

data class ConnectionDO(
    val connectionState: DeviceConnectionState,
    val data: String?
)