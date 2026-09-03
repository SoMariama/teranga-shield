package com.terangashield.app.domain.scoring

import com.terangashield.app.domain.engine.RiskAnalysisEngine
import com.terangashield.app.domain.model.AppLanguage
import com.terangashield.app.domain.model.DetectionSensitivity
import com.terangashield.app.domain.model.RiskLevel
import com.terangashield.app.domain.model.RiskThresholds
import com.terangashield.app.domain.model.ScenarioCategory
import com.terangashield.app.domain.model.SmsRiskReason

data class SmsAnalysisResult(
    val score: Float,
    val riskLevel: RiskLevel,
    val reason: SmsRiskReason,
    val suspiciousLinkUrl: String?,
)

/**
 * Analyse d'un SMS : plus rapide que le flux appel, pas de transcription à faire.
 * Combine le NLU (liens suspects, urgence, demande de code, usurpation) et une détection de lien.
 */
class SmsRiskAnalyzer(
    private val riskAnalysisEngine: RiskAnalysisEngine,
    private var sensitivity: DetectionSensitivity,
) {
    fun updateSensitivity(sensitivity: DetectionSensitivity) {
        this.sensitivity = sensitivity
    }

    suspend fun analyze(body: String, language: AppLanguage): SmsAnalysisResult {
        val nluResult = riskAnalysisEngine.analyze(body, language)
        val link = URL_REGEX.find(body)?.value

        // Un lien seul, sans autre signal, est traité comme un signal faible : beaucoup de SMS
        // légitimes (livraison, rendez-vous) contiennent des liens.
        val linkContribution = if (link != null) 0.15f else 0f
        val score = (nluResult.riskScore + linkContribution).coerceIn(0f, 1f)
        val riskLevel = RiskLevel.fromScore(score, RiskThresholds.forSensitivity(sensitivity))

        val reason = when {
            riskLevel == RiskLevel.SAFE -> SmsRiskReason.NONE
            ScenarioCategory.SENSITIVE_INFO_REQUEST in nluResult.matchedCategories -> SmsRiskReason.OTP_REQUEST
            link != null -> SmsRiskReason.SUSPICIOUS_LINK
            ScenarioCategory.INSTITUTION_IMPERSONATION in nluResult.matchedCategories -> SmsRiskReason.BRAND_IMPERSONATION
            else -> SmsRiskReason.NONE
        }

        return SmsAnalysisResult(score = score, riskLevel = riskLevel, reason = reason, suspiciousLinkUrl = link)
    }

    companion object {
        private val URL_REGEX = Regex("""https?://\S+|www\.\S+""", RegexOption.IGNORE_CASE)
    }
}
