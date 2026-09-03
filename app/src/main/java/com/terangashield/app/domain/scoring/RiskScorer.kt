package com.terangashield.app.domain.scoring

import com.terangashield.app.domain.engine.NluResult
import com.terangashield.app.domain.engine.VoiceClassificationResult
import com.terangashield.app.domain.model.AppLanguage
import com.terangashield.app.domain.model.DetectionSensitivity
import com.terangashield.app.domain.model.RiskLevel
import com.terangashield.app.domain.model.RiskThresholds
import com.terangashield.app.domain.model.ScoreBreakdown

data class CallScoringInput(
    val nluResult: NluResult,
    val voiceResult: VoiceClassificationResult,
    val cascadeScore: Float,
    val detectedLanguage: AppLanguage,
)

/**
 * Combine le NLU, l'analyse vocale et la progression de la conversation en un score unique.
 * Classe pure (sans dépendance Android) pour rester facilement testable unitairement,
 * y compris pour les cas de faux positifs (hôpital, banque, employeur).
 */
class RiskScorer(private var sensitivity: DetectionSensitivity) {

    private val stageTracker = ConversationStageTracker()

    fun updateSensitivity(sensitivity: DetectionSensitivity) {
        this.sensitivity = sensitivity
    }

    fun scoreCallWindow(input: CallScoringInput): ScoreBreakdown {
        val stageScore = stageTracker.ingest(input.nluResult)
        val combined = (input.nluResult.riskScore * NLU_WEIGHT) +
            (input.voiceResult.combinedScore * VOICE_WEIGHT) +
            (stageScore * STAGE_WEIGHT)
        val finalScore = combined.coerceIn(0f, 1f)

        return ScoreBreakdown(
            cascadeScore = input.cascadeScore,
            nluScore = input.nluResult.riskScore,
            voiceScore = input.voiceResult.combinedScore,
            conversationStageScore = stageScore,
            finalScore = finalScore,
            triggeredCategories = stageTracker.allCategoriesSeen().toList(),
            detectedLanguage = input.detectedLanguage,
        )
    }

    fun riskLevelFor(score: Float): RiskLevel = RiskLevel.fromScore(score, RiskThresholds.forSensitivity(sensitivity))

    fun reset() = stageTracker.reset()

    companion object {
        private const val NLU_WEIGHT = 0.5f
        private const val VOICE_WEIGHT = 0.2f
        private const val STAGE_WEIGHT = 0.3f
    }
}
