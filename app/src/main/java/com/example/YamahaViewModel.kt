package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioEngine
import com.example.audio.DrumKit
import com.example.audio.DrumSound
import com.example.audio.InstrumentType
import com.example.midi.ChordDetector
import com.example.midi.MidiFileExporter
import com.example.midi.MidiRecorderPlayer
import com.example.midi.RecordedSong
import com.example.midi.SongRepository
import com.example.rhythm.ArrangerStyle
import com.example.rhythm.RhythmEngine
import com.example.rhythm.StyleSection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppViewTab {
    PIANO,
    DRUM_PADS,
    BEAT_SEQUENCER,
    SONG_LIBRARY
}

enum class KeyLabelsMode {
    NOTE_NAMES,
    SOLFEGE,
    NONE
}

class YamahaViewModel(application: Application) : AndroidViewModel(application) {
    val audioEngine = AudioEngine()
    val songRepository = SongRepository(application)
    val rhythmEngine = RhythmEngine(audioEngine, viewModelScope)
    val midiRecorderPlayer = MidiRecorderPlayer(audioEngine, songRepository, viewModelScope)

    // UI Tab
    private val _currentTab = MutableStateFlow(AppViewTab.PIANO)
    val currentTab = _currentTab.asStateFlow()

    // Keyboard state
    private val _octaveOffset = MutableStateFlow(0) // -3 to +3 octaves
    val octaveOffset = _octaveOffset.asStateFlow()

    private val _transposeSemitones = MutableStateFlow(0) // -12 to +12
    val transposeSemitones = _transposeSemitones.asStateFlow()

    private val _keyLabelsMode = MutableStateFlow(KeyLabelsMode.NOTE_NAMES)
    val keyLabelsMode = _keyLabelsMode.asStateFlow()

    private val _isSustainPedal = MutableStateFlow(false)
    val isSustainPedal = _isSustainPedal.asStateFlow()

    private val _pitchBendVal = MutableStateFlow(0f) // -1.0 to +1.0
    val pitchBendVal = _pitchBendVal.asStateFlow()

    private val _modulationVal = MutableStateFlow(0f) // 0.0 to 1.0
    val modulationVal = _modulationVal.asStateFlow()

    private val _activeKeys = MutableStateFlow<Set<Int>>(emptySet())
    val activeKeys = _activeKeys.asStateFlow()

    private val _detectedChord = MutableStateFlow("--")
    val detectedChord = _detectedChord.asStateFlow()

    private val _keyZoom = MutableStateFlow(1.0f) // 0.8f to 1.5f
    val keyZoom = _keyZoom.asStateFlow()

    // Dialog state
    private val _showSaveRecordingDialog = MutableStateFlow(false)
    val showSaveRecordingDialog = _showSaveRecordingDialog.asStateFlow()

    // Haptics
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        audioEngine.start()
        viewModelScope.launch {
            songRepository.loadSongs()
        }
    }

    fun setTab(tab: AppViewTab) {
        _currentTab.value = tab
    }

    fun setKeyLabelsMode(mode: KeyLabelsMode) {
        _keyLabelsMode.value = mode
    }

    fun shiftOctave(delta: Int) {
        _octaveOffset.value = (_octaveOffset.value + delta).coerceIn(-2, 2)
    }

    fun shiftTranspose(delta: Int) {
        _transposeSemitones.value = (_transposeSemitones.value + delta).coerceIn(-12, 12)
    }

    fun setKeyZoom(zoom: Float) {
        _keyZoom.value = zoom.coerceIn(0.75f, 1.4f)
    }

    fun setPitchBend(bend: Float) {
        _pitchBendVal.value = bend
        audioEngine.setPitchBendSemitones(bend * 2.0f) // +/- 2 semitones standard pitch bend
    }

    fun setModulation(mod: Float) {
        _modulationVal.value = mod
        audioEngine.setModulation(mod)
    }

    fun toggleSustainPedal() {
        val newState = !_isSustainPedal.value
        _isSustainPedal.value = newState
        audioEngine.setSustainPedal(newState)
        triggerHaptic()
    }

    fun onKeyTouchDown(midiNote: Int, velocity: Float = 0.85f) {
        val transposedNote = (midiNote + _transposeSemitones.value).coerceIn(21, 108)
        audioEngine.noteOn(transposedNote, velocity)
        midiRecorderPlayer.recordNoteOn(transposedNote, velocity, audioEngine.currentInstrument.id)

        val newSet = HashSet(_activeKeys.value).apply { add(transposedNote) }
        _activeKeys.value = newSet
        _detectedChord.value = ChordDetector.detectChord(newSet)

        rhythmEngine.onPianoKeyPressed(transposedNote, newSet.toList())
        triggerHaptic()
    }

    fun onKeyTouchUp(midiNote: Int) {
        val transposedNote = (midiNote + _transposeSemitones.value).coerceIn(21, 108)
        audioEngine.noteOff(transposedNote)
        midiRecorderPlayer.recordNoteOff(transposedNote, audioEngine.currentInstrument.id)

        val newSet = HashSet(_activeKeys.value).apply { remove(transposedNote) }
        _activeKeys.value = newSet
        _detectedChord.value = if (newSet.isEmpty()) "--" else ChordDetector.detectChord(newSet)
    }

    fun onDrumPadTrigger(sound: DrumSound, velocity: Float = 0.9f) {
        audioEngine.triggerDrum(sound, velocity = velocity)
        midiRecorderPlayer.recordDrumHit(sound, velocity)
        triggerHaptic()
    }

    fun setInstrument(instrument: InstrumentType) {
        audioEngine.currentInstrument = instrument
    }

    fun setDualInstrument(instrument: InstrumentType?) {
        audioEngine.dualInstrument = instrument
    }

    fun toggleDualLayer() {
        audioEngine.isDualLayerEnabled = !audioEngine.isDualLayerEnabled
    }

    fun setDrumKit(kit: DrumKit) {
        audioEngine.currentDrumKit = kit
    }

    fun setMasterVolume(vol: Float) {
        audioEngine.masterVolume = vol.coerceIn(0f, 1f)
    }

    fun setReverbLevel(rev: Float) {
        audioEngine.reverbLevel = rev.coerceIn(0f, 1f)
    }

    fun startRecording() {
        midiRecorderPlayer.startRecording()
        triggerHaptic()
    }

    fun promptSaveRecording() {
        _showSaveRecordingDialog.value = true
    }

    fun dismissSaveRecordingDialog() {
        _showSaveRecordingDialog.value = false
    }

    fun finishAndSaveRecording(title: String) {
        viewModelScope.launch {
            midiRecorderPlayer.stopAndSaveRecording(
                title = title,
                bpm = rhythmEngine.currentBpm.value,
                instrumentId = audioEngine.currentInstrument.id,
                styleName = rhythmEngine.selectedStyle.value.name
            )
            _showSaveRecordingDialog.value = false
            triggerHaptic()
        }
    }

    fun shareMidiFile(context: Context, song: RecordedSong) {
        viewModelScope.launch {
            try {
                val file = MidiFileExporter.exportToMidiFile(context, song)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "audio/midi"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "${song.title}.mid")
                    putExtra(Intent.EXTRA_TEXT, "Exported MIDI from Yamaha Piano: ${song.title}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share MIDI File"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteSong(song: RecordedSong) {
        viewModelScope.launch {
            songRepository.deleteSong(song.id)
        }
    }

    private fun triggerHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(12)
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.stop()
        rhythmEngine.stop()
        midiRecorderPlayer.stopPlayback()
    }
}
