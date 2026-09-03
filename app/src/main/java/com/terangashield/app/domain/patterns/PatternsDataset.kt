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

@Serializable
data class PatternsDataset(
    val language: String,
    val schemaVersion: Int = 1,
    val categories: Map<String, List<PatternEntry>>,
    val legitimateContextAllowlist: List<LegitimateContextEntry> = emptyList(),
)
