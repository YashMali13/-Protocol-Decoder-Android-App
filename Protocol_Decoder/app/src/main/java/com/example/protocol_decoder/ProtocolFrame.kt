package com.example.protocol_decoder

data class ProtocolFrame(
    val deviceId: Int,
    val command: Int,
    val length: Int,
    val payload: List<Int>,
    val checksumValid: Boolean = false,
    val crcValid: Boolean = false
)
