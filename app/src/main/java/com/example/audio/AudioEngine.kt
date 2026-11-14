package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow

class AudioEngine {
    companion object {
        const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE_FRAMES = 2048
        private const val MAX_POLYPHONY = 28
    }

    private var audioTrack: AudioTrack? = null
    private var renderThread: Thread? = null
    private val isRunning = AtomicBoolean(false)

    // Voices
    private val activeVoices = ConcurrentLinkedQueue<NoteVoice>()
    private val activeDrums = ConcurrentLinkedQueue<DrumVoiceInstance>()

    // Global parameters
    @Volatile var masterVolume: Float = 0.85f
    @Volatile var reverbLevel: Float = 0.35f
    @Volatile var isSustainPedalOn: Boolean = false
    @Volatile var pitchBendRatio: Float = 1.0f // 1.0 is neutral
    @Volatile var modulationDepth: Float = 0.0f // 0.0 to 1.0

    // Currently selected instrument
    @Volatile var currentInstrument: InstrumentType = InstrumentType.GRAND_PIANO
    // Optional dual layered instrument
    @Volatile var dualInstrument: InstrumentType? = null
    @Volatile var isDualLayerEnabled: Boolean = false

    // Current drum kit
    @Volatile var currentDrumKit: DrumKit = DrumKit.STANDARD_YAMAHA

    // Realtime audio level meter for LCD display (0.0f to 1.0f)
    private val _vuMeterFlow = MutableStateFlow(0f)
    val vuMeterFlow = _vuMeterFlow.asStateFlow()

    // Simple stereo reverb delay line buffers
    private val revBufL = FloatArray(4410) // 100ms
    private val revBufR = FloatArray(5512) // 125ms
    private var revIdxL = 0
    private var revIdxR = 0

