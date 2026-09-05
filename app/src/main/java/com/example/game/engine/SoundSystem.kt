package com.example.game.engine

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Sistema de sonido sintetizado en tiempo real usando AudioTrack de Android.
 * Genera efectos acústicos sci-fi de baja latencia sin dependencias de archivos pesados.
 */
class SoundSystem {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 22050

    fun playShoot() {
        scope.launch {
            // Frecuencia descendente tipo láser chirp de 1200Hz a 250Hz en 80ms
            val durationMs = 85
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            var phase = 0.0

            for (i in 0 until numSamples) {
                val progress = i.toFloat() / numSamples
                val freq = 1200.0 - (progress * 950.0)
                phase += 2.0 * PI * freq / sampleRate
                val envelope = (1.0 - progress)
                buffer[i] = (sin(phase) * 28000.0 * envelope).toInt().toShort()
            }
            playPcm(buffer)
        }
    }

    fun playHit() {
        scope.launch {
            // Sonido metálico de impacto (dos tonos armónicos 880Hz y 1320Hz decayendo rápido)
            val durationMs = 60
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toFloat() / numSamples
                val env = (1.0 - progress) * (1.0 - progress)
                val s1 = sin(2.0 * PI * 880.0 * i / sampleRate)
                val s2 = sin(2.0 * PI * 1320.0 * i / sampleRate) * 0.5
                buffer[i] = ((s1 + s2) * 24000.0 * env).toInt().toShort()
            }
            playPcm(buffer)
        }
    }

    fun playExplosion() {
        scope.launch {
            // Ruido blanco filtrado con decaimiento tipo explosión 3D
            val durationMs = 220
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            var lastSample = 0.0

            for (i in 0 until numSamples) {
                val progress = i.toFloat() / numSamples
                val white = (Random.nextFloat() * 2f - 1f).toDouble()
                // Filtro paso bajo simple
                lastSample = lastSample * 0.85 + white * 0.15
                val bass = sin(2.0 * PI * 85.0 * i / sampleRate) * 0.6
                val env = (1.0 - progress) * (1.0 - progress)
                buffer[i] = ((lastSample + bass) * 30000.0 * env).toInt().toShort()
            }
            playPcm(buffer)
        }
    }

    fun playReload() {
        scope.launch {
            // Secuencia mecánica de dos clics rápidos
            val durationMs = 140
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / sampleRate
                var amp = 0.0
                if (t in 0.01..0.04) {
                    val sub = (t - 0.01) / 0.03
                    amp = sin(2.0 * PI * 650.0 * t) * (1.0 - sub)
                } else if (t in 0.08..0.12) {
                    val sub = (t - 0.08) / 0.04
                    amp = sin(2.0 * PI * 920.0 * t) * (1.0 - sub)
                }
                buffer[i] = (amp * 26000.0).toInt().toShort()
            }
            playPcm(buffer)
        }
    }

    fun playEmpty() {
        scope.launch {
            val durationMs = 30
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val progress = i.toFloat() / numSamples
                val amp = sin(2.0 * PI * 380.0 * i / sampleRate) * (1.0 - progress)
                buffer[i] = (amp * 18000.0).toInt().toShort()
            }
            playPcm(buffer)
        }
    }

    private fun playPcm(buffer: ShortArray) {
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
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
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            // Se libera automáticamente después de la duración
            track.setNotificationMarkerPosition(buffer.size)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onPeriodicNotification(p0: AudioTrack?) {}
                override fun onMarkerReached(t: AudioTrack?) {
                    try {
                        t?.stop()
                        t?.release()
                    } catch (_: Exception) {}
                }
            })
        } catch (_: Exception) {
            // Ignorar silenciosamente si el hardware de audio no está disponible
        }
    }
}
