package com.example.protocol_decoder


object ProtocolUtils {

    private fun hexStringToBytes(hex: String): ByteArray {
        return hex.split(" ")
            .filter { it.isNotBlank() }
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    fun parseHeader(hex: String): ProtocolFrame? {
        val bytes = hexStringToBytes(hex)
        if (bytes.size < 5 || bytes.first() != 0x7E.toByte() || bytes.last() != 0x7E.toByte()) {
            return null
        }

        val deviceId = bytes[1].toInt() and 0xFF
        val command = bytes[2].toInt() and 0xFF
        val length = bytes[3].toInt() and 0xFF

        return ProtocolFrame(deviceId, command, length, emptyList())
    }

    fun extractPayload(hex: String): ProtocolFrame? {
        val bytes = hexStringToBytes(hex)
        if (bytes.size < 5 || bytes.first() != 0x7E.toByte() || bytes.last() != 0x7E.toByte()) {
            return null
        }

        val deviceId = bytes[1].toInt() and 0xFF
        val command = bytes[2].toInt() and 0xFF
        val length = bytes[3].toInt() and 0xFF
        if (bytes.size < 7 + length) return null

        val payload = bytes.slice(4 until (4 + length)).map { it.toInt() and 0xFF }
        return ProtocolFrame(deviceId, command, length, payload)
    }

    fun validateFrame(hex: String): ProtocolFrame? {
        val bytes = hexStringToBytes(hex)
        if (bytes.size < 7 || bytes.first() != 0x7E.toByte() || bytes.last() != 0x7E.toByte()) {
            return null
        }

        val deviceId = bytes[1].toInt() and 0xFF
        val command = bytes[2].toInt() and 0xFF
        val length = bytes[3].toInt() and 0xFF
        val payload = bytes.slice(4 until 4 + length).map { it.toInt() and 0xFF }
        val checksum = bytes[4 + length].toInt() and 0xFF
        val crcHigh = bytes[5 + length].toInt() and 0xFF
        val crcLow = bytes[6 + length].toInt() and 0xFF
        val givenCrc = (crcHigh shl 8) or crcLow

        val calculatedChecksum =
            (deviceId + command + length + payload.sum()) and 0xFF
        val calculatedCrc = crc16CCITT(byteArrayOf(
            bytes[1], bytes[2], bytes[3], *payload.map { it.toByte() }.toByteArray()
        ))

        return ProtocolFrame(
            deviceId = deviceId,
            command = command,
            length = length,
            payload = payload,
            checksumValid = (checksum == calculatedChecksum),
            crcValid = (givenCrc == calculatedCrc)
        )
    }

    private fun crc16CCITT(data: ByteArray): Int {
        var crc = 0xFFFF
        for (b in data) {
            crc = crc xor (b.toInt() shl 8)
            for (i in 0 until 8) {
                crc = if ((crc and 0x8000) != 0) {
                    (crc shl 1) xor 0x1021
                } else {
                    crc shl 1
                }
            }
        }
        return crc and 0xFFFF
    }
}
