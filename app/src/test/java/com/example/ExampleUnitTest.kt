package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testChordDetection() {
    // C Major Triad (C4=60, E4=64, G4=67)
    val cMajorChord = com.example.midi.ChordDetector.detectChord(listOf(60, 64, 67))
    assertEquals("C", cMajorChord)

    // A Minor (A4=69, C5=72, E5=76)
    val aMinorChord = com.example.midi.ChordDetector.detectChord(listOf(69, 72, 76))
    assertEquals("Am", aMinorChord)
  }

  @Test
  fun testMidiFileHeader() {
    val song = com.example.midi.RecordedSong(
      id = "test",
      title = "Test Song",
      bpm = 120,
      instrumentId = 1,
      durationMs = 1000L,
      events = listOf(
        com.example.midi.MidiNoteEvent(0L, com.example.midi.MidiEventType.NOTE_ON, 60, 100),
        com.example.midi.MidiNoteEvent(500L, com.example.midi.MidiEventType.NOTE_OFF, 60, 0)
      )
    )
    val bytes = com.example.midi.MidiFileExporter.buildMidiBytes(song)
    assertTrue(bytes.size > 20)
    // Check "MThd" header magic
    assertEquals('M'.code.toByte(), bytes[0])
    assertEquals('T'.code.toByte(), bytes[1])
    assertEquals('h'.code.toByte(), bytes[2])
    assertEquals('d'.code.toByte(), bytes[3])
  }
}
