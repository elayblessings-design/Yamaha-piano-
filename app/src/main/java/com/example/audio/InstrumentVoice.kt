package com.example.audio

enum class InstrumentType(
    val id: Int,
    val displayName: String,
    val category: String,
    val attackMs: Float,
    val decayMs: Float,
    val sustainLevel: Float,
    val releaseMs: Float,
    val isPercussive: Boolean,
    val description: String
) {
    GRAND_PIANO(
        id = 1,
        displayName = "CFX Grand Piano",
        category = "Piano",
        attackMs = 3f,
        decayMs = 2800f,
        sustainLevel = 0.05f,
        releaseMs = 180f,
        isPercussive = true,
        description = "Yamaha concert grand with rich acoustic harmonics & hammer resonance"
    ),
    DX7_EPIANO(
        id = 2,
        displayName = "DX7 Electric Piano",
        category = "E-Piano",
        attackMs = 2f,
        decayMs = 2200f,
        sustainLevel = 0.15f,
        releaseMs = 220f,
        isPercussive = true,
        description = "Classic 80s 2-operator FM crystalline bell tine"
    ),
    CLAVINOVA(
        id = 3,
        displayName = "Clavinova Warm",
        category = "Piano",
        attackMs = 5f,
        decayMs = 3200f,
        sustainLevel = 0.08f,
        releaseMs = 200f,
        isPercussive = true,
        description = "Mellow warm ballad acoustic piano with felt hammer resonance"
    ),
    STRINGS(
        id = 4,
        displayName = "Orchestral Strings",
        category = "Ensemble",
        attackMs = 120f,
        decayMs = 1500f,
        sustainLevel = 0.85f,
        releaseMs = 450f,
        isPercussive = false,
        description = "Multi-voice detuned ensemble strings with lush vibrato"
    ),
    DRAWBAR_ORGAN(
        id = 5,
        displayName = "Jazz Drawbar Organ",
        category = "Organ",
        attackMs = 8f,
        decayMs = 500f,
        sustainLevel = 0.95f,
        releaseMs = 80f,
        isPercussive = false,
        description = "Full drawbar harmonic organ with Leslie rotary tremolo"
    ),
    SYNTH_BRASS(
        id = 6,
        displayName = "CS Synth Brass",
        category = "Synth",
        attackMs = 25f,
        decayMs = 800f,
        sustainLevel = 0.70f,
        releaseMs = 240f,
        isPercussive = false,
        description = "Iconic Yamaha CS-80 punchy analog filter brass"
    ),
    ACOUSTIC_GUITAR(
        id = 7,
        displayName = "Acoustic Steel",
        category = "Guitar",
        attackMs = 4f,
        decayMs = 1800f,
        sustainLevel = 0.04f,
        releaseMs = 140f,
        isPercussive = true,
        description = "Karplus-Strong physical plucked acoustic guitar"
    ),
    SLAP_BASS(
        id = 8,
        displayName = "Yamaha Slap Bass",
        category = "Bass",
        attackMs = 2f,
        decayMs = 1200f,
        sustainLevel = 0.12f,
        releaseMs = 120f,
        isPercussive = true,
        description = "Punchy electric slap bass with metallic string pop"
    );

    companion object {
        fun fromId(id: Int): InstrumentType = entries.find { it.id == id } ?: GRAND_PIANO
    }
}
