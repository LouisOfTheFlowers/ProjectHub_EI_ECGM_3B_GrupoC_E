package com.example.projecthub.settings

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.sin

object AppSoundPlayer {
    private val executor = Executors.newSingleThreadExecutor()

    fun playClick() {
        executor.execute {
            runCatching {
                val sampleRate = 44_100
                val durationMs = 120
                val sampleCount = sampleRate * durationMs / 1_000
                val buffer = ShortArray(sampleCount)
                val frequency = 880.0
                val maxAmplitude = Short.MAX_VALUE * 0.35

                for (index in buffer.indices) {
                    val fade = when {
                        index < sampleCount / 6 -> index.toDouble() / (sampleCount / 6)
                        index > sampleCount * 5 / 6 -> (sampleCount - index).toDouble() / (sampleCount / 6)
                        else -> 1.0
                    }
                    buffer[index] = (sin(2.0 * PI * index * frequency / sampleRate) * maxAmplitude * fade).toInt().toShort()
                }

                val audioTrack = AudioTrack.Builder()
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
                    .setBufferSizeInBytes(buffer.size * Short.SIZE_BYTES)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                Thread.sleep(durationMs + 40L)
                audioTrack.stop()
                audioTrack.release()
            }
        }
    }
}
