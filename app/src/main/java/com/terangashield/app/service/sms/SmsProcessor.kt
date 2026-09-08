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
 * badge de risque, alerte et notification du contact de confiance si le score est élevé — sinon
 * notification standard de nouveau message (voir [NotificationHelper.showNewMessageNotification]).
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

        val messageId = locator.smsRepository.insert(
            SmsRecordEntity(
                sender = sender,
                isKnownContact = isKnownContact,
                timestampMillis = System.currentTimeMillis(),
                riskLevel = result.riskLevel,
                finalScore = result.score,
                reason = result.reason,
                detectedLanguage = language,
                // Le message complet est conservé, y compris à risque élevé : l'utilisateur doit
                // pouvoir relire exactement ce qui lui a été envoyé pour évaluer l'alerte.
                body = body.take(BODY_MAX_CHARS),
                containsSuspiciousLink = result.suspiciousLinkUrl != null,
                suspiciousLinkUrl = result.suspiciousLinkUrl,
                opened = false,
                trustedContactNotified = notifyTrustedContact,
                highlightTerms = result.highlightTerms,
            ),
        )

        if (result.riskLevel == RiskLevel.HIGH) {
            NotificationHelper.showHighRiskAlert(
                context,
                R.string.alert_high_risk_call_title,
                R.string.sms_link_warning_body,
            )
            locator.trustedContactNotifier.notifyHighRisk(EventType.SMS, (result.score * 100).toInt())
        } else {
            // Depuis que l'app est le gestionnaire SMS par défaut, le système ne notifie plus lui-même
            // les messages entrants — sans ça, un message normal arrivait silencieusement, invisible.
            val displayName = ContactsLookup.getContactDisplayName(context, sender) ?: sender
            NotificationHelper.showNewMessageNotification(context, messageId, displayName, body.take(NOTIFICATION_PREVIEW_MAX_CHARS))
        }
    }

    companion object {
        private const val BODY_MAX_CHARS = 2000
        private const val NOTIFICATION_PREVIEW_MAX_CHARS = 120
    }
}
