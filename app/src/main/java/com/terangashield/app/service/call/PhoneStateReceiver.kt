package com.terangashield.app.service.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.terangashield.app.service.CallAudioAnalysisService

/**
 * Détecte le décroché (OFFHOOK) et la fin d'appel (IDLE) pour démarrer/arrêter l'analyse audio.
 * Ne déclenche l'analyse que pour les numéros inconnus (les contacts ne sont jamais analysés).
 */
class PhoneStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        when (state) {
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                val session = CurrentCallSession
                if (session.phoneNumber != null && !session.isKnownContact) {
                    CallAudioAnalysisService.start(context)
                }
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                CallAudioAnalysisService.stop(context)
            }
        }
    }
}
