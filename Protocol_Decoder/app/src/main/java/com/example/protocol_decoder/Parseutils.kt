package com.example.protocol_decoder

import java.lang.IllegalArgumentException

data class ParseResult(
    val deviceId: Int,
    val command: Int,
    val length: Int,
    val payload: ByteArray,
    val checksumGiven: Int,
    val checksumCalc: Int,
    val crcGiven: Int,
    val crcCalc: Int,
    val validChecksum: Boolean,
    val validCrc: Boolean,
    val errors: List<String>
)

fun sanitizeHex(input: String): String {
    return input.replace(Regex("(?i)0x|\\s+|\\|"), "")
}

@Throws(IllegalArgumentException::class)
fun hexToBytes(hex: String): ByteArray {
    val clean = sanitizeHex(hex)
    require(clean.length % 2 == 0) { "Hex string must have even length" }
    val out = ByteArray(clean.length / 2)
    for (i in clean.indices step 2) {
        out[i / 2] = clean.substring(i, i + 2).toInt(16).toByte()
    }
    return out
}

fun calcChecksumOver(bytes: ByteArray): Int {
    var sum = 0
    for (b in bytes) {
        sum = (sum + (b.toInt() and 0xFF)) and 0xFF
    }
    return sum
}

fun crc16Ccitt(data: ByteArray, init: Int = 0xFFFF): Int {
    var crc = init and 0xFFFF
    for (b in data) {
        crc = crc xor ((b.toInt() and 0xFF) shl 8)
        for (i in 0 until 8) {
            crc = if ((crc and 0x8000) != 0) {
                ((crc shl 1) xor 0x1021) and 0xFFFF
            } else {
                (crc shl 1) and 0xFFFF
            }
        }
    }
    return crc and 0xFFFF
}

fun parseFrame(hex: String): ParseResult {
    val errors = mutableListOf<String>()
    val b = try {
        hexToBytes(hex)
    } catch (e: Exception) {
        throw IllegalArgumentException("Invalid hex: ${e.message}")
    }

    if (b.size < 7) errors += "Frame too short"
    if (b.first() != 0x7E.toByte() || b.last() != 0x7E.toByte()) errors += "Missing start/end 0x7E"

    if (b.size < 7) throw IllegalArgumentException(errors.joinToString("; "))

    val device = b[1].toInt() and 0xFF
    val cmd = b[2].toInt() and 0xFF
    val len = b[3].toInt() and 0xFF

    val expectedSize = 7 + len
    if (b.size != expectedSize) errors += "Length mismatch: expected $expectedSize, actual ${b.size}"

    val payload = if (len > 0) b.copyOfRange(4, 4 + len) else ByteArray(0)
    val checksumGiven = b[4 + len].toInt() and 0xFF
    val crcHigh = b[5 + len]
    val crcLow = b[6 + len]
    val crcGiven = ((crcHigh.toInt() and 0xFF) shl 8) or (crcLow.toInt() and 0xFF)

    val bytesForCheck = b.sliceArray(1 .. 3 + len) // device..length..payload
    val checksumCalc = calcChecksumOver(bytesForCheck)
    val crcCalc = crc16Ccitt(bytesForCheck)

    val validChecksum = checksumCalc == checksumGiven
    val validCrc = crcCalc == crcGiven

    return ParseResult(
        deviceId = device,
        command = cmd,
        length = len,
        payload = payload,
        checksumGiven = checksumGiven,
        checksumCalc = checksumCalc,
        crcGiven = crcGiven,
        crcCalc = crcCalc,
        validChecksum = validChecksum,
        validCrc = validCrc,
        errors = errors
    )
}
