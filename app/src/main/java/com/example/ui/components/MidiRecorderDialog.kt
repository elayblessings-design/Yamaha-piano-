package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.YamahaCyanLcd
import com.example.ui.theme.YamahaPanelSurface
import com.example.ui.theme.YamahaRedAccent

@Composable
fun MidiRecorderDialog(
    durationMs: Long,
    bpm: Int,
    instrumentName: String,
    onSave: (String) -> Unit,
    onDiscard: () -> Unit
) {
    var title by remember { mutableStateOf("Yamaha Jam ${System.currentTimeMillis() % 10000}") }

    val durSec = (durationMs / 1000) % 60
    val durMin = (durationMs / 1000) / 60

    Dialog(onDismissRequest = onDiscard) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("save_recording_dialog"),
            shape = RoundedCornerShape(12.dp),
            color = YamahaPanelSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = "Save Recording",
                        tint = YamahaRedAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SAVE MIDI RECORDING",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "Summary: $instrumentName • $bpm BPM • Duration ${String.format("%d:%02d", durMin, durSec)}",
                    color = YamahaCyanLcd,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Recording Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = YamahaCyanLcd,
                        unfocusedBorderColor = Color(0xFF384257),
                        focusedLabelColor = YamahaCyanLcd
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recording_title_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDiscard,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFB0B6C3)
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("DISCARD")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(title) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YamahaRedAccent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("save_recording_confirm_button")
                    ) {
                        Text("SAVE RECORDING", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
