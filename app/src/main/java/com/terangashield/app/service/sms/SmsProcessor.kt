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
 *
 * Tous les messages sont analysés, y compris ceux venant d'un contact enregistré — un compte de
 * contact compromis qui envoie "donne-moi le code que tu viens de recevoir" à ses contacts est un
 * schéma d'arnaque réel et courant ; exempter les contacts d'analyse (choix initial du cahier des
 * charges) créait un angle mort. `isKnownContact` reste stocké pour l'affichage, mais ne
 * court-circuite plus l'analyse.
 */
class SmsProcessor(private val context: Context) {

    suspend fun process(sender: String, body: String) {
        val locator = ServiceLocator.get(context)
        val isKnownContact = ContactsLookup.isKnownContact(context, sender)
        val language = locator.userPreferencesRepository.language.first()
        val sensitivity = locator.userPreferencesRepository.sensitivity.first()
        locator.smsRiskAnalyzer.updateSensitivity(sensitivity)

        val result = locator.smsRiskAnalyzer.analyze(body, language)
        val notifyTrustedContact = result.riskLevel == RiskLevel.HIGH

        locator.smsRepository.insert(
            SmsRecordEntity(
                sender = sender,
                isKnownContact = isKnownContact,
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
