package com.example.midi

import com.example.audio.AudioEngine
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
import java.util.Collections

class MidiRecorderPlayer(
    private val audioEngine: AudioEngine,
    private val songRepository: SongRepository,
    private val scope: CoroutineScope
) {
    // Recording state
    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _recordingDurationMs = MutableStateFlow(0L)
    val recordingDurationMs = _recordingDurationMs.asStateFlow()

    private var recordStartTime = 0L
    private val recordedEvents = Collections.synchronizedList(mutableListOf<MidiNoteEvent>())
    private var recordTimerJob: Job? = null

    // Playback state
    private val _isPlayingSong = MutableStateFlow(false)
    val isPlayingSong = _isPlayingSong.asStateFlow()

    private val _playingSong = MutableStateFlow<RecordedSong?>(null)
    val playingSong = _playingSong.asStateFlow()

    private val _playbackTimeMs = MutableStateFlow(0L)
    val playbackTimeMs = _playbackTimeMs.asStateFlow()

    // Notes currently illuminated by playback
    private val _activePlaybackKeys = MutableStateFlow<Set<Int>>(emptySet())
    val activePlaybackKeys = _activePlaybackKeys.asStateFlow()

    private var playbackJob: Job? = null

    fun startRecording() {
        if (_isRecording.value) return
        stopPlayback()
        recordedEvents.clear()
        recordStartTime = System.currentTimeMillis()
        _isRecording.value = true
        _recordingDurationMs.value = 0L

        recordTimerJob?.cancel()
        recordTimerJob = scope.launch(Dispatchers.Default) {
            while (isActive && _isRecording.value) {
                _recordingDurationMs.value = System.currentTimeMillis() - recordStartTime
                delay(100)
            }
        }
    }

    fun recordNoteOn(note: Int, velocity: Float, instrumentId: Int) {
        if (!_isRecording.value) return
        val timestamp = System.currentTimeMillis() - recordStartTime
        val velByte = (velocity * 127).toInt().coerceIn(1, 127)
        recordedEvents.add(MidiNoteEvent(timestamp, MidiEventType.NOTE_ON, note, velByte, instrumentId))
    }

    fun recordNoteOff(note: Int, instrumentId: Int) {
        if (!_isRecording.value) return
        val timestamp = System.currentTimeMillis() - recordStartTime
        recordedEvents.add(MidiNoteEvent(timestamp, MidiEventType.NOTE_OFF, note, 0, instrumentId))
    }

    fun recordDrumHit(sound: DrumSound, velocity: Float) {
        if (!_isRecording.value) return
        val timestamp = System.currentTimeMillis() - recordStartTime
        val velByte = (velocity * 127).toInt().coerceIn(1, 127)
        recordedEvents.add(MidiNoteEvent(timestamp, MidiEventType.DRUM_HIT, sound.midiNote, velByte))
    }

    suspend fun stopAndSaveRecording(
        title: String,
        bpm: Int,
        instrumentId: Int,
        styleName: String?
    ): RecordedSong? {
        if (!_isRecording.value) return null
        _isRecording.value = false
        recordTimerJob?.cancel()
        recordTimerJob = null

        val duration = System.currentTimeMillis() - recordStartTime
        val eventsCopy = ArrayList(recordedEvents)
        if (eventsCopy.isEmpty()) return null

        return songRepository.saveSong(
            title = title,
            bpm = bpm,
            instrumentId = instrumentId,
            styleName = styleName,
            durationMs = duration,
            events = eventsCopy
        )
    }

    fun cancelRecording() {
        _isRecording.value = false
        recordTimerJob?.cancel()
        recordTimerJob = null
        recordedEvents.clear()
        _recordingDurationMs.value = 0L
    }

    fun playSong(song: RecordedSong) {
        stopPlayback()
        _playingSong.value = song
        _isPlayingSong.value = true
        _playbackTimeMs.value = 0L

        playbackJob = scope.launch(Dispatchers.Default) {
            val sorted = song.events.sortedBy { it.timestampMs }
            var eventIndex = 0
            val startTime = System.currentTimeMillis()
            val illuminatedKeys = mutableSetOf<Int>()

            while (isActive && _isPlayingSong.value) {
                val elapsed = System.currentTimeMillis() - startTime
                _playbackTimeMs.value = elapsed

                while (eventIndex < sorted.size && sorted[eventIndex].timestampMs <= elapsed) {
                    val ev = sorted[eventIndex]
                    when (ev.type) {
                        MidiEventType.NOTE_ON -> {
                            val inst = InstrumentType.fromId(ev.instrumentId)
                            val velFloat = ev.velocity.toFloat() / 127f
                            audioEngine.noteOn(ev.note, velFloat, inst)
                            illuminatedKeys.add(ev.note)
                            _activePlaybackKeys.value = HashSet(illuminatedKeys)
                        }
                        MidiEventType.NOTE_OFF -> {
                            audioEngine.noteOff(ev.note)
                            illuminatedKeys.remove(ev.note)
                            _activePlaybackKeys.value = HashSet(illuminatedKeys)
                        }
                        MidiEventType.DRUM_HIT -> {
                            val sound = DrumSound.entries.find { it.midiNote == ev.note } ?: DrumSound.KICK
                            val velFloat = ev.velocity.toFloat() / 127f
                            audioEngine.triggerDrum(sound, velocity = velFloat)
                        }
                    }
                    eventIndex++
                }

                if (eventIndex >= sorted.size && elapsed >= song.durationMs) {
                    break
                }

                delay(10)
            }

            // Cleanup at end of playback
            audioEngine.allNotesOff()
            _activePlaybackKeys.value = emptySet()
            _isPlayingSong.value = false
            _playingSong.value = null
            _playbackTimeMs.value = 0L
        }
    }

    fun stopPlayback() {
        _isPlayingSong.value = false
        playbackJob?.cancel()
        playbackJob = null
        audioEngine.allNotesOff()
        _activePlaybackKeys.value = emptySet()
        _playingSong.value = null
        _playbackTimeMs.value = 0L
    }
}
