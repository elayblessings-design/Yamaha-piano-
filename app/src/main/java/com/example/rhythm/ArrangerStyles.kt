package com.example.rhythm

import com.example.audio.DrumKit
import com.example.audio.DrumSound

enum class StyleSection {
    INTRO,
    MAIN_A,
    MAIN_B,
    FILL_IN,
    ENDING
}

data class ArrangerStyle(
    val id: Int,
    val name: String,
    val category: String,
    val defaultBpm: Int,
    val recommendedKit: DrumKit,
    val mainA: StylePattern,
    val mainB: StylePattern,
    val fillIn: StylePattern
)

object StyleLibrary {
    private fun makeTrack(sound: DrumSound, vararg activeSteps: Int): TrackPattern {
        val steps = BooleanArray(16)
        for (s in activeSteps) {
            if (s in 0..15) steps[s] = true
        }
        return TrackPattern(sound, steps)
    }

    val STYLES: List<ArrangerStyle> = listOf(
        // 1. Trap & Hip-Hop Beat (Song Beat style)
        ArrangerStyle(
            id = 1,
            name = "Trap & Hip-Hop Beat",
            category = "Urban / Hip-Hop",
            defaultBpm = 140,
            recommendedKit = DrumKit.TRAP_808,
            mainA = StylePattern(
                "Main A",
                listOf(
                    makeTrack(DrumSound.KICK, 0, 6, 8, 11),
                    makeTrack(DrumSound.SNARE, 4, 12),
                    makeTrack(DrumSound.CLAP, 4, 12),
                    makeTrack(DrumSound.HIHAT_CLOSED, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
                    makeTrack(DrumSound.HIHAT_OPEN, 2, 10),
                    makeTrack(DrumSound.CONGA, 3, 7, 14)
                )
            ),
            mainB = StylePattern(
                "Main B (Double Time)",
                listOf(
                    makeTrack(DrumSound.KICK, 0, 3, 6, 8, 11, 14),
                    makeTrack(DrumSound.SNARE, 4, 12, 15),
                    makeTrack(DrumSound.CLAP, 4, 12),
                    makeTrack(DrumSound.HIHAT_CLOSED, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
                    makeTrack(DrumSound.HIHAT_OPEN, 2, 6, 10, 14),
                    makeTrack(DrumSound.COWBELL, 4, 10, 12)
                )
            ),
            fillIn = StylePattern(
                "Fill In",
                listOf(
                    makeTrack(DrumSound.KICK, 0, 2, 4),
                    makeTrack(DrumSound.SNARE, 6, 8, 10, 11, 12, 13, 14, 15),
                    makeTrack(DrumSound.CRASH, 0, 12),
                    makeTrack(DrumSound.TOM_HIGH, 8, 10),
                    makeTrack(DrumSound.TOM_LOW, 12, 14)
                )
            )
        ),

        // 2. Pop 8-Beat (Classic Yamaha PSR Pop Ballad)
        ArrangerStyle(
            id = 2,
            name = "Yamaha 8-Beat Pop",
            category = "Pop / Ballad",
            defaultBpm = 108,
            recommendedKit = DrumKit.STANDARD_YAMAHA,
            mainA = StylePattern(
                "Main A",
                listOf(
                    makeTrack(DrumSound.KICK, 0, 6, 10),
                    makeTrack(DrumSound.SNARE, 4, 12),
                    makeTrack(DrumSound.HIHAT_CLOSED, 0, 2, 4, 6, 8, 10, 12, 14),
                    makeTrack(DrumSound.HIHAT_OPEN, 14),
                    makeTrack(DrumSound.RIMSHOT, 8)
                )
            ),
            mainB = StylePattern(
                "Main B",
                listOf(
                    makeTrack(DrumSound.KICK, 0, 2, 6, 10, 14),
                    makeTrack(DrumSound.SNARE, 4, 12),
                    makeTrack(DrumSound.HIHAT_CLOSED, 0, 2, 4, 6, 8, 10, 12, 14),
                    makeTrack(DrumSound.RIDE, 0, 2, 4, 6, 8, 10, 12, 14),
                    makeTrack(DrumSound.CRASH, 0)
                )
            ),
            fillIn = StylePattern(
                "Fill In",
                listOf(
                    makeTrack(DrumSound.KICK, 0, 4),
                    makeTrack(DrumSound.SNARE, 8, 10),
                    makeTrack(DrumSound.TOM_HIGH, 11, 12),
                    makeTrack(DrumSound.TOM_LOW, 13, 14),
                    makeTrack(DrumSound.CRASH, 15)
                )
            )
        ),

        // 3. Afrobeat & Amapiano Groove
        ArrangerStyle(
            id = 3,
            name = "Afrobeat & Amapiano",
            category = "African / World",
            defaultBpm = 114,
            recommendedKit = DrumKit.LATIN_WORLD,
            mainA = StylePattern(
                "Main A",
                listOf(
                    makeTrack(DrumSound.KICK, 0, 4, 8, 12),
                    makeTrack(DrumSound.RIMSHOT, 3, 6, 10, 14),
                    makeTrack(DrumSound.HIHAT_CLOSED, 0, 2, 3, 6, 8, 10, 11, 14),
                    makeTrack(DrumSound.CONGA, 2, 5, 8, 11, 13),
                    makeTrack(DrumSound.CLAP, 4, 12)
                )
            ),
            mainB = StylePattern(
                "Main B",
                listOf(
                    makeTrack(DrumSound.KICK, 0, 3, 6, 8, 11, 14),
                    makeTrack(DrumSound.RIMSHOT, 3, 6, 10, 14),
                    makeTrack(DrumSound.HIHAT_CLOSED, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
                    makeTrack(DrumSound.CONGA, 0, 2, 5, 8, 11, 13, 15),
                    makeTrack(DrumSound.COWBELL, 0, 3, 6, 8, 10, 12)
                )
            ),
            fillIn = StylePattern(
                "Fill In",
                listOf(
                    makeTrack(DrumSound.CONGA, 0, 2, 4, 6, 8, 10, 12, 14),
                    makeTrack(DrumSound.TOM_HIGH, 8, 10),
                    makeTrack(DrumSound.TOM_LOW, 12, 14),
                    makeTrack(DrumSound.CRASH, 0, 15)
                )
            )
        ),

        // 4. 16-Beat Rock Stadium
        ArrangerStyle(
            id = 4,
            name = "16-Beat Stadium Rock",
            category = "Rock",
            defaultBpm = 126,
            recommendedKit = DrumKit.ROCK_STADIUM,
            mainA = StylePattern(
                "Main A",
                listOf(
                    makeTrack(DrumSound.KICK, 0, 7, 8, 10),
                    makeTrack(DrumSound.SNARE, 4, 12),
                    makeTrack(DrumSound.HIHAT_CLOSED, 0, 2, 4, 6, 8, 10, 12, 14),
                    makeTrack(DrumSound.HIHAT_OPEN, 6, 14)
                )
            ),
            mainB = StylePattern(
                "Main B",
                listOf(
                    makeTrack(DrumSound.KICK, 0, 3, 6, 8, 10, 14),
                    makeTrack(DrumSound.SNARE, 4, 12),
                    makeTrack(DrumSound.RIDE, 0, 2, 4, 6, 8, 10, 12, 14),
                    makeTrack(DrumSound.CRASH, 0)
                )
            ),
            fillIn = StylePattern(
                "Fill In",
                listOf(
                    makeTrack(DrumSound.SNARE, 0, 2, 4, 6),
                    makeTrack(DrumSound.TOM_HIGH, 8, 10),
                    makeTrack(DrumSound.TOM_LOW, 12, 14),
                    makeTrack(DrumSound.CRASH, 15)
                )
            )
        ),

        // 5. Funk & Disco Groove
        ArrangerStyle(
            id = 5,
            name = "Funk & Disco Groove",
            category = "Funk / Soul",
            defaultBpm = 118,
            recommendedKit = DrumKit.STANDARD_YAMAHA,
            mainA = StylePattern(
                "Main A",
                listOf(
                    makeTrack(DrumSound.KICK, 0, 6, 10),
                    makeTrack(DrumSound.SNARE, 4, 12),
                    makeTrack(DrumSound.HIHAT_CLOSED, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
                    makeTrack(DrumSound.HIHAT_OPEN, 2, 10, 14),
                    makeTrack(DrumSound.CLAP, 4, 12)
                )
            ),
            mainB = StylePattern(
                "Main B",
                listOf(
                    makeTrack(DrumSound.KICK, 0, 4, 8, 12),
                    makeTrack(DrumSound.SNARE, 4, 12),
                    makeTrack(DrumSound.HIHAT_OPEN, 2, 6, 10, 14),
                    makeTrack(DrumSound.COWBELL, 0, 3, 6, 8, 11, 14)
                )
            ),
            fillIn = StylePattern(
                "Fill In",
                listOf(
                    makeTrack(DrumSound.SNARE, 4, 7, 10, 13, 14, 15),
                    makeTrack(DrumSound.KICK, 0, 8),
                    makeTrack(DrumSound.CRASH, 0, 15)
                )
            )
        ),

        // 6. EDM 4-On-The-Floor
        ArrangerStyle(
            id = 6,
            name = "EDM & Club Dance",
            category = "Electronic / Dance",
            defaultBpm = 128,
            recommendedKit = DrumKit.TRAP_808,
            mainA = StylePattern(
                "Main A",
                listOf(
                    makeTrack(DrumSound.KICK, 0, 4, 8, 12),
                    makeTrack(DrumSound.CLAP, 4, 12),
                    makeTrack(DrumSound.HIHAT_OPEN, 2, 6, 10, 14),
                    makeTrack(DrumSound.HIHAT_CLOSED, 0, 4, 8, 12)
                )
            ),
            mainB = StylePattern(
                "Main B",
                listOf(
                    makeTrack(DrumSound.KICK, 0, 4, 8, 12),
                    makeTrack(DrumSound.SNARE, 4, 12),
                    makeTrack(DrumSound.CLAP, 4, 12),
                    makeTrack(DrumSound.HIHAT_CLOSED, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
                    makeTrack(DrumSound.CRASH, 0)
                )
            ),
            fillIn = StylePattern(
                "Fill In",
                listOf(
                    makeTrack(DrumSound.SNARE, 0, 2, 4, 6, 8, 10, 12, 13, 14, 15),
                    makeTrack(DrumSound.CRASH, 15)
                )
            )
        ),

        // 7. Latin Bossa & Reggaeton
        ArrangerStyle(
            id = 7,
            name = "Latin Bossa & Dembow",
            category = "Latin",
            defaultBpm = 98,
            recommendedKit = DrumKit.LATIN_WORLD,
            mainA = StylePattern(
                "Main A",
                listOf(
                    makeTrack(DrumSound.KICK, 0, 4, 8, 12),
                    makeTrack(DrumSound.RIMSHOT, 3, 6, 11, 14),
                    makeTrack(DrumSound.HIHAT_CLOSED, 0, 2, 4, 6, 8, 10, 12, 14),
                    makeTrack(DrumSound.CONGA, 2, 5, 8, 11, 14)
                )
            ),
            mainB = StylePattern(
                "Main B",
                listOf(
                    makeTrack(DrumSound.KICK, 0, 4, 8, 12),
                    makeTrack(DrumSound.SNARE, 3, 6, 11, 14),
                    makeTrack(DrumSound.HIHAT_CLOSED, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
                    makeTrack(DrumSound.COWBELL, 0, 3, 6, 8, 10, 13)
                )
            ),
            fillIn = StylePattern(
                "Fill In",
                listOf(
                    makeTrack(DrumSound.CONGA, 0, 2, 4, 6, 8, 10, 12),
                    makeTrack(DrumSound.TOM_HIGH, 8, 10),
                    makeTrack(DrumSound.TOM_LOW, 12, 14),
                    makeTrack(DrumSound.CRASH, 15)
                )
            )
        ),

        // 8. Jazz & Swing Brush
        ArrangerStyle(
            id = 8,
            name = "Jazz & Swing Brush",
            category = "Jazz",
            defaultBpm = 132,
            recommendedKit = DrumKit.JAZZ_BRUSH,
            mainA = StylePattern(
                "Main A",
                listOf(
                    makeTrack(DrumSound.KICK, 0, 4, 8, 12),
                    makeTrack(DrumSound.SNARE, 4, 12),
                    makeTrack(DrumSound.RIDE, 0, 4, 6, 8, 12, 14),
                    makeTrack(DrumSound.HIHAT_CLOSED, 4, 12)
                )
            ),
            mainB = StylePattern(
                "Main B",
                listOf(
                    makeTrack(DrumSound.KICK, 0, 4, 8, 12),
                    makeTrack(DrumSound.SNARE, 4, 10, 12),
                    makeTrack(DrumSound.RIDE, 0, 4, 6, 8, 12, 14),
                    makeTrack(DrumSound.HIHAT_CLOSED, 4, 12),
                    makeTrack(DrumSound.CRASH, 0)
                )
            ),
            fillIn = StylePattern(
                "Fill In",
                listOf(
                    makeTrack(DrumSound.SNARE, 0, 3, 6, 9, 12, 14),
                    makeTrack(DrumSound.TOM_LOW, 12, 14),
                    makeTrack(DrumSound.CRASH, 15)
                )
            )
        )
    )
}
