package com.terangashield.app.domain.model

/**
 * Détail du score combiné, conservé pour affichage ("éléments déclencheurs") mais jamais
 * couplé à la transcription verbatim quand le score est élevé.
 */
data class ScoreBreakdown(
    val cascadeScore: Float,
    val nluScore: Float,
    val voiceScore: Float,
    val conversationStageScore: Float,
    val finalScore: Float,
    val triggeredCategories: List<ScenarioCategory>,
    val detectedLanguage: AppLanguage,
)

data class TrustedContact(
    val name: String,
    val phoneNumber: String,
)

/** Consentement horodaté, tracé pour pouvoir justifier a posteriori ce que l'utilisateur a accepté. */
data class ConsentRecord(
    val defaultAppsConsentGiven: Boolean,
    val defaultAppsConsentTimestamp: Long?,
    val micAnalysisConsentGiven: Boolean,
    val micAnalysisConsentTimestamp: Long?,
    val consentTextVersion: Int,
)

const val CURRENT_CONSENT_TEXT_VERSION = 1
