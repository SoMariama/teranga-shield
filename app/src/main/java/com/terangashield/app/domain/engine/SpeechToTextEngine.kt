package com.terangashield.app.domain.engine

import com.terangashield.app.domain.model.AppLanguage

data class TranscriptionResult(
    val text: String,
    val detectedLanguage: AppLanguage,
    val confidence: Float,
)

/**
 * Abstraction du moteur de transcription vocale locale.
 * Implémentation V1 : [com.terangashield.app.domain.engine.mock.MockSpeechToTextEngine].
 * Implémentation cible : Whisper tiny/base quantisé (TFLite/ONNX Runtime Mobile), voir README.
 */
interface SpeechToTextEngine {
    suspend fun initialize(languageHint: AppLanguage?)
    suspend fun transcribe(window: AudioWindow): TranscriptionResult
    fun release()
}
