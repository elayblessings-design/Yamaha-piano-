package com.example.midi

import com.example.audio.InstrumentType

enum class MidiEventType {
    NOTE_ON,
    NOTE_OFF,
    DRUM_HIT
}

data class MidiNoteEvent(
    val timestampMs: Long,
    val type: MidiEventType,
    val note: Int,
    val velocity: Int,
    val instrumentId: Int = 1
)

data class RecordedSong(
    val id: String,
    val title: String,
    val bpm: Int,
    val instrumentId: Int,
    val styleName: String? = null,
    val durationMs: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val events: List<MidiNoteEvent>
) {
    val instrument: InstrumentType get() = InstrumentType.fromId(instrumentId)
    val noteCount: Int get() = events.count { it.type == MidiEventType.NOTE_ON }
}

object ChordDetector {
    private val NOTE_NAMES = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    fun getNoteName(midiNote: Int): String {
        val octave = (midiNote / 12) - 1
        val note = NOTE_NAMES[midiNote % 12]
        return "$note$octave"
    }

    fun getPitchClass(midiNote: Int): Int = midiNote % 12

    fun detectChord(activeNotes: Collection<Int>): String {
        if (activeNotes.isEmpty()) return "--"
        val pitchClasses = activeNotes.map { it % 12 }.distinct().sorted()

        if (pitchClasses.size == 1) {
            return NOTE_NAMES[pitchClasses[0]]
        }

        // Check for 2-note power chord / dyad
        if (pitchClasses.size == 2) {
            val root = pitchClasses[0]
            val interval = (pitchClasses[1] - root + 12) % 12
            return when (interval) {
                7 -> "${NOTE_NAMES[root]}5"
                4 -> "${NOTE_NAMES[root]} (3rd)"
                3 -> "${NOTE_NAMES[root]}m (3rd)"
                else -> "${NOTE_NAMES[root]}/${NOTE_NAMES[pitchClasses[1]]}"
            }
        }

        // Triads & 7th chords detection by checking permutations of roots
        for (root in pitchClasses) {
            val intervals = pitchClasses.map { (it - root + 12) % 12 }.toSet()
            val rootName = NOTE_NAMES[root]

            // Major Triad (0, 4, 7)
            if (intervals.containsAll(listOf(0, 4, 7))) {
                if (intervals.contains(11)) return "${rootName}Maj7"
                if (intervals.contains(10)) return "${rootName}7"
                if (intervals.contains(9)) return "${rootName}6"
                if (intervals.contains(2)) return "${rootName}add9"
                return rootName
            }
            // Minor Triad (0, 3, 7)
            if (intervals.containsAll(listOf(0, 3, 7))) {
                if (intervals.contains(10)) return "${rootName}m7"
                if (intervals.contains(11)) return "${rootName}mMaj7"
                if (intervals.contains(9)) return "${rootName}m6"
                return "${rootName}m"
            }
            // Diminished (0, 3, 6)
            if (intervals.containsAll(listOf(0, 3, 6))) {
                if (intervals.contains(9)) return "${rootName}dim7"
                if (intervals.contains(10)) return "${rootName}m7b5"
                return "${rootName}dim"
            }
            // Augmented (0, 4, 8)
            if (intervals.containsAll(listOf(0, 4, 8))) {
                return "${rootName}aug"
            }
            // Sus4 (0, 5, 7)
            if (intervals.containsAll(listOf(0, 5, 7))) {
                return "${rootName}sus4"
            }
            // Sus2 (0, 2, 7)
            if (intervals.containsAll(listOf(0, 2, 7))) {
                return "${rootName}sus2"
            }
        }

        return NOTE_NAMES[pitchClasses[0]] + " poly"
    }
}
