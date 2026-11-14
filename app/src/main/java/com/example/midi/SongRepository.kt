package com.example.midi

import android.content.Context
import com.example.audio.InstrumentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class SongRepository(private val context: Context) {
    private val songsFile = File(context.filesDir, "yamaha_recorded_songs.json")

    private val _songsFlow = MutableStateFlow<List<RecordedSong>>(emptyList())
    val songsFlow = _songsFlow.asStateFlow()

    suspend fun loadSongs() = withContext(Dispatchers.IO) {
        val userSongs = mutableListOf<RecordedSong>()
        if (songsFile.exists()) {
            try {
                val jsonStr = songsFile.readText()
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    userSongs.add(parseSongJson(obj))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Combine with built-in Yamaha demo songs
        val allSongs = getDemoSongs() + userSongs
        _songsFlow.value = allSongs
    }

    suspend fun saveSong(
        title: String,
        bpm: Int,
        instrumentId: Int,
        styleName: String?,
        durationMs: Long,
        events: List<MidiNoteEvent>
    ): RecordedSong = withContext(Dispatchers.IO) {
        val newSong = RecordedSong(
            id = UUID.randomUUID().toString(),
            title = title.ifBlank { "Yamaha Recording ${System.currentTimeMillis() % 10000}" },
            bpm = bpm,
            instrumentId = instrumentId,
            styleName = styleName,
            durationMs = durationMs,
            createdAt = System.currentTimeMillis(),
            events = events
        )

        val currentList = _songsFlow.value.toMutableList()
        currentList.add(newSong)
        _songsFlow.value = currentList

        persistUserSongs(currentList.filter { !it.id.startsWith("demo_") })
        newSong
    }

    suspend fun deleteSong(songId: String) = withContext(Dispatchers.IO) {
        val updated = _songsFlow.value.filter { it.id != songId }
        _songsFlow.value = updated
        persistUserSongs(updated.filter { !it.id.startsWith("demo_") })
    }

    private fun persistUserSongs(userSongs: List<RecordedSong>) {
        try {
            val array = JSONArray()
            for (song in userSongs) {
                val obj = JSONObject().apply {
                    put("id", song.id)
                    put("title", song.title)
                    put("bpm", song.bpm)
                    put("instrumentId", song.instrumentId)
                    put("styleName", song.styleName ?: "")
                    put("durationMs", song.durationMs)
                    put("createdAt", song.createdAt)

                    val evArr = JSONArray()
                    for (ev in song.events) {
                        val evObj = JSONObject().apply {
                            put("t", ev.timestampMs)
                            put("type", ev.type.name)
                            put("n", ev.note)
                            put("v", ev.velocity)
                            put("inst", ev.instrumentId)
                        }
                        evArr.put(evObj)
                    }
                    put("events", evArr)
                }
                array.put(obj)
            }
            songsFile.writeText(array.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseSongJson(obj: JSONObject): RecordedSong {
        val evArr = obj.getJSONArray("events")
        val events = mutableListOf<MidiNoteEvent>()
        for (j in 0 until evArr.length()) {
            val evObj = evArr.getJSONObject(j)
            events.add(
                MidiNoteEvent(
                    timestampMs = evObj.getLong("t"),
                    type = MidiEventType.valueOf(evObj.getString("type")),
                    note = evObj.getInt("n"),
                    velocity = evObj.getInt("v"),
                    instrumentId = evObj.optInt("inst", 1)
                )
            )
        }

        return RecordedSong(
            id = obj.getString("id"),
            title = obj.getString("title"),
            bpm = obj.getInt("bpm"),
            instrumentId = obj.getInt("instrumentId"),
            styleName = obj.optString("styleName").takeIf { it.isNotBlank() },
            durationMs = obj.getLong("durationMs"),
            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
            events = events
        )
    }

    private fun getDemoSongs(): List<RecordedSong> {
        return listOf(
            // Demo 1: Für Elise (Yamaha CFX Grand Piano)
            createFurEliseDemo(),
            // Demo 2: Trap Beat & Melodic Piano Freestyle (Song beat)
            createTrapMelodyDemo(),
            // Demo 3: DX7 80s Ballad Groove
            createDX7BalladDemo()
        )
    }

    private fun createFurEliseDemo(): RecordedSong {
        val events = mutableListOf<MidiNoteEvent>()
        // E5, D#5, E5, D#5, E5, B4, D5, C5, A4
        val melody = listOf(
            76 to 250L, 75 to 250L, 76 to 250L, 75 to 250L, 76 to 250L,
            71 to 250L, 74 to 250L, 72 to 250L, 69 to 500L,
            // Accompaniment arpeggio: A2, E3, A3, C4, E4
            57 to 250L, 60 to 250L, 64 to 250L, 69 to 500L,
            76 to 250L, 75 to 250L, 76 to 250L, 75 to 250L, 76 to 250L,
            71 to 250L, 74 to 250L, 72 to 250L, 69 to 600L
        )

        var time = 100L
        for ((note, dur) in melody) {
            events.add(MidiNoteEvent(time, MidiEventType.NOTE_ON, note, 90, InstrumentType.GRAND_PIANO.id))
            events.add(MidiNoteEvent(time + dur - 30L, MidiEventType.NOTE_OFF, note, 0, InstrumentType.GRAND_PIANO.id))
            time += dur
        }

        return RecordedSong(
            id = "demo_fur_elise",
            title = "Yamaha CFX: Beethoven Für Elise",
            bpm = 130,
            instrumentId = InstrumentType.GRAND_PIANO.id,
            styleName = "Acoustic Classical",
            durationMs = time + 200L,
            events = events
        )
    }

    private fun createTrapMelodyDemo(): RecordedSong {
        val events = mutableListOf<MidiNoteEvent>()
        // Dark Trap Piano progression (A minor: A4, C5, E5, G4, F4) with bass & drum hits
        val notes = listOf(
            69 to 300L, 72 to 300L, 76 to 600L,
            71 to 300L, 74 to 300L, 77 to 600L,
            68 to 300L, 72 to 300L, 76 to 600L,
            65 to 300L, 69 to 300L, 72 to 800L
        )

        var time = 50L
        for ((note, dur) in notes) {
            events.add(MidiNoteEvent(time, MidiEventType.NOTE_ON, note, 96, InstrumentType.GRAND_PIANO.id))
            // Bass octave
            events.add(MidiNoteEvent(time, MidiEventType.NOTE_ON, note - 24, 105, InstrumentType.GRAND_PIANO.id))
            // Trap kick & snare hits in MIDI
            events.add(MidiNoteEvent(time, MidiEventType.DRUM_HIT, 36, 110)) // Kick
            events.add(MidiNoteEvent(time + dur / 2, MidiEventType.DRUM_HIT, 38, 100)) // Snare
            events.add(MidiNoteEvent(time + dur - 20L, MidiEventType.NOTE_OFF, note, 0, InstrumentType.GRAND_PIANO.id))
            events.add(MidiNoteEvent(time + dur - 20L, MidiEventType.NOTE_OFF, note - 24, 0, InstrumentType.GRAND_PIANO.id))
            time += dur
        }

        return RecordedSong(
            id = "demo_trap_beat",
            title = "Trap Piano Beat on Song",
            bpm = 140,
            instrumentId = InstrumentType.GRAND_PIANO.id,
            styleName = "Trap & Hip-Hop Beat",
            durationMs = time + 200L,
            events = events
        )
    }

    private fun createDX7BalladDemo(): RecordedSong {
        val events = mutableListOf<MidiNoteEvent>()
        // 80s chords: Dmaj9, F#m7, Bm9, Gmaj7
        val chordTimes = listOf(
            listOf(62, 66, 69, 73) to 1200L, // Dmaj9
            listOf(61, 66, 69, 73) to 1200L, // F#m7
            listOf(59, 62, 66, 71) to 1200L, // Bm7
            listOf(55, 59, 62, 67) to 1400L  // Gmaj7
        )

        var time = 50L
        for ((chord, dur) in chordTimes) {
            for (n in chord) {
                events.add(MidiNoteEvent(time, MidiEventType.NOTE_ON, n, 85, InstrumentType.DX7_EPIANO.id))
            }
            // Release
            for (n in chord) {
                events.add(MidiNoteEvent(time + dur - 50L, MidiEventType.NOTE_OFF, n, 0, InstrumentType.DX7_EPIANO.id))
            }
            time += dur
        }

        return RecordedSong(
            id = "demo_dx7_ballad",
            title = "Yamaha DX7: 80s Sunset Ballad",
            bpm = 92,
            instrumentId = InstrumentType.DX7_EPIANO.id,
            styleName = "Yamaha 8-Beat Pop",
            durationMs = time + 200L,
            events = events
        )
    }
}
