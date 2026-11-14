package com.example.ui.components

import android.content.Context
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.midi.RecordedSong
import com.example.ui.theme.YamahaBorder
import com.example.ui.theme.YamahaControlSurface
import com.example.ui.theme.YamahaCyanLcd
import com.example.ui.theme.YamahaGold
import com.example.ui.theme.YamahaPanelCard
import com.example.ui.theme.YamahaPanelSurface
import com.example.ui.theme.YamahaRedAccent

@Composable
fun SongLibraryDialog(
    songs: List<RecordedSong>,
    playingSong: RecordedSong?,
    isPlayingSong: Boolean,
    playbackTimeMs: Long,
    onPlaySong: (RecordedSong) -> Unit,
    onStopSong: () -> Unit,
    onShareMidi: (Context, RecordedSong) -> Unit,
    onDeleteSong: (RecordedSong) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .testTag("song_library_dialog"),
            shape = RoundedCornerShape(12.dp),
            color = YamahaPanelSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "MIDI Library",
                            tint = YamahaCyanLcd,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MIDI RECORDINGS & DEMOS",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_song_library_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFFB0B6C3)
                        )
                    }
                }

                Text(
                    text = "Play back recorded songs with realistic Yamaha piano sound and animated keys, or export standard .mid files.",
                    color = Color(0xFF8E98AA),
                    fontSize = 11.sp
                )

                // Songs List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(songs, key = { it.id }) { song ->
                        val isThisPlaying = isPlayingSong && (playingSong?.id == song.id)
                        val progress = if (isThisPlaying && song.durationMs > 0) {
                            (playbackTimeMs.toFloat() / song.durationMs.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        SongItemCard(
                            song = song,
                            isPlaying = isThisPlaying,
                            playbackProgress = progress,
                            onPlayPause = {
                                if (isThisPlaying) onStopSong() else onPlaySong(song)
                            },
                            onShare = { onShareMidi(context, song) },
                            onDelete = { onDeleteSong(song) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SongItemCard(
    song: RecordedSong,
    isPlaying: Boolean,
    playbackProgress: Float,
    onPlayPause: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val isDemo = song.id.startsWith("demo_")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isPlaying) YamahaCyanLcd else YamahaBorder,
                RoundedCornerShape(8.dp)
            )
            .testTag("song_card_${song.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) Color(0xFF142430) else YamahaPanelCard
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isDemo) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(YamahaGold.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "DEMO",
                                    color = YamahaGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = song.title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    val durSec = (song.durationMs / 1000) % 60
                    val durMin = (song.durationMs / 1000) / 60
                    Text(
                        text = "${song.instrument.displayName} • ${song.bpm} BPM • ${song.noteCount} notes • ${String.format("%d:%02d", durMin, durSec)}",
                        color = Color(0xFF8E98AA),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Actions: Play/Pause, Share MIDI, Delete
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play/Pause
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                if (isPlaying) YamahaRedAccent else YamahaCyanLcd,
                                RoundedCornerShape(6.dp)
                            )
                            .testTag("play_song_${song.id}")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play Song",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Share MIDI
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier
                            .size(34.dp)
                            .background(YamahaControlSurface, RoundedCornerShape(6.dp))
                            .testTag("share_midi_${song.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share MIDI File",
                            tint = YamahaGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Delete (for user recordings)
                    if (!isDemo) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(34.dp)
                                .background(YamahaControlSurface, RoundedCornerShape(6.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Song",
                                tint = Color(0xFFEF5350),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Playback progress bar
            if (isPlaying) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { playbackProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp)),
                    color = YamahaCyanLcd,
                    trackColor = Color(0xFF1E2836)
                )
            }
        }
    }
}
