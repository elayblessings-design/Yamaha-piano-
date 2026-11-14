package com.example.midi

import android.content.Context
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object MidiFileExporter {
    private const val TICKS_PER_BEAT = 480

    fun exportToMidiFile(context: Context, song: RecordedSong): File {
        val midiDir = File(context.cacheDir, "midi").apply { mkdirs() }
        val cleanName = song.title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val file = File(midiDir, "$cleanName.mid")

        val midiBytes = buildMidiBytes(song)
        FileOutputStream(file).use { it.write(midiBytes) }
        return file
    }

    fun buildMidiBytes(song: RecordedSong): ByteArray {
        val baos = ByteArrayOutputStream()

        // 1. Header Chunk: "MThd", len=6, format=0, tracks=1, division=TICKS_PER_BEAT
        baos.write("MThd".toByteArray(Charsets.US_ASCII))
        writeInt32(baos, 6)
        writeInt16(baos, 0) // Format 0
        writeInt16(baos, 1) // 1 track
        writeInt16(baos, TICKS_PER_BEAT)

        // 2. Track Data
        val trackStream = ByteArrayOutputStream()

        // Microseconds per quarter note: 60,000,000 / BPM
        val bpm = song.bpm.coerceIn(40, 240)
        val usPerBeat = 60000000 / bpm

        // Time signature (4/4) at delta=0
        writeVariableLength(trackStream, 0)
        trackStream.write(byteArrayOf(0xFF.toByte(), 0x58.toByte(), 0x04.toByte(), 0x04.toByte(), 0x02.toByte(), 0x18.toByte(), 0x08.toByte()))

        // Set Tempo at delta=0
        writeVariableLength(trackStream, 0)
        trackStream.write(byteArrayOf(0xFF.toByte(), 0x51.toByte(), 0x03.toByte()))
        trackStream.write((usPerBeat shr 16 and 0xFF).toByte().toInt())
        trackStream.write((usPerBeat shr 8 and 0xFF).toByte().toInt())
        trackStream.write((usPerBeat and 0xFF).toByte().toInt())

        // Track Name at delta=0
        val titleBytes = song.title.toByteArray(Charsets.UTF_8)
        writeVariableLength(trackStream, 0)
        trackStream.write(byteArrayOf(0xFF.toByte(), 0x03.toByte()))
        writeVariableLength(trackStream, titleBytes.size)
        trackStream.write(titleBytes)

        // Sort events by timestamp
        val sortedEvents = song.events.sortedBy { it.timestampMs }

        var previousEventTicks = 0L

        for (event in sortedEvents) {
            // Convert timestampMs to ticks:
            // ticks = (timestampMs * 1000) * TICKS_PER_BEAT / usPerBeat
            val eventTicks = ((event.timestampMs * 1000.0) * TICKS_PER_BEAT / usPerBeat.toDouble()).toLong()
            val deltaTicks = (eventTicks - previousEventTicks).coerceAtLeast(0L)
            previousEventTicks = eventTicks

            writeVariableLength(trackStream, deltaTicks.toInt())

            when (event.type) {
                MidiEventType.NOTE_ON -> {
                    // Channel 0: Note On (0x90, note, velocity)
                    trackStream.write(0x90)
                    trackStream.write(event.note.coerceIn(0, 127))
                    trackStream.write(event.velocity.coerceIn(1, 127))
                }
                MidiEventType.NOTE_OFF -> {
                    // Channel 0: Note Off (0x80, note, 0x00)
                    trackStream.write(0x80)
                    trackStream.write(event.note.coerceIn(0, 127))
                    trackStream.write(0x00)
                }
                MidiEventType.DRUM_HIT -> {
                    // Channel 9 (MIDI 10 standard drums): 0x99, note, velocity
                    trackStream.write(0x99)
                    trackStream.write(event.note.coerceIn(0, 127))
                    trackStream.write(event.velocity.coerceIn(1, 127))
                }
            }
        }

        // End of Track Meta Event at delta=120
        writeVariableLength(trackStream, 120)
        trackStream.write(byteArrayOf(0xFF.toByte(), 0x2F.toByte(), 0x00.toByte()))

        // 3. Write "MTrk" and track data length
        val trackBytes = trackStream.toByteArray()
        baos.write("MTrk".toByteArray(Charsets.US_ASCII))
        writeInt32(baos, trackBytes.size)
        baos.write(trackBytes)

        return baos.toByteArray()
    }

    private fun writeVariableLength(out: ByteArrayOutputStream, value: Int) {
        var v = value
        val buffer = IntArray(4)
        var i = 0
        buffer[i++] = v and 0x7F
        v = v shr 7
        while (v > 0) {
            buffer[i++] = (v and 0x7F) or 0x80
            v = v shr 7
        }
        for (j in i - 1 downTo 0) {
            out.write(buffer[j])
        }
    }

    private fun writeInt32(out: ByteArrayOutputStream, value: Int) {
        out.write((value shr 24) and 0xFF)
        out.write((value shr 16) and 0xFF)
        out.write((value shr 8) and 0xFF)
        out.write(value and 0xFF)
    }

    private fun writeInt16(out: ByteArrayOutputStream, value: Int) {
        out.write((value shr 8) and 0xFF)
        out.write(value and 0xFF)
    }
}
