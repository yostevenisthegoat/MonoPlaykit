package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class FeedbackManager(context: Context) {
    private val appContext = context.applicationContext

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private var toneGenerator: ToneGenerator? = null

    private fun getToneGenerator(): ToneGenerator? {
        if (toneGenerator == null) {
            toneGenerator = runCatching {
                ToneGenerator(AudioManager.STREAM_MUSIC, 65)
            }.getOrNull()
        }
        return toneGenerator
    }

    fun click(soundEnabled: Boolean, hapticEnabled: Boolean) {
        if (hapticEnabled) vibrateMs(18L)
        if (soundEnabled) playPcmClick(accent = false)
    }

    fun accentClick(soundEnabled: Boolean, hapticEnabled: Boolean) {
        if (hapticEnabled) vibrateMs(32L)
        if (soundEnabled) playPcmClick(accent = true)
    }

    fun alert(soundEnabled: Boolean, hapticEnabled: Boolean) {
        if (hapticEnabled) vibrateMs(180L)
        if (soundEnabled) {
            runCatching {
                getToneGenerator()?.startTone(ToneGenerator.TONE_PROP_BEEP2, 240)
            }
        }
    }

    fun shortBeep(soundEnabled: Boolean, hapticEnabled: Boolean) {
        if (hapticEnabled) vibrateMs(40L)
        if (soundEnabled) {
            runCatching {
                getToneGenerator()?.startTone(ToneGenerator.TONE_PROP_BEEP, 110)
            }
        }
    }

    fun playPcmClick(accent: Boolean) {
        Thread {
            runCatching {
                val sampleRate = 22050
                val durationSamples = (sampleRate * 0.032).toInt()
                val freq = if (accent) 1320.0 else 880.0
                val samples = ShortArray(durationSamples)
                for (i in 0 until durationSamples) {
                    val t = i.toDouble() / sampleRate
                    val env = exp(-t * 110.0)
                    val s = sin(2.0 * PI * freq * t) * env
                    samples[i] = (s * Short.MAX_VALUE * (if (accent) 0.75 else 0.55)).toInt().toShort()
                }
                val bufferBytes = samples.size * 2
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferBytes)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                track.write(samples, 0, samples.size)
                track.play()
                Thread.sleep(50L)
                track.release()
            }
        }.start()
    }

    private fun vibrateMs(ms: Long) {
        runCatching {
            val vib = vibrator ?: return
            if (!vib.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(ms)
            }
        }
    }

    fun release() {
        runCatching { toneGenerator?.release() }
        toneGenerator = null
    }
}
