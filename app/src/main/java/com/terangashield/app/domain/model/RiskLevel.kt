package com.terangashield.app.domain.model

/** Niveau de risque affiché à l'utilisateur, dérivé du score numérique combiné. */
enum class RiskLevel {
    SAFE,
    CAUTION,
    HIGH;

    companion object {
        fun fromScore(score: Float, thresholds: RiskThresholds): RiskLevel = when {
            score >= thresholds.highRiskThreshold -> HIGH
            score >= thresholds.cautionThreshold -> CAUTION
            else -> SAFE
        }
    }
}

/**
 * Seuils de déclenchement, calibrés par le curseur de sensibilité utilisateur.
 * Des seuils plus hauts en sensibilité "Faible" réduisent les faux positifs
 * sur les situations légitimes à forte charge émotionnelle (hôpital, banque, employeur).
 */
data class RiskThresholds(
    val cautionThreshold: Float,
    val highRiskThreshold: Float,
) {
    companion object {
        val LOW = RiskThresholds(cautionThreshold = 0.55f, highRiskThreshold = 0.80f)
        val MEDIUM = RiskThresholds(cautionThreshold = 0.40f, highRiskThreshold = 0.68f)
        val HIGH = RiskThresholds(cautionThreshold = 0.30f, highRiskThreshold = 0.55f)

        fun forSensitivity(sensitivity: DetectionSensitivity): RiskThresholds = when (sensitivity) {
            DetectionSensitivity.LOW -> LOW
            DetectionSensitivity.MEDIUM -> MEDIUM
            DetectionSensitivity.HIGH -> HIGH
        }
    }
}

enum class DetectionSensitivity { LOW, MEDIUM, HIGH }
