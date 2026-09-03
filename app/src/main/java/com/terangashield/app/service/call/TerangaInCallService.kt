package com.terangashield.app.service.call

import android.content.Intent
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import com.terangashield.app.ui.incall.InCallActivity

/**
 * Fournit l'écran d'appel (rôle "téléphone par défaut", `ROLE_DIALER`) : Android confie ici
 * chaque appel — entrant ou sortant, quelle que soit l'app qui l'a initié — dès que ce rôle est
 * accordé. Le filtrage avant sonnerie reste séparé, voir [TerangaCallScreeningService] ;
 * les deux rôles cohabitent normalement sur un même appareil.
 */
class TerangaInCallService : InCallService() {

    override fun onCreate() {
        super.onCreate()
        CallBridge.attachService(this)
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        CallBridge.setCall(call)
        startActivity(
            Intent(this, InCallActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        if (CallBridge.call.value === call) {
            CallBridge.setCall(null)
        }
    }

    override fun onCallAudioStateChanged(audioState: CallAudioState) {
        super.onCallAudioStateChanged(audioState)
        CallBridge.onAudioStateChanged(audioState)
    }

    override fun onDestroy() {
        CallBridge.detachService(this)
        super.onDestroy()
    }
}
