package com.terangashield.app.domain.engine

import com.terangashield.app.domain.model.AppLanguage
import kotlinx.coroutines.flow.Flow

data class TranscriptionResult(
    val text: String,
    val detectedLanguage: AppLanguage,
    val confidence: Float,
    val isFinal: Boolean,
)

/**
 * Abstraction du moteur de transcription vocale locale.
 *
 * Modèle en flux (session d'écoute continue) plutôt qu'un appel par fenêtre audio brute : les
 * moteurs de reconnaissance vocale (y compris le reconnaisseur embarqué d'Android, utilisé par
 * [com.terangashield.app.domain.engine.real.RealSpeechToTextEngine]) gèrent eux-mêmes la capture
 * micro et la détection de fin de parole — leur imposer de découper l'audio à notre place aurait
 * nécessité de dupliquer cette logique inutilement, contrairement à un modèle qu'on héberge
 * nous-mêmes.
 *
 * Implémentation V1 de test : [com.terangashield.app.domain.engine.mock.MockSpeechToTextEngine].
 */
interface SpeechToTextEngine {
    /** Vrai si ce moteur peut tourner entièrement hors ligne sur cet appareil. */
    fun isAvailable(): Boolean

    /**
     * Démarre une session d'écoute continue ; chaque résultat (partiel ou final) est émis au fil
     * de l'eau. La collecte du [Flow] doit être annulée pour arrêter l'écoute (fin d'appel).
     */
    fun listen(languageHint: AppLanguage?): Flow<TranscriptionResult>
}
