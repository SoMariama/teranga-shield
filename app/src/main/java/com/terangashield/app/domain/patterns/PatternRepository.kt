package com.terangashield.app.domain.patterns

import android.content.Context
import com.terangashield.app.domain.model.AppLanguage
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Charge les jeux de données de scénarios (patterns_fr/en/ru.json) depuis les assets.
 * Utilisé par [com.terangashield.app.domain.engine.mock.MockRiskAnalysisEngine] en attendant
 * l'intégration du vrai modèle NLU multilingue.
 */
class PatternRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val cache = mutableMapOf<AppLanguage, PatternsDataset>()

    fun load(language: AppLanguage): PatternsDataset =
        cache.getOrPut(language) {
            val fileName = "patterns_${language.code}.json"
            val text = try {
                context.assets.open(fileName).bufferedReader(Charsets.UTF_8).use { it.readText() }
            } catch (e: IOException) {
                return@getOrPut PatternsDataset(language = language.code, categories = emptyMap())
            }
            json.decodeFromString(PatternsDataset.serializer(), text)
        }
}
