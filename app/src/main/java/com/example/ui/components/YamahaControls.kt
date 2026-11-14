package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.KeyLabelsMode
import com.example.ui.theme.YamahaBorder
import com.example.ui.theme.YamahaControlSurface
import com.example.ui.theme.YamahaCyanLcd
import com.example.ui.theme.YamahaGold
import com.example.ui.theme.YamahaPanelCard
import com.example.ui.theme.YamahaRedAccent

@Composable
fun YamahaControls(
    octaveOffset: Int,
    transposeSemitones: Int,
    keyLabelsMode: KeyLabelsMode,
    isSustainOn: Boolean,
    isDualOn: Boolean,
    keyZoom: Float,
    masterVolume: Float,
    reverbLevel: Float,
    pitchBend: Float,
    modulation: Float,
    onOctaveChange: (Int) -> Unit,
    onTransposeChange: (Int) -> Unit,
    onToggleLabels: () -> Unit,
    onToggleSustain: () -> Unit,
    onToggleDual: () -> Unit,
    onZoomChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onReverbChange: (Float) -> Unit,
    onPitchBendChange: (Float) -> Unit,
    onModulationChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("yamaha_controls_panel"),
        color = YamahaPanelCard,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Performance wheels, Octave, Transpose, Sustain
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pitch Bend Wheel (spring return) & Mod Wheel
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PitchWheel(
                        value = pitchBend,
                        onValueChange = onPitchBendChange,
                        modifier = Modifier.testTag("pitch_bend_wheel")
                    )
                    ModWheel(
                        value = modulation,
                        onValueChange = onModulationChange,
                        modifier = Modifier.testTag("modulation_wheel")
                    )
                }

                // Octave & Transpose block
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Octave
                    ControlPill(
                        label = "OCTAVE",
                        value = if (octaveOffset > 0) "+$octaveOffset" else "$octaveOffset",
                        onDecrement = { onOctaveChange(-1) },
                        onIncrement = { onOctaveChange(1) }
                    )

                    // Transpose
                    ControlPill(
                        label = "TRANS",
                        value = if (transposeSemitones > 0) "+$transposeSemitones" else "$transposeSemitones",
                        onDecrement = { onTransposeChange(-1) },
                        onIncrement = { onTransposeChange(1) }
                    )
                }

                // Sustain Pedal Button with LED
                SustainPedalButton(
                    isSustainOn = isSustainOn,
                    onClick = onToggleSustain
                )

                // Quick Toggles: Dual Voice & Key Labels & Zoom
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Dual Voice Layer
                    IconButton(
                        onClick = onToggleDual,
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                if (isDualOn) YamahaGold.copy(alpha = 0.2f) else YamahaControlSurface,
                                RoundedCornerShape(6.dp)
                            )
                            .border(
                                1.dp,
                                if (isDualOn) YamahaGold else YamahaBorder,
                                RoundedCornerShape(6.dp)
                            )
                            .testTag("toggle_dual_layer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Dual Voice Layer",
                            tint = if (isDualOn) YamahaGold else Color(0xFFB0B6C3),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Key Labels Mode
                    IconButton(
                        onClick = onToggleLabels,
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                if (keyLabelsMode != KeyLabelsMode.NONE) YamahaCyanLcd.copy(alpha = 0.2f) else YamahaControlSurface,
                                RoundedCornerShape(6.dp)
                            )
                            .border(
                                1.dp,
                                if (keyLabelsMode != KeyLabelsMode.NONE) YamahaCyanLcd else YamahaBorder,
                                RoundedCornerShape(6.dp)
                            )
                            .testTag("toggle_key_labels_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TextFields,
                            contentDescription = "Key Labels",
                            tint = if (keyLabelsMode != KeyLabelsMode.NONE) YamahaCyanLcd else Color(0xFFB0B6C3),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Zoom Key buttons
                    IconButton(
                        onClick = { onZoomChange((keyZoom - 0.1f).coerceIn(0.75f, 1.4f)) },
                        modifier = Modifier
                            .size(38.dp)
                            .background(YamahaControlSurface, RoundedCornerShape(6.dp))
                            .border(1.dp, YamahaBorder, RoundedCornerShape(6.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomOut,
                            contentDescription = "Zoom Out Keys",
                            tint = Color(0xFFB0B6C3),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { onZoomChange((keyZoom + 0.1f).coerceIn(0.75f, 1.4f)) },
                        modifier = Modifier
                            .size(38.dp)
                            .background(YamahaControlSurface, RoundedCornerShape(6.dp))
                            .border(1.dp, YamahaBorder, RoundedCornerShape(6.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Zoom In Keys",
                            tint = Color(0xFFB0B6C3),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Bottom Sliders Row: Master Volume & Reverb
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Master Volume Slider
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Master Volume",
                        tint = YamahaCyanLcd,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "VOL ${(masterVolume * 100).toInt()}%",
                        color = Color(0xFFC0C7D5),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(62.dp)
                    )
                    Slider(
                        value = masterVolume,
                        onValueChange = onVolumeChange,
                        colors = SliderDefaults.colors(
                            thumbColor = YamahaCyanLcd,
                            activeTrackColor = YamahaCyanLcd,
                            inactiveTrackColor = Color(0xFF1B2330)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp)
                            .testTag("master_volume_slider")
                    )
                }

                // Reverb Level Slider
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "REVERB ${(reverbLevel * 100).toInt()}%",
                        color = Color(0xFFC0C7D5),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(80.dp)
                    )
                    Slider(
                        value = reverbLevel,
                        onValueChange = onReverbChange,
                        colors = SliderDefaults.colors(
                            thumbColor = YamahaGold,
                            activeTrackColor = YamahaGold,
                            inactiveTrackColor = Color(0xFF1B2330)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp)
                            .testTag("reverb_level_slider")
                    )
                }
            }
        }
    }
}

