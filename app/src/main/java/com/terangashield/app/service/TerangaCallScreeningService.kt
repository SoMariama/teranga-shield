package com.terangashield.app.service

import android.telecom.Call
import android.telecom.CallScreeningService
import com.terangashield.app.R
import com.terangashield.app.ServiceLocator
import com.terangashield.app.service.call.ContactsLookup
import com.terangashield.app.service.call.CurrentCallSession

/**
 * Interception de l'appel entrant avant la sonnerie (flux "Appels", étapes 1 à 4 du prompt produit).
 * Contact connu -> aucune vérification automatique. Numéro signalé -> alerte immédiate à l'écran.
 * Numéro inconnu -> laissé sonner normalement ; la suite (proposition haut-parleur, analyse) est
 * prise en charge par [CallAudioAnalysisService] une fois l'appel décroché.
 */
class TerangaCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val locator = ServiceLocator.get(applicationContext)
        val phoneNumber = callDetails.handle?.schemeSpecificPart.orEmpty()

        val isKnownContact = ContactsLookup.isKnownContact(applicationContext, phoneNumber)
        val isReported = !isKnownContact && phoneNumber.isNotBlank() &&
            locator.reportedNumbersIndex.isNumberReported(phoneNumber)

        CurrentCallSession.start(phoneNumber, isKnownContact, isReported)

        if (isReported) {
            NotificationHelper.showHighRiskAlert(
                applicationContext,
                R.string.alert_high_risk_call_title,
                R.string.alert_high_risk_call_body,
            )
        }

        // On ne bloque jamais automatiquement un appel : on alerte, l'utilisateur décide.
        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()
        respondToCall(callDetails, response)
    }
}
