package com.terangashield.app.service.sms

import android.content.Context
import com.terangashield.app.R
import com.terangashield.app.ServiceLocator
import com.terangashield.app.data.db.entity.SmsRecordEntity
import com.terangashield.app.domain.model.EventType
import com.terangashield.app.domain.model.RiskLevel
import com.terangashield.app.service.NotificationHelper
import com.terangashield.app.service.call.ContactsLookup
import kotlinx.coroutines.flow.first

/**
 * Flux "Messages" du prompt produit : vérification expéditeur, analyse NLU + lien suspect,
 * badge de risque, alerte et notification du contact de confiance si le score est élevé.
 */
class SmsProcessor(private val context: Context) {

    suspend fun process(sender: String, body: String) {
        val locator = ServiceLocator.get(context)
        val isKnownContact = ContactsLookup.isKnownContact(context, sender)
        val language = locator.userPreferencesRepository.language.first()
        val sensitivity = locator.userPreferencesRepository.sensitivity.first()
        locator.smsRiskAnalyzer.updateSensitivity(sensitivity)

        if (isKnownContact) {
            locator.smsRepository.insert(
                SmsRecordEntity(
                    sender = sender,
                    isKnownContact = true,
                    timestampMillis = System.currentTimeMillis(),
                    riskLevel = RiskLevel.SAFE,
                    finalScore = 0f,
                    reason = com.terangashield.app.domain.model.SmsRiskReason.NONE,
                    detectedLanguage = language,
                    bodyExcerpt = body.take(BODY_EXCERPT_MAX_CHARS),
                    containsSuspiciousLink = false,
                    suspiciousLinkUrl = null,
                    opened = false,
                    trustedContactNotified = false,
                ),
            )
            return
        }

        val result = locator.smsRiskAnalyzer.analyze(body, language)
        val notifyTrustedContact = result.riskLevel == RiskLevel.HIGH

        locator.smsRepository.insert(
            SmsRecordEntity(
                sender = sender,
                isKnownContact = false,
                timestampMillis = System.currentTimeMillis(),
                riskLevel = result.riskLevel,
                finalScore = result.score,
                reason = result.reason,
                detectedLanguage = language,
                // Le corps complet n'est conservé que si le risque n'est pas élevé.
                bodyExcerpt = if (result.riskLevel == RiskLevel.HIGH) null else body.take(BODY_EXCERPT_MAX_CHARS),
                containsSuspiciousLink = result.suspiciousLinkUrl != null,
                suspiciousLinkUrl = result.suspiciousLinkUrl,
                opened = false,
                trustedContactNotified = notifyTrustedContact,
            ),
        )

        if (result.riskLevel == RiskLevel.HIGH) {
            NotificationHelper.showHighRiskAlert(
                context,
                R.string.alert_high_risk_call_title,
                R.string.sms_link_warning_body,
            )
            locator.trustedContactNotifier.notifyHighRisk(EventType.SMS, (result.score * 100).toInt())
        }
    }

    companion object {
        private const val BODY_EXCERPT_MAX_CHARS = 500
    }
}