@Composable
private fun PitchWheel(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val heightPx = 54f
    var dragAccum by remember { mutableFloatStateOf(0f) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = "PITCH",
            color = Color(0xFF8E98AA),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .width(26.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF161A22), Color(0xFF282F3D), Color(0xFF161A22))
                    )
                )
                .border(1.dp, YamahaBorder, RoundedCornerShape(4.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            dragAccum = 0f
                            onValueChange(0f) // Spring return!
                        },
                        onDragCancel = {
                            dragAccum = 0f
                            onValueChange(0f)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragAccum -= (dragAmount.y / heightPx) * 2f
                            val clamped = dragAccum.coerceIn(-1f, 1f)
                            onValueChange(clamped)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Center notch indicator
            val notchOffset = (-value * 14).dp
            Box(
                modifier = Modifier
                    .offset(y = notchOffset)
                    .width(18.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (value != 0f) YamahaRedAccent else YamahaCyanLcd)
            )
        }
    }
}

@Composable
private fun ModWheel(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val heightPx = 54f
    var currentMod by remember(value) { mutableFloatStateOf(value) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = "MOD",
            color = Color(0xFF8E98AA),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .width(26.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF161A22), Color(0xFF282F3D), Color(0xFF161A22))
                    )
                )
                .border(1.dp, YamahaBorder, RoundedCornerShape(4.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        currentMod = (currentMod - (dragAmount.y / heightPx)).coerceIn(0f, 1f)
                        onValueChange(currentMod)
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            // Track fill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(value.coerceIn(0.05f, 1f))
                    .background(YamahaCyanLcd.copy(alpha = 0.4f))
            )
            // Cursor
            val notchOffset = (-value * 34).dp
            Box(
                modifier = Modifier
                    .offset(y = notchOffset)
                    .width(18.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(YamahaCyanLcd)
            )
        }
    }
}

@Composable
private fun ControlPill(
    label: String,
    value: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = Color(0xFF8E98AA),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .background(YamahaControlSurface, RoundedCornerShape(6.dp))
                .border(1.dp, YamahaBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 2.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onDecrement() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease $label",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = value,
                color = YamahaGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onIncrement() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase $label",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun SustainPedalButton(
    isSustainOn: Boolean,
    onClick: () -> Unit
) {
    val ledColor by animateColorAsState(
        targetValue = if (isSustainOn) YamahaRedAccent else Color(0xFF4A1016),
        label = "pedal_led"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = "SUSTAIN",
            color = Color(0xFF8E98AA),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .height(38.dp)
                .background(
                    if (isSustainOn) Color(0xFF381418) else YamahaControlSurface,
                    RoundedCornerShape(6.dp)
                )
                .border(
                    1.dp,
                    if (isSustainOn) YamahaRedAccent else YamahaBorder,
                    RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(ledColor)
            )
            Text(
                text = if (isSustainOn) "PEDAL ON" else "PEDAL",
                color = if (isSustainOn) Color.White else Color(0xFFB0B6C3),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
