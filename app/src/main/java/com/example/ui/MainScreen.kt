package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AppViewTab
import com.example.KeyLabelsMode
import com.example.YamahaViewModel
import com.example.ui.components.BeatSequencerView
import com.example.ui.components.DrumPadsView
import com.example.ui.components.MidiRecorderDialog
import com.example.ui.components.PianoKeyboard
import com.example.ui.components.SongLibraryDialog
import com.example.ui.components.VoiceSelectorRow
import com.example.ui.components.YamahaControls
import com.example.ui.components.YamahaDisplay
import com.example.ui.theme.YamahaCyanLcd
import com.example.ui.theme.YamahaDarkChassis
import com.example.ui.theme.YamahaGold
import com.example.ui.theme.YamahaPanelCard
import com.example.ui.theme.YamahaPanelSurface
import com.example.ui.theme.YamahaRedAccent

@Composable
fun MainScreen(viewModel: YamahaViewModel) {
    val context = LocalContext.current

    val currentTab by viewModel.currentTab.collectAsState()
    val octaveOffset by viewModel.octaveOffset.collectAsState()
    val transposeSemitones by viewModel.transposeSemitones.collectAsState()
    val keyLabelsMode by viewModel.keyLabelsMode.collectAsState()
    val isSustainOn by viewModel.isSustainPedal.collectAsState()
    val pitchBend by viewModel.pitchBendVal.collectAsState()
    val modulation by viewModel.modulationVal.collectAsState()
    val keyZoom by viewModel.keyZoom.collectAsState()
    val activeKeys by viewModel.activeKeys.collectAsState()
    val detectedChord by viewModel.detectedChord.collectAsState()
    val showSaveDialog by viewModel.showSaveRecordingDialog.collectAsState()

    // Audio Engine State
    val vuMeterLevel by viewModel.audioEngine.vuMeterFlow.collectAsState()

    // Rhythm Engine State
    val isPlayingRhythm by viewModel.rhythmEngine.isPlaying.collectAsState()
    val currentStep by viewModel.rhythmEngine.currentStep.collectAsState()
    val currentBeat by viewModel.rhythmEngine.currentBeat.collectAsState()
    val currentBar by viewModel.rhythmEngine.currentBar.collectAsState()
    val currentBpm by viewModel.rhythmEngine.currentBpm.collectAsState()
    val selectedStyle by viewModel.rhythmEngine.selectedStyle.collectAsState()
    val currentSection by viewModel.rhythmEngine.currentSection.collectAsState()
    val isSyncStart by viewModel.rhythmEngine.isSyncStart.collectAsState()
    val isMetronomeOn by viewModel.rhythmEngine.isMetronomeOn.collectAsState()
    val isAccompanimentOn by viewModel.rhythmEngine.isAccompanimentOn.collectAsState()
    val customPattern by viewModel.rhythmEngine.customPattern.collectAsState()

    // MIDI Recorder & Player State
    val isRecording by viewModel.midiRecorderPlayer.isRecording.collectAsState()
    val recordingDurationMs by viewModel.midiRecorderPlayer.recordingDurationMs.collectAsState()
    val isPlayingSong by viewModel.midiRecorderPlayer.isPlayingSong.collectAsState()
    val playingSong by viewModel.midiRecorderPlayer.playingSong.collectAsState()
    val playbackTimeMs by viewModel.midiRecorderPlayer.playbackTimeMs.collectAsState()
    val playbackKeys by viewModel.midiRecorderPlayer.activePlaybackKeys.collectAsState()
    val songsList by viewModel.songRepository.songsFlow.collectAsState()

    var showSongLibraryModal by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = YamahaDarkChassis
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(YamahaDarkChassis)
        ) {
            // 1. Top Workstation Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(YamahaPanelSurface)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(YamahaRedAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Y",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "YAMAHA PIANO",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                }

                // MIDI Record Button
                Button(
                    onClick = {
                        if (isRecording) {
                            viewModel.promptSaveRecording()
                        } else {
                            viewModel.startRecording()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) Color(0xFFC62828) else Color(0xFF262D3D),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("midi_record_button")
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                        contentDescription = "Record MIDI",
                        tint = if (isRecording) Color.White else YamahaRedAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isRecording) "STOP & SAVE" else "REC MIDI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 2. Mode Tabs: KEYBOARD | DRUM PADS | SEQUENCER | SONGS
            TabRow(
                selectedTabIndex = currentTab.ordinal,
                containerColor = YamahaPanelSurface,
                contentColor = YamahaCyanLcd,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
                        color = YamahaCyanLcd,
                        height = 2.5.dp
                    )
                },
                modifier = Modifier.fillMaxWidth().height(38.dp)
            ) {
                TabItem(
                    title = "KEYBOARD",
                    icon = Icons.Default.Piano,
                    isSelected = currentTab == AppViewTab.PIANO,
                    onClick = { viewModel.setTab(AppViewTab.PIANO) },
                    testTag = "tab_keyboard"
                )
                TabItem(
                    title = "DRUM PADS",
                    icon = Icons.Default.Radio,
                    isSelected = currentTab == AppViewTab.DRUM_PADS,
                    onClick = { viewModel.setTab(AppViewTab.DRUM_PADS) },
                    testTag = "tab_drum_pads"
                )
                TabItem(
                    title = "SEQUENCER",
                    icon = Icons.Default.GridOn,
                    isSelected = currentTab == AppViewTab.BEAT_SEQUENCER,
                    onClick = { viewModel.setTab(AppViewTab.BEAT_SEQUENCER) },
                    testTag = "tab_sequencer"
                )
                TabItem(
                    title = "SONGS",
                    icon = Icons.Default.MusicNote,
                    isSelected = currentTab == AppViewTab.SONG_LIBRARY,
                    onClick = { showSongLibraryModal = true },
                    testTag = "tab_songs"
                )
            }

            // 3. LCD Screen & Voice Selector
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                YamahaDisplay(
                    instrument = viewModel.audioEngine.currentInstrument,
                    dualInstrument = viewModel.audioEngine.dualInstrument,
                    isDualOn = viewModel.audioEngine.isDualLayerEnabled,
                    drumKit = viewModel.audioEngine.currentDrumKit,
                    selectedStyle = selectedStyle,
                    styleSection = currentSection,
                    bpm = currentBpm,
                    bar = currentBar,
                    beat = currentBeat,
                    detectedChord = detectedChord,
                    vuMeterLevel = vuMeterLevel,
                    isRecording = isRecording,
                    recordingDurationMs = recordingDurationMs,
                    isPlayingRhythm = isPlayingRhythm
                )

                VoiceSelectorRow(
                    currentInstrument = viewModel.audioEngine.currentInstrument,
                    dualInstrument = viewModel.audioEngine.dualInstrument,
                    isDualOn = viewModel.audioEngine.isDualLayerEnabled,
                    onSelectInstrument = { viewModel.setInstrument(it) },
                    onSelectDualInstrument = { viewModel.setDualInstrument(it) }
                )
            }

            // 4. Center Workstation Panel according to selected Tab
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                when (currentTab) {
                    AppViewTab.PIANO -> {
                        YamahaControls(
                            octaveOffset = octaveOffset,
                            transposeSemitones = transposeSemitones,
                            keyLabelsMode = keyLabelsMode,
                            isSustainOn = isSustainOn,
                            isDualOn = viewModel.audioEngine.isDualLayerEnabled,
                            keyZoom = keyZoom,
                            masterVolume = viewModel.audioEngine.masterVolume,
                            reverbLevel = viewModel.audioEngine.reverbLevel,
                            pitchBend = pitchBend,
                            modulation = modulation,
                            onOctaveChange = { viewModel.shiftOctave(it) },
                            onTransposeChange = { viewModel.shiftTranspose(it) },
                            onToggleLabels = {
                                val next = when (keyLabelsMode) {
                                    KeyLabelsMode.NOTE_NAMES -> KeyLabelsMode.SOLFEGE
                                    KeyLabelsMode.SOLFEGE -> KeyLabelsMode.NONE
                                    KeyLabelsMode.NONE -> KeyLabelsMode.NOTE_NAMES
                                }
                                viewModel.setKeyLabelsMode(next)
                            },
                            onToggleSustain = { viewModel.toggleSustainPedal() },
                            onToggleDual = { viewModel.toggleDualLayer() },
                            onZoomChange = { viewModel.setKeyZoom(it) },
                            onVolumeChange = { viewModel.setMasterVolume(it) },
                            onReverbChange = { viewModel.setReverbLevel(it) },
                            onPitchBendChange = { viewModel.setPitchBend(it) },
                            onModulationChange = { viewModel.setModulation(it) }
                        )
                    }

                    AppViewTab.DRUM_PADS -> {
                        DrumPadsView(
                            currentKit = viewModel.audioEngine.currentDrumKit,
                            isPlayingBeat = isPlayingRhythm,
                            isSyncStart = isSyncStart,
                            bpm = currentBpm,
                            onSelectKit = { viewModel.setDrumKit(it) },
                            onTriggerDrum = { sound, vel -> viewModel.onDrumPadTrigger(sound, vel) },
                            onToggleBeat = { viewModel.rhythmEngine.togglePlayPause() },
                            onSyncStart = { viewModel.rhythmEngine.triggerSyncStart() },
                            onFillIn = { viewModel.rhythmEngine.triggerFillIn() },
                            onTapTempo = { viewModel.rhythmEngine.tapTempo() }
                        )
                    }

                    AppViewTab.BEAT_SEQUENCER -> {
                        BeatSequencerView(
                            pattern = customPattern,
                            currentStep = currentStep,
                            isPlaying = isPlayingRhythm,
                            bpm = currentBpm,
                            selectedStyle = selectedStyle,
                            currentSection = currentSection,
                            isMetronomeOn = isMetronomeOn,
                            isAccompanimentOn = isAccompanimentOn,
                            onToggleStep = { sound, s -> viewModel.rhythmEngine.toggleStep(sound, s) },
                            onTogglePlay = { viewModel.rhythmEngine.togglePlayPause() },
                            onBpmChange = { viewModel.rhythmEngine.setBpm(it) },
                            onSelectStyle = { viewModel.rhythmEngine.selectStyle(it) },
                            onSelectSection = { viewModel.rhythmEngine.setSection(it) },
                            onToggleMetronome = { viewModel.rhythmEngine.toggleMetronome() },
                            onToggleAccompaniment = { viewModel.rhythmEngine.toggleAccompaniment() },
                            onClearPattern = { viewModel.rhythmEngine.clearPattern() }
                        )
                    }

                    AppViewTab.SONG_LIBRARY -> {
                        // Handled by modal dialog
                    }
                }
            }

            // 5. Yamaha Piano Keyboard (Anchor at bottom)
            PianoKeyboard(
                octaveOffset = octaveOffset,
                keyLabelsMode = keyLabelsMode,
                keyZoom = keyZoom,
                activeKeys = activeKeys,
                playbackKeys = playbackKeys,
                onKeyDown = { note, vel -> viewModel.onKeyTouchDown(note, vel) },
                onKeyUp = { note -> viewModel.onKeyTouchUp(note) }
            )
        }
    }

    // Modal: Save Recording
    if (showSaveDialog) {
        MidiRecorderDialog(
            durationMs = recordingDurationMs,
            bpm = currentBpm,
            instrumentName = viewModel.audioEngine.currentInstrument.displayName,
            onSave = { title -> viewModel.finishAndSaveRecording(title) },
            onDiscard = {
                viewModel.midiRecorderPlayer.cancelRecording()
                viewModel.dismissSaveRecordingDialog()
            }
        )
    }

    // Modal: Song Library & Playback
    if (showSongLibraryModal) {
        SongLibraryDialog(
            songs = songsList,
            playingSong = playingSong,
            isPlayingSong = isPlayingSong,
            playbackTimeMs = playbackTimeMs,
            onPlaySong = { viewModel.midiRecorderPlayer.playSong(it) },
            onStopSong = { viewModel.midiRecorderPlayer.stopPlayback() },
            onShareMidi = { ctx, song -> viewModel.shareMidiFile(ctx, song) },
            onDeleteSong = { viewModel.deleteSong(it) },
            onDismiss = { showSongLibraryModal = false }
        )
    }
}

@Composable
private fun TabItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Tab(
        selected = isSelected,
        onClick = onClick,
        modifier = Modifier.testTag(testTag),
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(14.dp),
                    tint = if (isSelected) YamahaCyanLcd else Color(0xFF8E98AA)
                )
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) YamahaCyanLcd else Color(0xFF8E98AA)
                )
            }
        }
    )
}
