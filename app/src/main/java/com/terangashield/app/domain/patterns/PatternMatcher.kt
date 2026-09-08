package com.terangashield.app.domain.patterns

import com.terangashield.app.domain.engine.NluResult
import com.terangashield.app.domain.model.ScenarioCategory
import java.text.Normalizer
import kotlin.math.min

/**
 * Correspondance de phrases pondérées contre un [PatternsDataset] — cœur logique du NLU factice
 * ([com.terangashield.app.domain.engine.mock.MockRiskAnalysisEngine]), extrait en fonction pure
 * (sans dépendance Android) pour rester facilement testable unitairement.
 */
object PatternMatcher {

    fun score(text: String, dataset: PatternsDataset): NluResult {
        val normalizedText = normalize(text)
        if (normalizedText.isBlank()) return NluResult(0f, emptyList(), confidence = 0f)

        val categoryScores = mutableMapOf<String, Float>()
        // Termes exacts (issus du texte d'origine, pas normalisés) ayant déclenché chaque
        // catégorie — utilisés pour surligner le passage suspect dans l'UI (voir MessageDetailScreen).
        val categoryMatchedTerms = mutableMapOf<String, MutableList<String>>()
        for ((category, entries) in dataset.categories) {
            var categoryScore = 0f
            for (entry in entries) {
                if (normalizedText.contains(normalize(entry.phrase))) {
                    categoryScore += entry.weight
                    categoryMatchedTerms.getOrPut(category) { mutableListOf() }.add(entry.phrase)
                }
            }
            for (rule in dataset.keywordSignals[category].orEmpty()) {
                val matchedPerGroup = rule.groups.map { group -> group.firstOrNull { term -> normalizedText.contains(normalize(term)) } }
                if (matchedPerGroup.all { it != null }) {
                    categoryScore += rule.weight
                    matchedPerGroup.filterNotNull().forEach {
                        categoryMatchedTerms.getOrPut(category) { mutableListOf() }.add(it)
                    }
                }
            }
            if (categoryScore > 0f) categoryScores[category] = min(categoryScore, 1f)
        }

        for (allow in dataset.legitimateContextAllowlist) {
            if (normalizedText.contains(normalize(allow.phrase))) {
                val current = categoryScores[allow.dampens]
                if (current != null) {
                    categoryScores[allow.dampens] = current * allow.dampenFactor
                }
            }
        }

        val matched = categoryScores.entries.filter { it.value > 0.05f }
        val triggeredCategories = matched.mapNotNull { runCatching { ScenarioCategory.valueOf(it.key) }.getOrNull() }
        val finalScore = min(matched.sumOf { it.value.toDouble() }.toFloat() / 2f, 1f)
        val matchedTerms = matched.flatMap { categoryMatchedTerms[it.key].orEmpty() }.distinct()

        return NluResult(
            riskScore = finalScore,
            matchedCategories = triggeredCategories,
            confidence = if (matched.isEmpty()) 0.2f else 0.6f,
            matchedTerms = matchedTerms,
        )
    }

    private fun normalize(text: String): String =
        Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
}