    fun start() {
        if (isRunning.get()) return

        val minBufSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = maxOf(minBufSize, BUFFER_SIZE_FRAMES * 4)

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
            isRunning.set(true)

            renderThread = Thread({ audioLoop() }, "YamahaAudioRenderThread").apply {
                priority = Thread.MAX_PRIORITY
                start()
            }
        } catch (e: Exception) {
            Log.e("AudioEngine", "Failed to start AudioTrack", e)
        }
    }

    fun stop() {
        isRunning.set(false)
        try {
            renderThread?.join(500)
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            Log.e("AudioEngine", "Error stopping audio track", e)
        } finally {
            audioTrack = null
            renderThread = null
            activeVoices.clear()
            activeDrums.clear()
        }
    }

    fun noteOn(midiNote: Int, velocity: Float = 0.85f, overrideInstrument: InstrumentType? = null) {
        val instrument = overrideInstrument ?: currentInstrument

        // Check if note is already held to retrigger cleanly
        for (v in activeVoices) {
            if (v.midiNote == midiNote && v.instrument == instrument && !v.isReleased) {
                v.release()
            }
        }

        // Steal oldest voice if exceeding polyphony
        while (activeVoices.size >= MAX_POLYPHONY) {
            val oldest = activeVoices.poll() ?: break
            oldest.isDead = true
        }

        val voice = NoteVoice(
            midiNote = midiNote,
            instrument = instrument,
            velocity = velocity.coerceIn(0.1f, 1.0f),
            sampleRate = SAMPLE_RATE.toFloat()
        )
        activeVoices.add(voice)

        // If dual layer is on and no override, also trigger the layer instrument
        if (isDualLayerEnabled && overrideInstrument == null && dualInstrument != null && dualInstrument != instrument) {
            val layerVoice = NoteVoice(
                midiNote = midiNote,
                instrument = dualInstrument!!,
                velocity = (velocity * 0.75f).coerceIn(0.1f, 1.0f),
                sampleRate = SAMPLE_RATE.toFloat()
            )
            activeVoices.add(layerVoice)
        }
    }

    fun noteOff(midiNote: Int) {
        for (v in activeVoices) {
            if (v.midiNote == midiNote && !v.isReleased) {
                if (isSustainPedalOn) {
                    v.isSustainedByPedal = true
                } else {
                    v.release()
                }
            }
        }
    }

    fun setSustainPedal(enabled: Boolean) {
        isSustainPedalOn = enabled
        if (!enabled) {
            // Release all voices that were held by pedal
            for (v in activeVoices) {
                if (v.isSustainedByPedal) {
                    v.isSustainedByPedal = false
                    v.release()
                }
            }
        }
    }

    fun setPitchBendSemitones(semitones: Float) {
        // Map semitones (-2 to +2) to frequency ratio: 2^(semitones/12)
        pitchBendRatio = 2.0.pow(semitones.toDouble() / 12.0).toFloat()
    }

    fun setModulation(depth: Float) {
        modulationDepth = depth.coerceIn(0f, 1f)
    }

    fun triggerDrum(sound: DrumSound, kit: DrumKit? = null, velocity: Float = 0.9f) {
        val targetKit = kit ?: currentDrumKit

        // Choke closed hi-hat over open hi-hat
        if (sound == DrumSound.HIHAT_CLOSED) {
            for (d in activeDrums) {
                if (d.sound == DrumSound.HIHAT_OPEN) {
                    d.isDead = true
                }
            }
        }

        while (activeDrums.size >= 16) {
            val oldest = activeDrums.poll() ?: break
            oldest.isDead = true
        }

        val instance = DrumVoiceInstance(
            sound = sound,
            kit = targetKit,
            velocity = velocity.coerceIn(0.1f, 1.0f),
            sampleRate = SAMPLE_RATE.toFloat()
        )
        activeDrums.add(instance)
    }

    fun allNotesOff() {
        for (v in activeVoices) {
            v.release()
        }
        activeDrums.clear()
    }

    private fun audioLoop() {
        val shortBuffer = ShortArray(BUFFER_SIZE_FRAMES * 2) // Stereo (L, R)
        var meterAccumulator = 0f
        var meterCounter = 0

        while (isRunning.get()) {
            val bend = pitchBendRatio
            val mod = modulationDepth
            val masterVol = masterVolume
            val revSend = reverbLevel

            // Render audio frames
            for (i in 0 until BUFFER_SIZE_FRAMES) {
                var sumInst = 0f
                var sumDrum = 0f

                // Instrument voices
                val vIter = activeVoices.iterator()
                while (vIter.hasNext()) {
                    val voice = vIter.next()
                    if (voice.isDead) {
                        vIter.remove()
                    } else {
                        sumInst += voice.renderSample(bend, mod)
                    }
                }

                // Drum voices
                val dIter = activeDrums.iterator()
                while (dIter.hasNext()) {
                    val drum = dIter.next()
                    if (drum.isDead) {
                        dIter.remove()
                    } else {
                        sumDrum += drum.renderSample()
                    }
                }

                val dryMix = sumInst + sumDrum

                // Stereo Reverb Delay Network
                val delayedL = revBufL[revIdxL]
                val delayedR = revBufR[revIdxR]
                revBufL[revIdxL] = dryMix + delayedL * 0.45f
                revBufR[revIdxR] = dryMix + delayedR * 0.50f
                revIdxL = (revIdxL + 1) % revBufL.size
                revIdxR = (revIdxR + 1) % revBufR.size

                val outL = (dryMix + delayedL * revSend) * masterVol
                val outR = (dryMix + delayedR * revSend) * masterVol

                // Soft saturation limiter (tanh-like) to prevent digital harsh clipping
                val saturatedL = softSaturate(outL)
                val saturatedR = softSaturate(outR)

                val shortL = (saturatedL * 32767f).toInt().coerceIn(-32768, 32767).toShort()
                val shortR = (saturatedR * 32767f).toInt().coerceIn(-32768, 32767).toShort()

                val baseIdx = i * 2
                shortBuffer[baseIdx] = shortL
                shortBuffer[baseIdx + 1] = shortR

                val peak = maxOf(abs(saturatedL), abs(saturatedR))
                if (peak > meterAccumulator) meterAccumulator = peak
            }

            // Write to AudioTrack
            audioTrack?.write(shortBuffer, 0, shortBuffer.size)

            meterCounter++
            if (meterCounter >= 4) { // Update ~20Hz
                _vuMeterFlow.value = meterAccumulator.coerceIn(0f, 1f)
                meterAccumulator = 0f
                meterCounter = 0
            }
        }
    }

    private fun softSaturate(x: Float): Float {
        return when {
            x > 1.2f -> 0.85f + 0.15f * (1f - exp(-(x - 1.2f)))
            x < -1.2f -> -0.85f - 0.15f * (1f - exp(-(-x - 1.2f)))
            x > 0.6f -> 0.6f + 0.4f * (x - 0.6f) / (1f + 0.4f * (x - 0.6f))
            x < -0.6f -> -0.6f - 0.4f * (-x - 0.6f) / (1f + 0.4f * (-x - 0.6f))
            else -> x
        }
    }
}
