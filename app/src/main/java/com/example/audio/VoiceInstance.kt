package com.example.audio

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class NoteVoice(
    val midiNote: Int,
    val instrument: InstrumentType,
    val velocity: Float, // 0.0 to 1.0
    val sampleRate: Float = 44100f
) {
    val baseFreq: Float = 440f * Math.pow(2.0, (midiNote - 69.0) / 12.0).toFloat()
    var isReleased: Boolean = false
    var isSustainedByPedal: Boolean = false
    var isDead: Boolean = false

    private var sampleCount: Long = 0
    private var releaseStartSample: Long = -1

    // Phase accumulators for harmonics
    private var phase: Double = 0.0
    private var modPhase: Double = 0.0
    private var organPhases = DoubleArray(9) { 0.0 }

    // Guitar string buffer simulation for Karplus-Strong
    private val ksBuffer: FloatArray? = if (instrument == InstrumentType.ACOUSTIC_GUITAR) {
        val len = (sampleRate / baseFreq.coerceIn(50f, 4000f)).toInt().coerceIn(10, 2048)
        val buf = FloatArray(len)
        var last = 0f
        for (i in buf.indices) {
            // White noise burst lowpassed
            val white = (Math.random().toFloat() * 2f - 1f) * velocity
            last = 0.5f * last + 0.5f * white
            buf[i] = last
        }
        buf
    } else null
    private var ksIndex = 0

    fun release() {
        if (!isReleased) {
            isReleased = true
            releaseStartSample = sampleCount
        }
    }

    fun renderSample(pitchBendRatio: Float, modDepth: Float): Float {
        if (isDead) return 0f

        val currentFreq = baseFreq * pitchBendRatio
        val timeSec = sampleCount / sampleRate
        val samplesSinceRelease = if (isReleased) (sampleCount - releaseStartSample) else 0L

        // Envelope computation
        val attackSamples = (instrument.attackMs / 1000f * sampleRate).coerceAtLeast(1f)
        val decaySamples = (instrument.decayMs / 1000f * sampleRate).coerceAtLeast(1f)
        val releaseSamples = (instrument.releaseMs / 1000f * sampleRate).coerceAtLeast(1f)

        val env: Float
        if (isReleased) {
            val relProgress = samplesSinceRelease.toFloat() / releaseSamples
            if (relProgress >= 1f) {
                isDead = true
                return 0f
            }
            val startEnv = if (releaseStartSample < attackSamples) {
                releaseStartSample / attackSamples
            } else {
                val decProgress = ((releaseStartSample - attackSamples) / decaySamples).coerceIn(0f, 1f)
                1f - decProgress * (1f - instrument.sustainLevel)
            }
            env = startEnv * (1f - relProgress) * (1f - relProgress)
        } else {
            if (sampleCount < attackSamples) {
                env = (sampleCount / attackSamples).coerceIn(0f, 1f)
            } else {
                val decProgress = ((sampleCount - attackSamples) / decaySamples).coerceIn(0f, 1f)
                val decayCurve = exp(-decProgress.toDouble() * 3.0).toFloat()
                env = instrument.sustainLevel + (1f - instrument.sustainLevel) * decayCurve
                // For long notes without sustain
                if (instrument.isPercussive && env < 0.001f) {
                    isDead = true
                    return 0f
                }
            }
        }

        // LFO vibrato
        val vibrato = if (modDepth > 0.001f) {
            sin(2.0 * PI * 5.5 * timeSec).toFloat() * 0.03f * modDepth
        } else 0f
        val effectiveFreq = currentFreq * (1f + vibrato)

        val sampleVal: Float = when (instrument) {
            InstrumentType.GRAND_PIANO -> renderYamahaGrandPiano(effectiveFreq, timeSec, env)
            InstrumentType.DX7_EPIANO -> renderDX7EPiano(effectiveFreq, timeSec, env)
            InstrumentType.CLAVINOVA -> renderClavinova(effectiveFreq, timeSec, env)
            InstrumentType.STRINGS -> renderStrings(effectiveFreq, timeSec, env, modDepth)
            InstrumentType.DRAWBAR_ORGAN -> renderDrawbarOrgan(effectiveFreq, timeSec, env)
            InstrumentType.SYNTH_BRASS -> renderSynthBrass(effectiveFreq, timeSec, env)
            InstrumentType.ACOUSTIC_GUITAR -> renderGuitar(env)
            InstrumentType.SLAP_BASS -> renderSlapBass(effectiveFreq, timeSec, env)
        }

        sampleCount++
        return sampleVal * velocity
    }

    private fun renderYamahaGrandPiano(freq: Float, timeSec: Float, env: Float): Float {
        val dt = (freq / sampleRate) * 2.0 * PI
        phase += dt
        if (phase > 2.0 * PI * 1000) phase %= (2.0 * PI)

        // Yamaha CFX Grand Piano multi-harmonic overtone spectrum with stiffness inharmonicity
        val f1 = sin(phase).toFloat()
        val f2 = sin(phase * 2.002).toFloat() * 0.55f * exp(-timeSec * 2.5f)
        val f3 = sin(phase * 3.006).toFloat() * 0.35f * exp(-timeSec * 4.0f)
        val f4 = sin(phase * 4.012).toFloat() * 0.20f * exp(-timeSec * 6.0f)
        val f5 = sin(phase * 5.020).toFloat() * 0.12f * exp(-timeSec * 8.0f)
        val f6 = sin(phase * 6.030).toFloat() * 0.07f * exp(-timeSec * 10.0f)

        // Hammer strike transient (first 25ms click)
        val hammer = if (timeSec < 0.025f) {
            val hammerDecay = 1f - (timeSec / 0.025f)
            sin(phase * 12.0).toFloat() * 0.25f * hammerDecay
        } else 0f

        val sum = (f1 + f2 + f3 + f4 + f5 + f6 + hammer) * 0.5f
        return sum * env
    }

    private fun renderDX7EPiano(freq: Float, timeSec: Float, env: Float): Float {
        // Yamaha DX7 2-Operator FM synthesis: Carrier modulated by Modulator
        val dtCarrier = (freq / sampleRate) * 2.0 * PI
        val dtMod = (freq * 14.0 / sampleRate) * 2.0 * PI // Crystalline 14th harmonic ratio

        phase += dtCarrier
        modPhase += dtMod
        if (phase > 2.0 * PI * 1000) phase %= (2.0 * PI)
        if (modPhase > 2.0 * PI * 1000) modPhase %= (2.0 * PI)

        // FM index decays sharply for crystalline attack chime then warm sine body
        val fmModIndex = (1.8f * exp(-timeSec * 5.0f) + 0.3f).toDouble()
        val modulator = sin(modPhase) * fmModIndex
        val carrier = sin(phase + modulator).toFloat()

        // Second subtle warm sub carrier
        val subCarrier = sin(phase * 0.5).toFloat() * 0.2f

        return (carrier * 0.8f + subCarrier) * env
    }

    private fun renderClavinova(freq: Float, timeSec: Float, env: Float): Float {
        val dt = (freq / sampleRate) * 2.0 * PI
        phase += dt
        if (phase > 2.0 * PI * 1000) phase %= (2.0 * PI)

        val f1 = sin(phase).toFloat()
        val f2 = sin(phase * 2.0).toFloat() * 0.40f * exp(-timeSec * 1.8f)
        val f3 = sin(phase * 3.0).toFloat() * 0.18f * exp(-timeSec * 3.0f)
        val f4 = sin(phase * 4.0).toFloat() * 0.08f * exp(-timeSec * 5.0f)
        return (f1 + f2 + f3 + f4) * 0.55f * env
    }

    private fun renderStrings(freq: Float, timeSec: Float, env: Float, modDepth: Float): Float {
        // 3 detuned sawtooths with ensemble chorus
        val dt1 = (freq * 0.997 / sampleRate) * 2.0 * PI
        val dt2 = (freq * 1.003 / sampleRate) * 2.0 * PI
        val dt3 = (freq * 1.000 / sampleRate) * 2.0 * PI

        phase += dt3
        modPhase += dt1
        if (phase > 2.0 * PI * 1000) phase %= (2.0 * PI)
        if (modPhase > 2.0 * PI * 1000) modPhase %= (2.0 * PI)

        // Soft saw approximation with harmonics
        val saw1 = (sin(phase) + 0.5 * sin(phase * 2.0) + 0.33 * sin(phase * 3.0) + 0.25 * sin(phase * 4.0)).toFloat()
        val saw2 = (sin(modPhase) + 0.5 * sin(modPhase * 2.0) + 0.33 * sin(modPhase * 3.0)).toFloat()

        val ensemble = (saw1 * 0.6f + saw2 * 0.5f) * 0.45f
        return ensemble * env
    }

    private fun renderDrawbarOrgan(freq: Float, timeSec: Float, env: Float): Float {
        // 9 standard Hammond/Yamaha drawbars: 16', 5 1/3', 8', 4', 2 2/3', 2', 1 3/5', 1 1/3', 1'
        val ratios = doubleArrayOf(0.5, 1.5, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 8.0)
        val amplitudes = floatArrayOf(0.8f, 0.4f, 0.9f, 0.7f, 0.4f, 0.5f, 0.2f, 0.2f, 0.3f)

        // Leslie speaker rotary tremolo modulation
        val tremolo = 1f + 0.15f * sin(2.0 * PI * 6.2 * timeSec).toFloat()

        var sum = 0f
        for (i in ratios.indices) {
            val dt = (freq * ratios[i] / sampleRate) * 2.0 * PI
            organPhases[i] += dt
            if (organPhases[i] > 2.0 * PI * 100) organPhases[i] %= (2.0 * PI)
            sum += sin(organPhases[i]).toFloat() * amplitudes[i]
        }
        return (sum * 0.22f * tremolo) * env
    }

    private fun renderSynthBrass(freq: Float, timeSec: Float, env: Float): Float {
        val dt = (freq / sampleRate) * 2.0 * PI
        phase += dt
        if (phase > 2.0 * PI * 1000) phase %= (2.0 * PI)

        // Analog resonant filter sweep that opens on attack and clamps down
        val filterEnv = (exp(-timeSec * 3.5f) * 0.6f + 0.4f).coerceIn(0.2f, 1.0f)
        val h1 = sin(phase).toFloat()
        val h2 = sin(phase * 2.0).toFloat() * 0.7f * filterEnv
        val h3 = sin(phase * 3.0).toFloat() * 0.5f * filterEnv
        val h4 = sin(phase * 4.0).toFloat() * 0.35f * filterEnv * filterEnv
        val h5 = sin(phase * 5.0).toFloat() * 0.2f * filterEnv * filterEnv

        return (h1 + h2 + h3 + h4 + h5) * 0.38f * env
    }

    private fun renderGuitar(env: Float): Float {
        val buf = ksBuffer ?: return 0f
        val len = buf.size
        val currentVal = buf[ksIndex]
        val nextIndex = (ksIndex + 1) % len
        val nextVal = buf[nextIndex]

        // Lowpass damping filter
        val averaged = (currentVal + nextVal) * 0.5f * 0.993f
        buf[ksIndex] = averaged
        ksIndex = nextIndex

        return currentVal * env * 1.2f
    }

    private fun renderSlapBass(freq: Float, timeSec: Float, env: Float): Float {
        val dt = (freq / sampleRate) * 2.0 * PI
        phase += dt
        if (phase > 2.0 * PI * 1000) phase %= (2.0 * PI)

        // Low fundamental punch + metallic slap pop (first 40ms)
        val f1 = sin(phase).toFloat() * 1.1f
        val f2 = sin(phase * 2.0).toFloat() * 0.6f
        val f3 = sin(phase * 3.0).toFloat() * 0.3f
        val slapPop = if (timeSec < 0.04f) {
            sin(phase * 7.5).toFloat() * 0.7f * (1f - timeSec / 0.04f)
        } else 0f

        return (f1 + f2 + f3 + slapPop) * 0.45f * env
    }
}

