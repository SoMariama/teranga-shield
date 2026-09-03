package com.terangashield.app.service.call

import android.content.Intent
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import com.terangashield.app.ServiceLocator
import com.terangashield.app.data.db.entity.CallRecordEntity
import com.terangashield.app.domain.model.AppLanguage
import com.terangashield.app.ui.incall.InCallActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Fournit l'écran d'appel (rôle "téléphone par défaut", `ROLE_DIALER`) : Android confie ici
 * chaque appel — entrant ou sortant, quelle que soit l'app qui l'a initié — dès que ce rôle est
 * accordé. Le filtrage avant sonnerie reste séparé, voir `TerangaCallScreeningService` ; les
 * deux rôles cohabitent normalement sur un même appareil.
 *
 * Point d'enregistrement UNIQUE de l'historique des appels (voir [persistCallRecord]) : couvre
 * aussi bien les appels entrants inconnus analysés par `CallAudioAnalysisService` que les appels
 * sortants et les appels avec des contacts connus, qu'aucun autre composant ne journalisait
 * auparavant.
 */
class TerangaInCallService : InCallService() {

    override fun onCreate() {
        super.onCreate()
        CallBridge.attachService(this)
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        // Une exception ici ferait planter le service — Telecom coupe alors l'appel faute
        // d'interface pour l'afficher. On isole donc tout ce qui n'est pas strictement
        // necessaire pour que l'appel aboutisse malgre tout si un detail annexe echoue.
        runCatching { ensureSessionStarted(call) }
        CallBridge.setCall(call)
        runCatching {
            startActivity(
                Intent(this, InCallActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
            )
        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        if (CallBridge.call.value === call) {
            CallBridge.setCall(null)
        }
        runCatching { persistCallRecord(call) }
    }

    override fun onCallAudioStateChanged(audioState: CallAudioState) {
        super.onCallAudioStateChanged(audioState)
        CallBridge.onAudioStateChanged(audioState)
    }

    /**
     * TerangaCallScreeningService.onScreenCall() n'est invoqué que pour les appels ENTRANTS —
     * un appel sortant (composé depuis le clavier de l'app) n'a donc jamais initialisé
     * [CurrentCallSession]. On la démarre ici si besoin, pour que l'enregistrement final dispose
     * toujours d'un numéro et d'un statut de contact, quel que soit le sens de l'appel.
     */
    private fun ensureSessionStarted(call: Call) {
        if (CurrentCallSession.phoneNumber != null) return
        val number = call.details.handle?.schemeSpecificPart.orEmpty()
        val isContact = ContactsLookup.isKnownContact(applicationContext, number)
        CurrentCallSession.start(number, isKnownContact = isContact, isReportedNumber = false)
    }

    private fun persistCallRecord(call: Call) {
        val session = CurrentCallSession
        val phoneNumber = call.details.handle?.schemeSpecificPart?.ifBlank { null }
            ?: session.phoneNumber
            ?: return

        val connectTimeMillis = call.details.connectTimeMillis
        val durationSeconds = if (connectTimeMillis > 0) {
            ((System.currentTimeMillis() - connectTimeMillis) / 1000).toInt().coerceAtLeast(0)
        } else {
            0
        }
        val timestampMillis = session.callStartMillis.takeIf { it > 0 } ?: System.currentTimeMillis()

        val riskLevel = session.riskLevel
        val finalScore = session.finalScore
        val triggeredCategories = session.triggeredCategories
        val detectedLanguage = session.detectedLanguage ?: AppLanguage.FRENCH
        // Jamais la transcription verbatim pour un risque élevé, voir contrainte de confidentialité.
        val transcriptExcerpt = if (riskLevel == com.terangashield.app.domain.model.RiskLevel.HIGH) {
            null
        } else {
            session.transcriptExcerpt
        }
        val isKnownContact = session.isKnownContact
        val trustedContactNotified = session.trustedContactAlreadyNotified

        val locator = ServiceLocator.get(applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            locator.callRepository.insert(
                CallRecordEntity(
                    phoneNumber = phoneNumber,
                    isKnownContact = isKnownContact,
                    timestampMillis = timestampMillis,
                    durationSeconds = durationSeconds,
                    riskLevel = riskLevel,
                    finalScore = finalScore,
                    triggeredCategories = triggeredCategories,
                    detectedLanguage = detectedLanguage,
                    transcriptExcerpt = transcriptExcerpt,
                    trustedContactNotified = trustedContactNotified,
                ),
            )
        }
        CurrentCallSession.reset()
    }

    override fun onDestroy() {
        CallBridge.detachService(this)
        super.onDestroy()
    }
}
