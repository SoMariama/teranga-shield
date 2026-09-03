package com.terangashield.app.domain.scoring

import com.terangashield.app.domain.engine.NluResult
import com.terangashield.app.domain.model.ScenarioCategory

/**
 * Suivi de la progression de la conversation sur tout l'appel : le score n'est pas calculé
 * phrase par phrase de façon isolée, il s'accumule, et une prime est ajoutée si le schéma
 * classique d'arnaque est observé (mise en confiance -> urgence fabriquée -> demande sensible).
 */
class ConversationStageTracker {
    private val categoriesSeen = linkedSetOf<ScenarioCategory>()
    private var runningScore = 0f

    fun ingest(nluResult: NluResult): Float {
        categoriesSeen.addAll(nluResult.matchedCategories)
        runningScore = (runningScore * 0.7f) + (nluResult.riskScore * 0.3f)
        return (runningScore + progressionBonus()).coerceIn(0f, 1f)
    }

    fun allCategoriesSeen(): Set<ScenarioCategory> = categoriesSeen

    fun reset() {
        categoriesSeen.clear()
        runningScore = 0f
    }

    private fun progressionBonus(): Float {
        val hasTrust = ScenarioCategory.TRUST_BUILDING in categoriesSeen
        val hasUrgency = ScenarioCategory.FABRICATED_URGENCY in categoriesSeen
        val hasSensitive = ScenarioCategory.SENSITIVE_INFO_REQUEST in categoriesSeen
        return when {
            hasTrust && hasUrgency && hasSensitive -> 0.25f
            (hasTrust && hasUrgency) || (hasUrgency && hasSensitive) || (hasTrust && hasSensitive) -> 0.12f
            else -> 0f
        }
    }
}
