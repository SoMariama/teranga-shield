package com.terangashield.app.domain.scoring

import com.terangashield.app.domain.engine.NluResult
import com.terangashield.app.domain.engine.VoiceClassificationResult
import com.terangashield.app.domain.model.AppLanguage
import com.terangashield.app.domain.model.DetectionSensitivity
import com.terangashield.app.domain.model.RiskLevel
import com.terangashield.app.domain.model.ScenarioCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RiskScorerTest {

    private val silentVoice = VoiceClassificationResult(0f, 0f, 0f)

    private fun nlu(score: Float, vararg categories: ScenarioCategory) =
        NluResult(riskScore = score, matchedCategories = categories.toList(), confidence = 0.6f)

    @Test
    fun `no signal stays safe`() {
        val scorer = RiskScorer(DetectionSensitivity.MEDIUM)
        val breakdown = scorer.scoreCallWindow(
            CallScoringInput(nlu(0f), silentVoice, cascadeScore = 0f, detectedLanguage = AppLanguage.FRENCH),
        )
        assertEquals(RiskLevel.SAFE, scorer.riskLevelFor(breakdown.finalScore))
    }

    @Test
    fun `single isolated urgency phrase is not enough to reach high risk`() {
        val scorer = RiskScorer(DetectionSensitivity.MEDIUM)
        val breakdown = scorer.scoreCallWindow(
            CallScoringInput(nlu(0.6f, ScenarioCategory.FABRICATED_URGENCY), silentVoice, 0.5f, AppLanguage.FRENCH),
        )
        assertTrue(
            "un seul signal isolé ne doit pas déclencher une alerte de risque élevé",
            scorer.riskLevelFor(breakdown.finalScore) != RiskLevel.HIGH,
        )
    }

    @Test
    fun `full scam progression across the call reaches high risk`() {
        val scorer = RiskScorer(DetectionSensitivity.MEDIUM)
        // Schéma classique : mise en confiance -> urgence fabriquée -> demande d'information sensible.
        scorer.scoreCallWindow(CallScoringInput(nlu(0.5f, ScenarioCategory.TRUST_BUILDING), silentVoice, 0.4f, AppLanguage.FRENCH))
        scorer.scoreCallWindow(CallScoringInput(nlu(0.6f, ScenarioCategory.FABRICATED_URGENCY), silentVoice, 0.5f, AppLanguage.FRENCH))
        val last = scorer.scoreCallWindow(
            CallScoringInput(nlu(1.0f, ScenarioCategory.SENSITIVE_INFO_REQUEST), silentVoice, 0.6f, AppLanguage.FRENCH),
        )
        assertEquals(RiskLevel.HIGH, scorer.riskLevelFor(last.finalScore))
    }

    @Test
    fun `false positive - hospital call with dampened urgency stays safe`() {
        val scorer = RiskScorer(DetectionSensitivity.MEDIUM)
        // Le NLU (voir PatternMatcher) atténue déjà le score d'urgence pour ce contexte légitime ;
        // on vérifie ici que le RiskScorer ne l'amplifie pas artificiellement.
        val breakdown = scorer.scoreCallWindow(
            CallScoringInput(nlu(0.12f, ScenarioCategory.FABRICATED_URGENCY), silentVoice, 0.3f, AppLanguage.FRENCH),
        )
        assertEquals(RiskLevel.SAFE, scorer.riskLevelFor(breakdown.finalScore))
    }

    @Test
    fun `false positive - bank appointment reminder stays safe`() {
        val scorer = RiskScorer(DetectionSensitivity.MEDIUM)
        val breakdown = scorer.scoreCallWindow(
            CallScoringInput(nlu(0.16f, ScenarioCategory.INSTITUTION_IMPERSONATION), silentVoice, 0.3f, AppLanguage.FRENCH),
        )
        assertTrue(scorer.riskLevelFor(breakdown.finalScore) != RiskLevel.HIGH)
    }

    @Test
    fun `false positive - employer HR call about a job interview stays safe`() {
        val scorer = RiskScorer(DetectionSensitivity.MEDIUM)
        val breakdown = scorer.scoreCallWindow(
            CallScoringInput(nlu(0.15f, ScenarioCategory.TRUST_BUILDING), silentVoice, 0.3f, AppLanguage.FRENCH),
        )
        assertEquals(RiskLevel.SAFE, scorer.riskLevelFor(breakdown.finalScore))
    }

    @Test
    fun `higher sensitivity lowers the threshold for the same score`() {
        val breakdown = RiskScorer(DetectionSensitivity.HIGH).scoreCallWindow(
            CallScoringInput(nlu(0.55f, ScenarioCategory.SENSITIVE_INFO_REQUEST), silentVoice, 0.5f, AppLanguage.FRENCH),
        )
        val highSensitivityLevel = RiskScorer(DetectionSensitivity.HIGH).riskLevelFor(breakdown.finalScore)
        val lowSensitivityLevel = RiskScorer(DetectionSensitivity.LOW).riskLevelFor(breakdown.finalScore)
        assertTrue(highSensitivityLevel.ordinal >= lowSensitivityLevel.ordinal)
    }

    @Test
    fun `reset clears conversation progression`() {
        val scorer = RiskScorer(DetectionSensitivity.MEDIUM)
        scorer.scoreCallWindow(CallScoringInput(nlu(0.5f, ScenarioCategory.TRUST_BUILDING), silentVoice, 0.4f, AppLanguage.FRENCH))
        scorer.reset()
        val breakdown = scorer.scoreCallWindow(
            CallScoringInput(nlu(0.1f, ScenarioCategory.FABRICATED_URGENCY), silentVoice, 0.2f, AppLanguage.FRENCH),
        )
        assertEquals(RiskLevel.SAFE, scorer.riskLevelFor(breakdown.finalScore))
    }
}
