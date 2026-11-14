package com.example.rhythm

import com.example.audio.DrumSound

data class TrackPattern(
    val sound: DrumSound,
    val steps: BooleanArray = BooleanArray(16)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TrackPattern
        return sound == other.sound && steps.contentEquals(other.steps)
    }

    override fun hashCode(): Int {
        var result = sound.hashCode()
        result = 31 * result + steps.contentHashCode()
        return result
    }

    fun copyPattern(): TrackPattern = TrackPattern(sound, steps.clone())
}

data class StylePattern(
    val name: String,
    val tracks: List<TrackPattern>
) {
    fun getTrack(sound: DrumSound): TrackPattern? = tracks.find { it.sound == sound }

    fun copyPattern(): StylePattern {
        return StylePattern(name, tracks.map { it.copyPattern() })
    }
}
