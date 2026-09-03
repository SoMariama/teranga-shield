package com.terangashield.app.domain.engine.mock

import com.terangashield.app.domain.engine.AudioWindow
import com.terangashield.app.domain.engine.SpeechToTextEngine
import com.terangashield.app.domain.engine.TranscriptionResult
import com.terangashield.app.domain.model.AppLanguage
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Implémentation factice de la transcription : ne fait aucune reconnaissance vocale réelle.
 * Permet de simuler un déroulé de conversation (voir [pushSimulatedUtterance]) pour tester le
 * flux UI (score cumulatif, alertes) avant l'intégration d'un vrai moteur (Whisper tiny/base quantisé).
 */
class MockSpeechToTextEngine : SpeechToTextEngine {

    private var languageHint: AppLanguage = AppLanguage.FRENCH
    private val simulatedUtterances = ConcurrentLinkedQueue<String>()

    override suspend fun initialize(languageHint: AppLanguage?) {
        this.languageHint = languageHint ?: AppLanguage.FRENCH
    }

    /** Utilisé par les écrans de démo/tests pour injecter une phrase comme si elle avait été prononcée. */
    fun pushSimulatedUtterance(text: String) {
        simulatedUtterances.add(text)
    }

    override suspend fun transcribe(window: AudioWindow): TranscriptionResult {
        val text = simulatedUtterances.poll() ?: ""
        return TranscriptionResult(text = text, detectedLanguage = languageHint, confidence = if (text.isBlank()) 0f else 0.9f)
    }

    override fun release() {
        simulatedUtterances.clear()
    }
}
