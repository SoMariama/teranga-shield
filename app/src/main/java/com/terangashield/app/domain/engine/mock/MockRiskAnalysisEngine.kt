package com.terangashield.app.domain.engine.mock

import com.terangashield.app.domain.engine.NluResult
import com.terangashield.app.domain.engine.RiskAnalysisEngine
import com.terangashield.app.domain.model.AppLanguage
import com.terangashield.app.domain.patterns.PatternMatcher
import com.terangashield.app.domain.patterns.PatternRepository

/**
 * Implémentation factice du NLU, basée sur une correspondance de phrases pondérées contre les
 * jeux de données patterns_xx.json ([PatternMatcher]), plutôt qu'une vraie compréhension
 * d'intention. Suffisante pour tester le flux UI de bout en bout avant l'intégration du vrai
 * modèle TFLite.
 */
class MockRiskAnalysisEngine(private val patternRepository: PatternRepository) : RiskAnalysisEngine {

    override suspend fun initialize() {
        // Rien à charger : le mock lit les patterns à la demande, sans modèle binaire.
    }

    override suspend fun analyze(text: String, language: AppLanguage): NluResult =
        PatternMatcher.score(text, patternRepository.load(language))

    override fun release() = Unit
}
