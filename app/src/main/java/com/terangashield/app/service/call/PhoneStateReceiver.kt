package com.terangashield.app.service.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.terangashield.app.service.CallAudioAnalysisService

/**
 * Détecte le décroché (OFFHOOK) et la fin d'appel (IDLE) pour démarrer/arrêter l'analyse audio.
 * Analyse tous les appels, y compris les contacts enregistrés — un contact compromis qui demande
 * un code par téléphone est un schéma d'arnaque réel, exempter les contacts (choix initial du
 * cahier des charges) créait un angle mort.
 */
class PhoneStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        when (state) {
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                if (CurrentCallSession.phoneNumber != null) {
                    CallAudioAnalysisService.start(context)
                }
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                CallAudioAnalysisService.stop(context)
            }
        }
    }
}