class DrumVoiceInstance(
    val sound: DrumSound,
    val kit: DrumKit,
    val velocity: Float,
    val sampleRate: Float = 44100f
) {
    var isDead: Boolean = false
    private var sampleCount: Long = 0
    private val totalSamples: Long = ((sound.defaultDecayMs / 1000f) * sampleRate).toLong()

    // Phase & noise state
    private var phase: Double = 0.0
    private var noiseState: Float = 0f
    private var bongoPhase: Double = 0.0

    fun renderSample(): Float {
        if (isDead || sampleCount >= totalSamples) {
            isDead = true
            return 0f
        }

        val timeSec = sampleCount / sampleRate
        val progress = sampleCount.toFloat() / totalSamples.toFloat()
        val env = (1f - progress) * (1f - progress)

        val out: Float = when (sound) {
            DrumSound.KICK -> renderKick(timeSec, env)
            DrumSound.SNARE -> renderSnare(timeSec, env)
            DrumSound.HIHAT_CLOSED -> renderHiHat(timeSec, isClosed = true)
            DrumSound.HIHAT_OPEN -> renderHiHat(timeSec, isClosed = false)
            DrumSound.CLAP -> renderClap(timeSec)
            DrumSound.RIMSHOT -> renderRimshot(timeSec, env)
            DrumSound.TOM_HIGH -> renderTom(timeSec, env, pitchMultiplier = 1.6f)
            DrumSound.TOM_LOW -> renderTom(timeSec, env, pitchMultiplier = 0.9f)
            DrumSound.CRASH -> renderCymbal(timeSec, env, isCrash = true)
            DrumSound.RIDE -> renderCymbal(timeSec, env, isCrash = false)
            DrumSound.CONGA -> renderConga(timeSec, env)
            DrumSound.COWBELL -> renderCowbell(timeSec, env)
        }

        sampleCount++
        return (out * velocity).coerceIn(-1f, 1f)
    }

    private fun renderKick(timeSec: Float, env: Float): Float {
        // Fast pitch drop: start around 160Hz (or 110Hz for 808) sweeping down to 45Hz
        val startFreq = if (kit.isElectronic) 110f else 165f
        val endFreq = kit.kickTuning
        val sweepTime = if (kit.isElectronic) 0.12f else 0.055f
        val sweepProgress = (timeSec / sweepTime).coerceIn(0f, 1f)
        val currentFreq = startFreq + (endFreq - startFreq) * sweepProgress

        val dt = (currentFreq / sampleRate) * 2.0 * PI
        phase += dt

        val sine = sin(phase).toFloat()

        // Initial click transient
        val click = if (timeSec < 0.012f) {
            (Math.random().toFloat() * 2f - 1f) * (1f - timeSec / 0.012f) * 0.4f
        } else 0f

        // Long sub boom for 808
        val subTail = if (kit.isElectronic) {
            val subDecay = exp(-timeSec * 3.5f)
            (sine * subDecay) * 1.2f
        } else {
            val kickDecay = exp(-timeSec * 9.0f)
            (sine + click) * kickDecay
        }
        return subTail * 1.1f
    }

    private fun renderSnare(timeSec: Float, env: Float): Float {
        // Snare = tuned tonal body + shaped white noise burst
        val bodyFreq = kit.snareTuning
        val dt = (bodyFreq / sampleRate) * 2.0 * PI
        phase += dt

        val tone = sin(phase).toFloat() * exp(-timeSec * 16.0f) * 0.6f
        val whiteNoise = (Math.random().toFloat() * 2f - 1f)
        noiseState = 0.35f * noiseState + 0.65f * whiteNoise // simple highpass/bandpass
        val noiseDecay = exp(-timeSec * 12.0f)
        val noise = noiseState * noiseDecay * 0.85f

        val snap = if (kit.isElectronic && timeSec < 0.015f) {
            sin(phase * 4.0).toFloat() * (1f - timeSec / 0.015f) * 0.5f
        } else 0f

        return (tone + noise + snap) * 0.9f
    }

    private fun renderHiHat(timeSec: Float, isClosed: Boolean): Float {
        // Sum of multiple high frequency inharmonic square waves for metallic ring
        val squareFreqs = doubleArrayOf(340.0, 560.0, 720.0, 890.0, 1150.0, 1420.0)
        var metallic = 0.0
        for (f in squareFreqs) {
            val s = sin(2.0 * PI * f * timeSec)
            metallic += if (s > 0) 1.0 else -1.0
        }
        val noise = (Math.random().toFloat() * 2f - 1f) * 0.6f
        val composite = (metallic.toFloat() * 0.15f + noise)

        val decayRate = if (isClosed) 38.0f else 6.5f
        val decay = exp(-timeSec * decayRate)
        return composite * decay * 0.75f
    }

    private fun renderClap(timeSec: Float): Float {
        // Multi-tap noise burst (3 quick pre-claps then main tail)
        val preClapSpacings = floatArrayOf(0.010f, 0.022f, 0.034f)
        var preAmp = 0f
        for (spacing in preClapSpacings) {
            if (timeSec >= spacing && timeSec < spacing + 0.008f) {
                preAmp = 0.7f * (1f - (timeSec - spacing) / 0.008f)
            }
        }
        val mainStart = 0.045f
        val mainAmp = if (timeSec >= mainStart) {
            exp(-(timeSec - mainStart) * 16.0f)
        } else 0f

        val white = (Math.random().toFloat() * 2f - 1f)
        noiseState = 0.4f * noiseState + 0.6f * white
        return noiseState * (preAmp + mainAmp) * 0.85f
    }

    private fun renderRimshot(timeSec: Float, env: Float): Float {
        val dt = (1200f / sampleRate) * 2.0 * PI
        phase += dt
        val click = sin(phase).toFloat() * exp(-timeSec * 50.0f)
        val wood = sin(phase * 0.4).toFloat() * exp(-timeSec * 35.0f)
        return (click * 0.7f + wood * 0.5f)
    }

    private fun renderTom(timeSec: Float, env: Float, pitchMultiplier: Float): Float {
        val startFreq = 220f * pitchMultiplier
        val endFreq = 95f * pitchMultiplier
        val sweepProgress = (timeSec / 0.08f).coerceIn(0f, 1f)
        val freq = startFreq + (endFreq - startFreq) * sweepProgress
        val dt = (freq / sampleRate) * 2.0 * PI
        phase += dt
        val sine = sin(phase).toFloat()
        val decay = exp(-timeSec * 7.5f)
        return sine * decay * 0.85f
    }

    private fun renderCymbal(timeSec: Float, env: Float, isCrash: Boolean): Float {
        val white = (Math.random().toFloat() * 2f - 1f)
        noiseState = 0.2f * noiseState + 0.8f * white
        val shimmer = sin(2.0 * PI * 4200.0 * timeSec).toFloat() * 0.2f
        val decayRate = if (isCrash) 3.5f else 5.5f
        val decay = exp(-timeSec * decayRate)
        return (noiseState * 0.8f + shimmer) * decay * 0.6f
    }

    private fun renderConga(timeSec: Float, env: Float): Float {
        val dt = (280f / sampleRate) * 2.0 * PI
        bongoPhase += dt
        val tone = sin(bongoPhase).toFloat()
        val slap = if (timeSec < 0.015f) (Math.random().toFloat() * 2f - 1f) * (1f - timeSec / 0.015f) * 0.5f else 0f
        val decay = exp(-timeSec * 10.0f)
        return (tone + slap) * decay * 0.8f
    }

    private fun renderCowbell(timeSec: Float, env: Float): Float {
        val dt1 = (587f / sampleRate) * 2.0 * PI
        val dt2 = (845f / sampleRate) * 2.0 * PI
        phase += dt1
        bongoPhase += dt2
        val sq1 = if (sin(phase) > 0) 0.5f else -0.5f
        val sq2 = if (sin(bongoPhase) > 0) 0.5f else -0.5f
        val decay = exp(-timeSec * 12.0f)
        return (sq1 + sq2) * decay * 0.7f
    }
}
