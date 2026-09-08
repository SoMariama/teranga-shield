package com.terangashield.app.domain.patterns

import kotlinx.serialization.Serializable

/** Doit rester en phase avec le schéma documenté dans assets/patterns_schema.json. */
@Serializable
data class PatternEntry(
    val phrase: String,
    /** Contribution au score si la phrase (ou une formulation proche) est détectée, entre 0 et 1. */
    val weight: Float,
)

@Serializable
data class LegitimateContextEntry(
    val phrase: String,
    /** Catégorie dont le score est atténué quand ce contexte légitime est détecté (ex. hôpital, banque, employeur). */
    val dampens: String,
    val dampenFactor: Float = 0.5f,
)

/**
 * Règle de détection par mots-clés (complément des phrases entières de [PatternEntry], trop
 * strictes pour du langage courant — voir [com.terangashield.app.domain.patterns.PatternMatcher]).
 * La règle ne se déclenche que si CHAQUE groupe de [groups] a au moins un terme présent dans le
 * texte — un seul groupe suffit pour les règles à un seul groupe (ex. mots d'urgence isolés), tandis
 * qu'une règle à deux groupes (ex. verbe d'action + terme sensible) exige la co-occurrence des deux,
 * pour éviter qu'un mot isolé et courant ("code" dans "code postal") déclenche une alerte à lui seul.
 */
@Serializable
data class KeywordSignalRule(
    val weight: Float,
    val groups: List<List<String>>,
)

@Serializable
data class PatternsDataset(
    val language: String,
    val schemaVersion: Int = 1,
    val categories: Map<String, List<PatternEntry>>,
    val legitimateContextAllowlist: List<LegitimateContextEntry> = emptyList(),
    val keywordSignals: Map<String, List<KeywordSignalRule>> = emptyMap(),
)
