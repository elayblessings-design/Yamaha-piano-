package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.DrumKit
import com.example.audio.InstrumentType
import com.example.rhythm.ArrangerStyle
import com.example.rhythm.StyleSection
import com.example.ui.theme.YamahaCyanLcd
import com.example.ui.theme.YamahaGold
import com.example.ui.theme.YamahaLcdBackground
import com.example.ui.theme.YamahaLcdMuted
import com.example.ui.theme.YamahaLcdText
import com.example.ui.theme.YamahaRedAccent

@Composable
fun YamahaDisplay(
    instrument: InstrumentType,
    dualInstrument: InstrumentType?,
    isDualOn: Boolean,
    drumKit: DrumKit,
    selectedStyle: ArrangerStyle,
    styleSection: StyleSection,
    bpm: Int,
    bar: Int,
    beat: Int,
    detectedChord: String,
    vuMeterLevel: Float,
    isRecording: Boolean,
    recordingDurationMs: Long,
    isPlayingRhythm: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rec_blink")
    val recAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rec_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(YamahaLcdBackground)
            .border(2.dp, Color(0xFF16323F), RoundedCornerShape(8.dp))
            .padding(10.dp)
            .testTag("yamaha_lcd_display")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header: Branding & Recording Indicator & VU Meter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "YAMAHA",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.8.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WORKSTATION LCD",
                        color = YamahaLcdMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // REC Status & Meter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isRecording) {
                        val sec = (recordingDurationMs / 1000) % 60
                        val min = (recordingDurationMs / 1000) / 60
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FiberManualRecord,
                                contentDescription = "Recording Active",
                                tint = YamahaRedAccent.copy(alpha = recAlpha),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("REC %02d:%02d", min, sec),
                                color = YamahaRedAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Mini VU Meter Bars
                    VuMeter(level = vuMeterLevel)
                }
            }

            // Main Display Screen Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF040E14), RoundedCornerShape(4.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Voice Info
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(
                        text = "VOICE",
                        color = YamahaLcdMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "[${String.format("%02d", instrument.id)}] ${instrument.displayName.uppercase()}",
                        color = YamahaLcdText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                    if (isDualOn && dualInstrument != null) {
                        Text(
                            text = "+ LAYER: ${dualInstrument.displayName.uppercase()}",
                            color = YamahaGold,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Text(
                            text = "KIT: ${drumKit.displayName.uppercase()}",
                            color = YamahaLcdMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }
                }

                // Center Column: Style & Section
                Column(
                    modifier = Modifier.weight(1.2f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isPlayingRhythm) "BEAT RUNNING" else "ARRANGER STYLE",
                        color = if (isPlayingRhythm) YamahaCyanLcd else YamahaLcdMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedStyle.name.uppercase(),
                        color = if (isPlayingRhythm) YamahaCyanLcd else YamahaLcdText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                    Text(
                        text = "SEC: ${styleSection.name} | ${bpm} BPM",
                        color = YamahaGold,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Right Column: Chord & Beat Counter
                Column(
                    modifier = Modifier.weight(0.9f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "CHORD",
                        color = YamahaLcdMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = detectedChord,
                        color = if (detectedChord != "--") YamahaGold else YamahaLcdText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (isPlayingRhythm) "BAR $bar : $beat" else "TEMPO $bpm",
                        color = YamahaCyanLcd,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun VuMeter(level: Float) {
    val bars = 6
    val activeBars = (level * bars).toInt().coerceIn(0, bars)

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(14.dp)
    ) {
        for (i in 0 until bars) {
            val isActive = i < activeBars
            val barColor = when {
                i >= 5 -> YamahaRedAccent
                i >= 4 -> YamahaGold
                else -> YamahaCyanLcd
            }
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight((i + 1f) / bars)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (isActive) barColor else Color(0xFF132B36))
            )
        }
    }
}
