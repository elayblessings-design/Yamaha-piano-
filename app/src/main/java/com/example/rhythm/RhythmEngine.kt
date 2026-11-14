package com.example.rhythm

import com.example.audio.AudioEngine
import com.example.audio.DrumKit
import com.example.audio.DrumSound
import com.example.audio.InstrumentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class RhythmEngine(
    private val audioEngine: AudioEngine,
    private val scope: CoroutineScope
) {
    private var rhythmJob: Job? = null

    // Rhythm state flows
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentStep = MutableStateFlow(0) // 0 to 15
    val currentStep = _currentStep.asStateFlow()

    private val _currentBeat = MutableStateFlow(1) // 1 to 4
    val currentBeat = _currentBeat.asStateFlow()

    private val _currentBar = MutableStateFlow(1)
    val currentBar = _currentBar.asStateFlow()

    private val _currentBpm = MutableStateFlow(120)
    val currentBpm = _currentBpm.asStateFlow()

    private val _selectedStyle = MutableStateFlow(StyleLibrary.STYLES[0])
    val selectedStyle = _selectedStyle.asStateFlow()

    private val _currentSection = MutableStateFlow(StyleSection.MAIN_A)
    val currentSection = _currentSection.asStateFlow()

    private val _isSyncStart = MutableStateFlow(false)
    val isSyncStart = _isSyncStart.asStateFlow()

    private val _isMetronomeOn = MutableStateFlow(false)
    val isMetronomeOn = _isMetronomeOn.asStateFlow()

    private val _isAccompanimentOn = MutableStateFlow(false)
    val isAccompanimentOn = _isAccompanimentOn.asStateFlow()

    // Editable 16-step grid pattern for live customization
    private val _customPattern = MutableStateFlow(StyleLibrary.STYLES[0].mainA.copyPattern())
    val customPattern = _customPattern.asStateFlow()

    // Auto-accompaniment state
    private var lastAccRootNote: Int = 48 // C3
    private var lastAccChordNotes: List<Int> = listOf(48, 52, 55) // C major

    // Tap tempo timestamps
    private val tapTimestamps = mutableListOf<Long>()

    init {
        val defaultStyle = StyleLibrary.STYLES[0]
        _selectedStyle.value = defaultStyle
        _currentBpm.value = defaultStyle.defaultBpm
        _customPattern.value = defaultStyle.mainA.copyPattern()
    }

    fun selectStyle(style: ArrangerStyle) {
        _selectedStyle.value = style
        _currentBpm.value = style.defaultBpm
        audioEngine.currentDrumKit = style.recommendedKit
        _currentSection.value = StyleSection.MAIN_A
        _customPattern.value = style.mainA.copyPattern()
    }

    fun selectKit(kit: DrumKit) {
        audioEngine.currentDrumKit = kit
    }

    fun setBpm(bpm: Int) {
        _currentBpm.value = bpm.coerceIn(40, 260)
    }

    fun tapTempo() {
        val now = System.currentTimeMillis()
        tapTimestamps.add(now)
        while (tapTimestamps.size > 4) {
            tapTimestamps.removeAt(0)
        }
        if (tapTimestamps.size >= 2) {
            val intervals = mutableListOf<Long>()
            for (i in 1 until tapTimestamps.size) {
                val diff = tapTimestamps[i] - tapTimestamps[i - 1]
                if (diff in 200..2000) {
                    intervals.add(diff)
                }
            }
            if (intervals.isNotEmpty()) {
                val avgMs = intervals.average()
                val calculatedBpm = (60000.0 / avgMs).toInt().coerceIn(40, 240)
                setBpm(calculatedBpm)
            }
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            stop()
        } else {
            start()
        }
    }

    fun start() {
        if (_isPlaying.value) return
        _isSyncStart.value = false
        _isPlaying.value = true
        _currentStep.value = 0
        _currentBeat.value = 1
        _currentBar.value = 1

        rhythmJob?.cancel()
        rhythmJob = scope.launch(Dispatchers.Default) {
            var step = 0
            var bar = 1

            while (isActive) {
                val bpm = _currentBpm.value
                val stepDurationMs = (60000L / bpm) / 4L // 16th note

                _currentStep.value = step
                _currentBeat.value = (step / 4) + 1
                _currentBar.value = bar

                // Play metronome if enabled
                if (_isMetronomeOn.value && step % 4 == 0) {
                    val isFirstBeat = (step == 0)
                    audioEngine.triggerDrum(
                        if (isFirstBeat) DrumSound.COWBELL else DrumSound.RIMSHOT,
                        velocity = if (isFirstBeat) 0.8f else 0.45f
                    )
                }

                // Play drum pattern steps
                val currentSec = _currentSection.value
                val activePattern = if (currentSec == StyleSection.FILL_IN) {
                    _selectedStyle.value.fillIn
                } else if (currentSec == StyleSection.MAIN_B) {
                    _selectedStyle.value.mainB
                } else {
                    _customPattern.value
                }

                for (track in activePattern.tracks) {
                    if (track.steps[step]) {
                        audioEngine.triggerDrum(track.sound)
                    }
                }

                // Auto-accompaniment bass & chords on lower keys
                if (_isAccompanimentOn.value) {
                    playAccompanimentStep(step)
                }

                delay(stepDurationMs)

                step = (step + 1) % 16
                if (step == 0) {
                    bar++
                    // If we just finished a Fill-In, revert back to Main A or B
                    if (_currentSection.value == StyleSection.FILL_IN) {
                        _currentSection.value = StyleSection.MAIN_B
                    }
                }
            }
        }
    }

    fun stop() {
        _isPlaying.value = false
        _isSyncStart.value = false
        rhythmJob?.cancel()
        rhythmJob = null
        _currentStep.value = 0
        _currentBeat.value = 1
    }

    fun triggerSyncStart() {
        if (_isPlaying.value) {
            stop()
        } else {
            _isSyncStart.value = !_isSyncStart.value
        }
    }

    fun onPianoKeyPressed(midiNote: Int, chordNotes: List<Int> = emptyList()) {
        if (_isSyncStart.value && !_isPlaying.value) {
            _isSyncStart.value = false
            start()
        }

        // Update chord for auto accompaniment
        if (midiNote < 60) { // Left hand accompaniment range
            lastAccRootNote = midiNote
            lastAccChordNotes = if (chordNotes.isNotEmpty()) chordNotes else listOf(midiNote, midiNote + 4, midiNote + 7)
        }
    }

    private fun playAccompanimentStep(step: Int) {
        val root = lastAccRootNote
        // Bassline pattern: root on 0, fifth on 6, octave on 10
        when (step) {
            0 -> audioEngine.noteOn(root - 12, velocity = 0.75f, overrideInstrument = InstrumentType.SLAP_BASS)
            2 -> audioEngine.noteOff(root - 12)
            6 -> audioEngine.noteOn(root - 5, velocity = 0.65f, overrideInstrument = InstrumentType.SLAP_BASS)
            8 -> audioEngine.noteOff(root - 5)
            10 -> audioEngine.noteOn(root, velocity = 0.60f, overrideInstrument = InstrumentType.SLAP_BASS)
            12 -> audioEngine.noteOff(root)
        }

        // Chord comping: syncopated chord stabs
        if (step == 4 || step == 10 || step == 14) {
            for (note in lastAccChordNotes) {
                audioEngine.noteOn(note, velocity = 0.50f, overrideInstrument = InstrumentType.DX7_EPIANO)
            }
        } else if (step == 6 || step == 12 || step == 15) {
            for (note in lastAccChordNotes) {
                audioEngine.noteOff(note)
            }
        }
    }

    fun triggerFillIn() {
        if (_isPlaying.value) {
            _currentSection.value = StyleSection.FILL_IN
        } else {
            start()
        }
    }

    fun setSection(section: StyleSection) {
        _currentSection.value = section
        if (section == StyleSection.MAIN_A) {
            _customPattern.value = _selectedStyle.value.mainA.copyPattern()
        } else if (section == StyleSection.MAIN_B) {
            _customPattern.value = _selectedStyle.value.mainB.copyPattern()
        }
    }

    fun toggleMetronome() {
        _isMetronomeOn.value = !_isMetronomeOn.value
    }

    fun toggleAccompaniment() {
        _isAccompanimentOn.value = !_isAccompanimentOn.value
    }

    fun toggleStep(sound: DrumSound, stepIndex: Int) {
        val current = _customPattern.value
        val track = current.getTrack(sound) ?: return
        if (stepIndex in 0..15) {
            track.steps[stepIndex] = !track.steps[stepIndex]
            _customPattern.value = current.copyPattern()
        }
    }

    fun clearPattern() {
        val current = _customPattern.value
        for (t in current.tracks) {
            t.steps.fill(false)
        }
        _customPattern.value = current.copyPattern()
    }
}
