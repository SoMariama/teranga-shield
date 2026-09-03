package com.terangashield.app.service.call

import com.terangashield.app.domain.model.AppLanguage
import com.terangashield.app.domain.model.RiskLevel
import com.terangashield.app.domain.model.ScenarioCategory

/**
 * État partagé de l'appel en cours entre les composants qui ne peuvent pas se passer un objet
 * directement : [com.terangashield.app.service.TerangaCallScreeningService] (décision au filtrage,
 * avant sonnerie, appels entrants uniquement), [com.terangashield.app.service.CallAudioAnalysisService]
 * (analyse de risque pendant l'appel actif) et [com.terangashield.app.service.call.TerangaInCallService]
 * (point d'enregistrement unique de l'historique, pour TOUS les appels, entrants comme sortants).
 * Un appel Android à la fois, donc un état global suffit.
 */
object CurrentCallSession {
    @Volatile var phoneNumber: String? = null
    @Volatile var isKnownContact: Boolean = false
    @Volatile var isReportedNumber: Boolean = false
    @Volatile var callStartMillis: Long = 0L
    @Volatile var trustedContactAlreadyNotified: Boolean = false

    // Renseigné progressivement par CallAudioAnalysisService au fil de l'appel (pas seulement à
    // la fin), pour que TerangaInCallService.onCallRemoved dispose toujours de la dernière
    // valeur connue au moment d'enregistrer l'appel dans l'historique.
    @Volatile var riskLevel: RiskLevel = RiskLevel.SAFE
    @Volatile var finalScore: Float = 0f
    @Volatile var triggeredCategories: List<ScenarioCategory> = emptyList()
    @Volatile var detectedLanguage: AppLanguage? = null
    @Volatile var transcriptExcerpt: String? = null

    fun start(phoneNumber: String, isKnownContact: Boolean, isReportedNumber: Boolean) {
        this.phoneNumber = phoneNumber
        this.isKnownContact = isKnownContact
        this.isReportedNumber = isReportedNumber
        this.callStartMillis = System.currentTimeMillis()
        this.trustedContactAlreadyNotified = false
        this.riskLevel = RiskLevel.SAFE
        this.finalScore = 0f
        this.triggeredCategories = emptyList()
        this.detectedLanguage = null
        this.transcriptExcerpt = null
    }

    /** N'écrase jamais un score déjà plus élevé — le niveau retenu est le maximum sur tout l'appel. */
    fun updateRiskIfHigher(
        riskLevel: RiskLevel,
        finalScore: Float,
        triggeredCategories: List<ScenarioCategory>,
        detectedLanguage: AppLanguage,
        transcriptExcerpt: String?,
    ) {
        if (riskLevel.ordinal >= this.riskLevel.ordinal) {
            this.riskLevel = riskLevel
            this.finalScore = finalScore
            this.triggeredCategories = triggeredCategories
        }
        this.detectedLanguage = detectedLanguage
        this.transcriptExcerpt = transcriptExcerpt
    }

    fun reset() {
        phoneNumber = null
        isKnownContact = false
        isReportedNumber = false
        callStartMillis = 0L
        trustedContactAlreadyNotified = false
        riskLevel = RiskLevel.SAFE
        finalScore = 0f
        triggeredCategories = emptyList()
        detectedLanguage = null
        transcriptExcerpt = null
    }
}
