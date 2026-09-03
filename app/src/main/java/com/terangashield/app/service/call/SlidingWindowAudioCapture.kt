package com.terangashield.app.service.call

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import com.terangashield.app.domain.engine.AudioWindow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Capture par fenêtres glissantes de quelques secondes, jamais un flux audio continu envoyé en
 * une fois au modèle — voir la contrainte de légèreté. Chaque fenêtre est traitée puis relâchée.
 */
class SlidingWindowAudioCapture(
    private val context: Context,
    private val windowDurationMillis: Long = WINDOW_DURATION_MILLIS,
    private val sampleRateHz: Int = SAMPLE_RATE_HZ,
) {
    fun hasMicPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    /** Émet une [AudioWindow] toutes les [windowDurationMillis] ms tant que le flux est collecté. */
    fun captureWindows(): Flow<AudioWindow> = flow {
        if (!hasMicPermission()) return@flow

        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRateHz,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        if (minBufferSize <= 0) return@flow

        val samplesPerWindow = (sampleRateHz * windowDurationMillis / 1000L).toInt()
        val bufferSize = maxOf(minBufferSize, samplesPerWindow * 2)

        @Suppress("MissingPermission")
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            sampleRateHz,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
        )

        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            audioRecord.release()
            return@flow
        }

        try {
            audioRecord.startRecording()
            val windowBuffer = ShortArray(samplesPerWindow)
            while (true) {
                val start = System.currentTimeMillis()
                var offset = 0
                while (offset < windowBuffer.size) {
                    val read = audioRecord.read(windowBuffer, offset, windowBuffer.size - offset)
                    if (read <= 0) break
                    offset += read
                }
                emit(
                    AudioWindow(
                        pcm16 = windowBuffer.copyOf(offset),
                        sampleRateHz = sampleRateHz,
                        startOffsetMillis = start,
                        durationMillis = windowDurationMillis,
                    ),
                )
            }
        } finally {
            audioRecord.stop()
            audioRecord.release()
        }
    }

    companion object {
        const val WINDOW_DURATION_MILLIS = 4000L
        const val SAMPLE_RATE_HZ = 16_000
    }
}
