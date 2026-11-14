package com.example.ui.components

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.KeyLabelsMode
import com.example.ui.theme.PianoBlackKey
import com.example.ui.theme.PianoBlackKeyPressed
import com.example.ui.theme.PianoWhiteKey
import com.example.ui.theme.PianoWhiteKeyBorder
import com.example.ui.theme.PianoWhiteKeyPressed
import com.example.ui.theme.YamahaCyanLcd
import com.example.ui.theme.YamahaGold
import com.example.ui.theme.YamahaRedAccent
import com.example.ui.theme.YamahaRedDark

data class KeyGeometry(
    val midiNote: Int,
    val isBlack: Boolean,
    val noteLabel: String,
    val solfegeLabel: String,
    val whiteKeyIndex: Int
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PianoKeyboard(
    octaveOffset: Int,
    keyLabelsMode: KeyLabelsMode,
    keyZoom: Float,
    activeKeys: Set<Int>,
    playbackKeys: Set<Int>,
    onKeyDown: (Int, Float) -> Unit,
    onKeyUp: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 3 Octaves of playable keys: e.g. base Octave 3 (C3 = 48) shifted by octaveOffset
    val baseMidi = 48 + (octaveOffset * 12)
    val numWhiteKeys = 22 // ~3 octaves from C to C
    val scrollState = rememberScrollState()

    val whiteKeyWidth: Dp = (48.dp * keyZoom).coerceIn(36.dp, 64.dp)
    val whiteKeyHeight: Dp = 190.dp
    val blackKeyWidth: Dp = whiteKeyWidth * 0.62f
    val blackKeyHeight: Dp = whiteKeyHeight * 0.60f

    // Track active pointer IDs to note mapping for multi-touch
    val pointerToNoteMap = remember { mutableStateMapOf<Int, Int>() }

    // Generate keys
    val keysList = remember(baseMidi, numWhiteKeys) {
        val list = mutableListOf<KeyGeometry>()
        val whiteNotes = listOf(0, 2, 4, 5, 7, 9, 11) // C, D, E, F, G, A, B
        val noteNames = listOf("C", "D", "E", "F", "G", "A", "B")
        val solfege = listOf("Do", "Re", "Mi", "Fa", "Sol", "La", "Si")

        var currentWhiteIdx = 0
        var midi = baseMidi

        while (currentWhiteIdx < numWhiteKeys) {
            val semitone = midi % 12
            val isWhite = whiteNotes.contains(semitone)

            if (isWhite) {
                val whitePosInOctave = whiteNotes.indexOf(semitone)
                val octaveNum = (midi / 12) - 1
                list.add(
                    KeyGeometry(
                        midiNote = midi,
                        isBlack = false,
                        noteLabel = "${noteNames[whitePosInOctave]}$octaveNum",
                        solfegeLabel = solfege[whitePosInOctave],
                        whiteKeyIndex = currentWhiteIdx
                    )
                )
                currentWhiteIdx++
            } else {
                // Black key belongs between currentWhiteIdx - 1 and currentWhiteIdx
                val prevWhite = list.lastOrNull { !it.isBlack }
                val prevIdx = prevWhite?.whiteKeyIndex ?: 0
                val blackNames = mapOf(1 to "C#", 3 to "D#", 6 to "F#", 8 to "G#", 10 to "A#")
                val blackSolfege = mapOf(1 to "Di", 3 to "Ri", 6 to "Fi", 8 to "Si", 10 to "Li")
                val octaveNum = (midi / 12) - 1
                list.add(
                    KeyGeometry(
                        midiNote = midi,
                        isBlack = true,
                        noteLabel = "${blackNames[semitone] ?: ""}$octaveNum",
                        solfegeLabel = blackSolfege[semitone] ?: "",
                        whiteKeyIndex = prevIdx
                    )
                )
            }
            midi++
        }
        list
    }

    val whiteKeys = remember(keysList) { keysList.filter { !it.isBlack } }
    val blackKeys = remember(keysList) { keysList.filter { it.isBlack } }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("piano_keyboard_section")
    ) {
        // Signature Yamaha Red Felt Strip above keys
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(YamahaRedAccent, YamahaRedDark)
                    )
                )
                .border(0.5.dp, Color(0xFF6B0B14))
        )

        // Scrollable Keyboard Body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(whiteKeyHeight)
                .background(Color(0xFF151820))
                .horizontalScroll(scrollState)
        ) {
            val totalWidth = whiteKeyWidth * numWhiteKeys

            // Multi-touch dispatch area
            Box(
                modifier = Modifier
                    .width(totalWidth)
                    .height(whiteKeyHeight)
                    .pointerInteropFilter { motionEvent ->
                        val action = motionEvent.actionMasked
                        val actionIndex = motionEvent.actionIndex

                        fun findNoteAt(xPx: Float, yPx: Float, density: Float): Pair<Int, Float>? {
                            val whiteWidthPx = (whiteKeyWidth.value * density)
                            val blackWidthPx = (blackKeyWidth.value * density)
                            val blackHeightPx = (blackKeyHeight.value * density)

                            // Check black keys first (higher z-order)
                            if (yPx <= blackHeightPx) {
                                for (bk in blackKeys) {
                                    val leftEdge = (bk.whiteKeyIndex + 1) * whiteWidthPx - (blackWidthPx / 2f)
                                    val rightEdge = leftEdge + blackWidthPx
                                    if (xPx in leftEdge..rightEdge) {
                                        val vel = (yPx / blackHeightPx).coerceIn(0.5f, 1.0f)
                                        return Pair(bk.midiNote, vel)
                                    }
                                }
                            }

                            // Check white keys
                            val whiteIndex = (xPx / whiteWidthPx).toInt()
                            val wk = whiteKeys.getOrNull(whiteIndex)
                            if (wk != null) {
                                val vel = (yPx / (whiteKeyHeight.value * density)).coerceIn(0.4f, 1.0f)
                                return Pair(wk.midiNote, vel)
                            }
                            return null
                        }

                        val density = 3f // approximate fallback, motion event uses pixel units

                        when (action) {
                            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                                val pointerId = motionEvent.getPointerId(actionIndex)
                                val x = motionEvent.getX(actionIndex)
                                val y = motionEvent.getY(actionIndex)
                                val target = findNoteAt(x, y, 2.5f)
                                if (target != null) {
                                    val (note, vel) = target
                                    pointerToNoteMap[pointerId] = note
                                    onKeyDown(note, vel)
                                }
                            }
                            MotionEvent.ACTION_MOVE -> {
                                for (i in 0 until motionEvent.pointerCount) {
                                    val pid = motionEvent.getPointerId(i)
                                    val x = motionEvent.getX(i)
                                    val y = motionEvent.getY(i)
                                    val target = findNoteAt(x, y, 2.5f)
                                    val prevNote = pointerToNoteMap[pid]
                                    if (target != null && target.first != prevNote) {
                                        if (prevNote != null) onKeyUp(prevNote)
                                        pointerToNoteMap[pid] = target.first
                                        onKeyDown(target.first, target.second)
                                    }
                                }
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                                val pointerId = motionEvent.getPointerId(actionIndex)
                                val note = pointerToNoteMap.remove(pointerId)
                                if (note != null) {
                                    onKeyUp(note)
                                }
                            }
                        }
                        true
                    }
            ) {
                // 1. Draw White Keys Row
                Row(modifier = Modifier.fillMaxSize()) {
                    for (wk in whiteKeys) {
                        val isPressed = activeKeys.contains(wk.midiNote)
                        val isPlayback = playbackKeys.contains(wk.midiNote)

                        val keyBg = when {
                            isPlayback -> Brush.verticalGradient(listOf(YamahaCyanLcd.copy(alpha = 0.5f), PianoWhiteKeyPressed))
                            isPressed -> Brush.verticalGradient(listOf(PianoWhiteKeyPressed, Color(0xFFD6DBE5)))
                            else -> Brush.verticalGradient(listOf(Color.White, PianoWhiteKey, Color(0xFFEBEFF5)))
                        }

                        Box(
                            modifier = Modifier
                                .width(whiteKeyWidth)
                                .height(whiteKeyHeight)
                                .clip(RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp))
                                .background(keyBg)
                                .border(1.dp, PianoWhiteKeyBorder, RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp))
                                .padding(bottom = 8.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (keyLabelsMode != KeyLabelsMode.NONE) {
                                val label = if (keyLabelsMode == KeyLabelsMode.NOTE_NAMES) wk.noteLabel else wk.solfegeLabel
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPressed || isPlayback) YamahaRedAccent else Color(0xFF555E6D)
                                )
                            }
                        }
                    }
                }

                // 2. Draw Black Keys Overlay
                for (bk in blackKeys) {
                    val isPressed = activeKeys.contains(bk.midiNote)
                    val isPlayback = playbackKeys.contains(bk.midiNote)

                    // Black key offset from left edge
                    val leftOffset = (whiteKeyWidth * (bk.whiteKeyIndex + 1)) - (blackKeyWidth / 2)

                    val keyBg = when {
                        isPlayback -> Brush.verticalGradient(listOf(YamahaCyanLcd, Color(0xFF005E6B)))
                        isPressed -> Brush.verticalGradient(listOf(YamahaRedAccent, PianoBlackKeyPressed))
                        else -> Brush.verticalGradient(listOf(Color(0xFF32363F), PianoBlackKey, Color(0xFF0D0F12)))
                    }

                    Box(
                        modifier = Modifier
                            .offset(x = leftOffset, y = 0.dp)
                            .width(blackKeyWidth)
                            .height(blackKeyHeight)
                            .zIndex(10f)
                            .shadow(3.dp, RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                            .clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                            .background(keyBg)
                            .border(1.dp, Color(0xFF101216), RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                            .padding(bottom = 6.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        if (keyLabelsMode != KeyLabelsMode.NONE) {
                            val label = if (keyLabelsMode == KeyLabelsMode.NOTE_NAMES) bk.noteLabel else bk.solfegeLabel
                            Text(
                                text = label,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPressed || isPlayback) YamahaGold else Color(0xFFB0B6C3)
                            )
                        }
                    }
                }
            }
        }
    }
}
