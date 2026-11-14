package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.DrumKit
import com.example.audio.DrumSound
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
fun DrumPadsView(
    currentKit: DrumKit,
    isPlayingBeat: Boolean,
    isSyncStart: Boolean,
    bpm: Int,
    onSelectKit: (DrumKit) -> Unit,
    onTriggerDrum: (DrumSound, Float) -> Unit,
    onToggleBeat: () -> Unit,
    onSyncStart: () -> Unit,
    onFillIn: () -> Unit,
    onTapTempo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("drum_pads_view"),
        color = YamahaPanelCard,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Kit Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KIT:",
                    color = Color(0xFF8E98AA),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                for (kit in DrumKit.entries) {
                    val isSelected = (kit == currentKit)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectKit(kit) },
                        label = {
                            Text(
                                text = kit.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = YamahaCyanLcd.copy(alpha = 0.2f),
                            selectedLabelColor = YamahaCyanLcd,
                            containerColor = YamahaControlSurface,
                            labelColor = Color(0xFFB0B6C3)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) YamahaCyanLcd else YamahaBorder,
                            enabled = true,
                            selected = isSelected
                        ),
                        modifier = Modifier.testTag("kit_chip_${kit.id}")
                    )
                }
            }

            // Arranger Quick Trigger Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141822), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Start/Stop Beat button
                    Button(
                        onClick = onToggleBeat,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlayingBeat) YamahaRedAccent else YamahaCyanLcd,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("arranger_play_button")
                    ) {
                        Icon(
                            imageVector = if (isPlayingBeat) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Toggle Beat",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPlayingBeat) "STOP BEAT" else "START BEAT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Sync Start
                    Button(
                        onClick = onSyncStart,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSyncStart) YamahaGold else YamahaControlSurface,
                            contentColor = if (isSyncStart) Color.Black else Color(0xFFB0B6C3)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = if (isSyncStart) "SYNC READY" else "SYNC START",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Fill In button
                    Button(
                        onClick = onFillIn,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YamahaControlSurface,
                            contentColor = Color(0xFFE2E4EC)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("FILL IN", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Tap Tempo & BPM Display
                Button(
                    onClick = onTapTempo,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YamahaControlSurface,
                        contentColor = YamahaGold
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "TAP ($bpm)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 12-Pad Grid (3 rows x 4 columns)
            val padRows = listOf(
                listOf(DrumSound.CRASH to PadSpecial, DrumSound.TOM_HIGH to PadPerc, DrumSound.TOM_LOW to PadPerc, DrumSound.RIDE to PadSpecial),
                listOf(DrumSound.HIHAT_CLOSED to PadHiHat, DrumSound.HIHAT_OPEN to PadHiHat, DrumSound.CLAP to PadSnare, DrumSound.RIMSHOT to PadSnare),
                listOf(DrumSound.KICK to PadKick, DrumSound.SNARE to PadSnare, DrumSound.CONGA to PadPerc, DrumSound.COWBELL to PadSpecial)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (row in padRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for ((sound, color) in row) {
                            Box(modifier = Modifier.weight(1f)) {
                                DrumPadItem(
                                    sound = sound,
                                    padColor = color,
                                    onTrigger = { vel -> onTriggerDrum(sound, vel) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrumPadItem(
    sound: DrumSound,
    padColor: Color,
    onTrigger: (Float) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            onTrigger(0.95f)
        }
    }

    val padBg = if (isPressed) {
        Brush.verticalGradient(
            listOf(padColor, padColor.copy(alpha = 0.7f))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFF262D3D), Color(0xFF1B202B))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(if (isPressed) 6.dp else 2.dp, RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .background(padBg)
            .border(
                1.5.dp,
                if (isPressed) padColor else padColor.copy(alpha = 0.35f),
                RoundedCornerShape(6.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null) {}
            .padding(4.dp)
            .testTag("drum_pad_${sound.name}"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = sound.shortCode,
                color = if (isPressed) Color.White else padColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = sound.displayName,
                color = if (isPressed) Color.White else Color(0xFF9EA7B8),
                fontSize = 9.sp,
                maxLines = 1,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
