package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.DrumSound
import com.example.rhythm.ArrangerStyle
import com.example.rhythm.StyleLibrary
import com.example.rhythm.StylePattern
import com.example.rhythm.StyleSection
import com.example.ui.theme.PadHiHat
import com.example.ui.theme.PadKick
import com.example.ui.theme.PadPerc
import com.example.ui.theme.PadSnare
import com.example.ui.theme.PadSpecial
import com.example.ui.theme.YamahaBorder
import com.example.ui.theme.YamahaControlSurface
import com.example.ui.theme.YamahaCyanLcd
import com.example.ui.theme.YamahaGold
import com.example.ui.theme.YamahaPanelCard
import com.example.ui.theme.YamahaRedAccent

@Composable
fun BeatSequencerView(
    pattern: StylePattern,
    currentStep: Int,
    isPlaying: Boolean,
    bpm: Int,
    selectedStyle: ArrangerStyle,
    currentSection: StyleSection,
    isMetronomeOn: Boolean,
    isAccompanimentOn: Boolean,
    onToggleStep: (DrumSound, Int) -> Unit,
    onTogglePlay: () -> Unit,
    onBpmChange: (Int) -> Unit,
    onSelectStyle: (ArrangerStyle) -> Unit,
    onSelectSection: (StyleSection) -> Unit,
    onToggleMetronome: () -> Unit,
    onToggleAccompaniment: () -> Unit,
    onClearPattern: () -> Unit,
    modifier: Modifier = Modifier
) {
    var styleMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("beat_sequencer_view"),
        color = YamahaPanelCard,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Arranger Controls: Style Selector, Sections (Main A, Main B, Fill In), BPM
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Style Selector Dropdown
                Box {
                    Button(
                        onClick = { styleMenuExpanded = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YamahaControlSurface,
                            contentColor = YamahaCyanLcd
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(36.dp).testTag("select_style_button")
                    ) {
                        Text(
                            text = "STYLE: ${selectedStyle.name.take(18)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    DropdownMenu(
                        expanded = styleMenuExpanded,
                        onDismissRequest = { styleMenuExpanded = false },
                        modifier = Modifier.background(Color(0xFF1E2330))
                    ) {
                        for (style in StyleLibrary.STYLES) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = style.name,
                                            color = if (style.id == selectedStyle.id) YamahaCyanLcd else Color.White,
                                            fontWeight = if (style.id == selectedStyle.id) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = "${style.category} • ${style.defaultBpm} BPM",
                                            color = Color(0xFF8E98AA),
                                            fontSize = 11.sp
                                        )
                                    }
                                },
                                onClick = {
                                    onSelectStyle(style)
                                    styleMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Section buttons: Main A, Main B, Fill In
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SectionButton(
                        label = "MAIN A",
                        isSelected = currentSection == StyleSection.MAIN_A,
                        onClick = { onSelectSection(StyleSection.MAIN_A) }
                    )
                    SectionButton(
                        label = "MAIN B",
                        isSelected = currentSection == StyleSection.MAIN_B,
                        onClick = { onSelectSection(StyleSection.MAIN_B) }
                    )
                    SectionButton(
                        label = "FILL IN",
                        isSelected = currentSection == StyleSection.FILL_IN,
                        onClick = { onSelectSection(StyleSection.FILL_IN) }
                    )
                }

                // Transport: Play/Pause, Accompaniment, Metronome
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onTogglePlay,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlaying) YamahaRedAccent else YamahaCyanLcd,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(36.dp).testTag("sequencer_play_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Toggle Sequencer",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPlaying) "PAUSE" else "PLAY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Auto-Accompaniment Toggle
                    Button(
                        onClick = onToggleAccompaniment,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAccompanimentOn) YamahaGold else YamahaControlSurface,
                            contentColor = if (isAccompanimentOn) Color.Black else Color(0xFFB0B6C3)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = "ACCOMP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Metronome Toggle
                    Button(
                        onClick = onToggleMetronome,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMetronomeOn) YamahaCyanLcd.copy(alpha = 0.3f) else YamahaControlSurface,
                            contentColor = if (isMetronomeOn) YamahaCyanLcd else Color(0xFFB0B6C3)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("METRO", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    // Clear button
                    Button(
                        onClick = onClearPattern,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YamahaControlSurface,
                            contentColor = Color(0xFFE57373)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Sequencer",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // BPM Slider Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141822), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "Tempo BPM",
                    tint = YamahaGold,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "TEMPO: $bpm BPM",
                    color = YamahaGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(110.dp)
                )
                Slider(
                    value = bpm.toFloat(),
                    onValueChange = { onBpmChange(it.toInt()) },
                    valueRange = 50f..220f,
                    colors = SliderDefaults.colors(
                        thumbColor = YamahaGold,
                        activeTrackColor = YamahaGold,
                        inactiveTrackColor = Color(0xFF1B2330)
                    ),
                    modifier = Modifier.weight(1f).height(24.dp)
                )
            }

            // 16-Step Drum Grid Scrollable Table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Step Counter Header (1..16 grouped by beats)
                    Row(
                        modifier = Modifier.padding(start = 74.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (s in 0 until 16) {
                            val isCurrent = isPlaying && (s == currentStep)
                            val beatGroup = s / 4
                            val isBeatStart = (s % 4 == 0)

                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        when {
                                            isCurrent -> YamahaRedAccent
                                            isBeatStart -> YamahaCyanLcd.copy(alpha = 0.25f)
                                            else -> Color(0xFF1B202C)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${s + 1}",
                                    color = if (isCurrent) Color.White else Color(0xFF8E98AA),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Sequencer Tracks
                    val trackColors = mapOf(
                        DrumSound.KICK to PadKick,
                        DrumSound.SNARE to PadSnare,
                        DrumSound.HIHAT_CLOSED to PadHiHat,
                        DrumSound.HIHAT_OPEN to PadHiHat,
                        DrumSound.CLAP to PadPerc,
                        DrumSound.CONGA to PadSpecial,
                        DrumSound.COWBELL to YamahaGold,
                        DrumSound.RIMSHOT to Color(0xFF90CAF9)
                    )

                    for (track in pattern.tracks) {
                        val color = trackColors[track.sound] ?: PadPerc
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Track Label
                            Box(
                                modifier = Modifier
                                    .width(70.dp)
                                    .height(30.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(YamahaControlSurface)
                                    .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = track.sound.shortCode,
                                    color = color,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // 16 Step Buttons
                            for (stepIdx in 0 until 16) {
                                val isActive = track.steps[stepIdx]
                                val isCurrentPlayhead = isPlaying && (stepIdx == currentStep)
                                val isBeatDivision = (stepIdx % 4 == 0)

                                val stepBg = when {
                                    isActive && isCurrentPlayhead -> Color.White
                                    isActive -> color
                                    isCurrentPlayhead -> YamahaRedAccent.copy(alpha = 0.5f)
                                    isBeatDivision -> Color(0xFF262C3A)
                                    else -> Color(0xFF181C26)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(28.dp, 30.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(stepBg)
                                        .border(
                                            1.dp,
                                            if (isActive) color else YamahaBorder.copy(alpha = 0.5f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .clickable { onToggleStep(track.sound, stepIdx) }
                                        .testTag("step_${track.sound.name}_$stepIdx"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isActive) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (isCurrentPlayhead) color else Color.White)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) YamahaCyanLcd else YamahaControlSurface,
            contentColor = if (isSelected) Color.Black else Color(0xFFB0B6C3)
        ),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
