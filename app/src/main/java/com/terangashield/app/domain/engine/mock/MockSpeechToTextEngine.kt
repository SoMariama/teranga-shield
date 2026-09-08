package com.terangashield.app.domain.engine.mock

import com.terangashield.app.domain.engine.SpeechToTextEngine
import com.terangashield.app.domain.engine.TranscriptionResult
import com.terangashield.app.domain.model.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Implémentation factice : ne fait aucune reconnaissance vocale réelle et n'émet jamais de
 * résultat. Pour démontrer le flux de bout en bout sans reconnaisseur réel, voir
 * [com.terangashield.app.debug.ScamSimulator], qui appelle directement `RiskAnalysisEngine` avec
 * un texte scripté plutôt que de passer par ce moteur.
 */
class MockSpeechToTextEngine : SpeechToTextEngine {
    override fun isAvailable(language: AppLanguage): Boolean = false

    override fun listen(languageHint: AppLanguage?): Flow<TranscriptionResult> = emptyFlow()
}
