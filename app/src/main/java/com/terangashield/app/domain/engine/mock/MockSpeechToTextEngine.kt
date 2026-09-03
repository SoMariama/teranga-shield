package com.terangashield.app.domain.engine.mock

import com.terangashield.app.domain.engine.SpeechToTextEngine
import com.terangashield.app.domain.engine.TranscriptionResult
import com.terangashield.app.domain.model.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Implémentation factice : ne fait aucune reconnaissance vocale réelle et n'émet jamais de
 * résultat. Utilisée en repli sur les appareils où
 * [com.terangashield.app.domain.engine.real.RealSpeechToTextEngine] n'est pas disponible (avant
 * Android 12) — dans ce cas [com.terangashield.app.service.CallAudioAnalysisService] ne lance de
 * toute façon pas l'analyse réelle (voir `isAvailable`). Pour démontrer le flux de bout en bout
 * sans reconnaisseur réel, voir [com.terangashield.app.debug.ScamSimulator], qui appelle
 * directement `RiskAnalysisEngine` avec un texte scripté plutôt que de passer par ce moteur.
 */
class MockSpeechToTextEngine : SpeechToTextEngine {
    override fun isAvailable(): Boolean = false

    override fun listen(languageHint: AppLanguage?): Flow<TranscriptionResult> = emptyFlow()
}
