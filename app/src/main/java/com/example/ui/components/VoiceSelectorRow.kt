package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.InstrumentType
import com.example.ui.theme.YamahaBorder
import com.example.ui.theme.YamahaControlSurface
import com.example.ui.theme.YamahaCyanLcd
import com.example.ui.theme.YamahaGold

@Composable
fun VoiceSelectorRow(
    currentInstrument: InstrumentType,
    dualInstrument: InstrumentType?,
    isDualOn: Boolean,
    onSelectInstrument: (InstrumentType) -> Unit,
    onSelectDualInstrument: (InstrumentType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "VOICE:",
            color = Color(0xFF8E98AA),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )

        for (inst in InstrumentType.entries) {
            val isPrimary = (inst == currentInstrument)
            val isDual = isDualOn && (inst == dualInstrument)

            FilterChip(
                selected = isPrimary || isDual,
                onClick = {
                    if (isDualOn && isPrimary) {
                        // In dual mode clicking primary allows setting dual
                        onSelectDualInstrument(inst)
                    } else {
                        onSelectInstrument(inst)
                    }
                },
                label = {
                    Text(
                        text = "${String.format("%02d", inst.id)} ${inst.displayName}",
                        fontSize = 11.sp,
                        fontWeight = if (isPrimary || isDual) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = if (isPrimary) YamahaCyanLcd.copy(alpha = 0.22f) else YamahaGold.copy(alpha = 0.22f),
                    selectedLabelColor = if (isPrimary) YamahaCyanLcd else YamahaGold,
                    containerColor = YamahaControlSurface,
                    labelColor = Color(0xFFB0B6C3)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = when {
                        isPrimary -> YamahaCyanLcd
                        isDual -> YamahaGold
                        else -> YamahaBorder
                    },
                    enabled = true,
                    selected = isPrimary || isDual
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("voice_chip_${inst.id}")
            )
        }
    }
}
