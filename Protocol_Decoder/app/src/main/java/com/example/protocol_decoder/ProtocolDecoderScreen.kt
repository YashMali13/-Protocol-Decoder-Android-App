package com.example.protocoldemo.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


import com.example.protocol_decoder.ProtocolFrame
import com.example.protocol_decoder.ProtocolUtils

@Composable
fun ProtocolDecoderScreen() {
    var inputHex by remember { mutableStateOf("") }
    var decodedFrame by remember { mutableStateOf<ProtocolFrame?>(null) }
    var statusMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            modifier = Modifier.padding(top = 7.dp),
            text = "📱 Protocol Decoder",

            fontSize = 35.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0288D1)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Input box
        OutlinedTextField(
            value = inputHex,
            onValueChange = { inputHex = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Enter Hex Frame") },
            placeholder = { Text("Example: 7E 01 10 02 1A 2B 58 12 34 7E") },
            singleLine = false
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                inputHex = "7E 01 10 02 1A 2B 58 12 34 7E"
            }) {
                Text("Paste Example")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                decodedFrame = ProtocolUtils.parseHeader(inputHex)
                statusMessage = if (decodedFrame != null) "Header Parsed" else "Invalid Frame"
            }) { Text("Parse Header") }

            Button(onClick = {
                decodedFrame = ProtocolUtils.extractPayload(inputHex)
                statusMessage = if (decodedFrame != null) "Payload Extracted" else "Invalid Frame"
            }) { Text("Show Payload") }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    decodedFrame = ProtocolUtils.validateFrame(inputHex)
                    statusMessage = if (decodedFrame?.checksumValid == true &&
                        decodedFrame?.crcValid == true
                    ) "✅ Frame is VALID" else "❌ Frame is INVALID"
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
            ) {
                Text("Validate Frame", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Output
        decodedFrame?.let { frame ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Device ID: 0x${frame.deviceId.toString(16)} (${frame.deviceId})")
                    Text("Command: 0x${frame.command.toString(16)}")
                    Text("Length: ${frame.length}")
                    Text("Payload: ${frame.payload.joinToString(" ") { "%02X".format(it) }}")
                    Text("Checksum Valid: ${frame.checksumValid}")
                    Text("CRC Valid: ${frame.crcValid}")
                }
            }
        }

        if (statusMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(statusMessage, fontWeight = FontWeight.Bold)
        }
    }
}
