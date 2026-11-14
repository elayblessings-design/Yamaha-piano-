package com.example.audio

enum class DrumSound(
    val id: Int,
    val displayName: String,
    val shortCode: String,
    val midiNote: Int,
    val defaultDecayMs: Float
) {
    KICK(1, "Kick Drum", "BD", 36, 320f),
    SNARE(2, "Snare Drum", "SD", 38, 220f),
    HIHAT_CLOSED(3, "Closed Hi-Hat", "CH", 42, 60f),
    HIHAT_OPEN(4, "Open Hi-Hat", "OH", 46, 380f),
    CLAP(5, "Hand Clap", "CP", 39, 180f),
    RIMSHOT(6, "Rimshot", "RM", 37, 90f),
    TOM_HIGH(7, "High Tom", "HT", 50, 260f),
    TOM_LOW(8, "Low Tom", "LT", 45, 360f),
    CRASH(9, "Crash Cymbal", "CR", 49, 750f),
    RIDE(10, "Ride Cymbal", "RD", 51, 550f),
    CONGA(11, "High Conga", "CG", 62, 240f),
    COWBELL(12, "Cowbell", "CB", 56, 210f);

    companion object {
        fun fromId(id: Int): DrumSound = entries.find { it.id == id } ?: KICK
    }
}

enum class DrumKit(
    val id: Int,
    val displayName: String,
    val description: String,
    val kickTuning: Float,
    val snareTuning: Float,
    val isElectronic: Boolean
) {
    STANDARD_YAMAHA(
        id = 1,
        displayName = "Yamaha Stage Custom",
        description = "Natural acoustic birch kit with punchy kick & wooden snare",
        kickTuning = 55f,
        snareTuning = 195f,
        isElectronic = false
    ),
    TRAP_808(
        id = 2,
        displayName = "808 Trap & Hip-Hop",
        description = "Sub-heavy 808 boom bass, snappy trap snare, and sizzle hats",
        kickTuning = 42f,
        snareTuning = 210f,
        isElectronic = true
    ),
    ROCK_STADIUM(
        id = 3,
        displayName = "Stadium Rock Power",
        description = "Gated reverberant stadium drums with heavy brass crash",
        kickTuning = 58f,
        snareTuning = 180f,
        isElectronic = false
    ),
    LATIN_WORLD(
        id = 4,
        displayName = "Latin & Percussion",
        description = "Congas, bongos, timbales, and wooden claves",
        kickTuning = 60f,
        snareTuning = 220f,
        isElectronic = false
    ),
    JAZZ_BRUSH(
        id = 5,
        displayName = "Vintage Jazz Brush",
        description = "Warm acoustic kick, soft brush snare, and vintage ride",
        kickTuning = 62f,
        snareTuning = 175f,
        isElectronic = false
    );

    companion object {
        fun fromId(id: Int): DrumKit = entries.find { it.id == id } ?: STANDARD_YAMAHA
    }
}
