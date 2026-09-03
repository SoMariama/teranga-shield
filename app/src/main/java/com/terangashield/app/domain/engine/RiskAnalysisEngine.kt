package com.terangashield.app.domain.engine

import com.terangashield.app.domain.model.AppLanguage
import com.terangashield.app.domain.model.ScenarioCategory

data class NluResult(
    val riskScore: Float,
    val matchedCategories: List<ScenarioCategory>,
    val confidence: Float,
)

/**
 * Abstraction du NLU local (compréhension d'intention, pas simple mot-clé).
 * Implémentation V1 : [com.terangashield.app.domain.engine.mock.MockRiskAnalysisEngine], basée sur
 * les jeux de données patterns_xx.json (voir [com.terangashield.app.domain.patterns.PatternRepository]).
 * Implémentation cible : modèle multilingue quantisé int8 (TFLite, NNAPI si disponible).
 */
interface RiskAnalysisEngine {
    suspend fun initialize()
    suspend fun analyze(text: String, language: AppLanguage): NluResult
    fun release()
}
