package com.terangashield.app.service.call

import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Pont entre [com.terangashield.app.service.call.TerangaInCallService] (composant système lié
 * par Telecom) et [com.terangashield.app.ui.incall.InCallActivity] (UI Compose de l'écran
 * d'appel) : deux composants Android distincts qui ne peuvent pas se passer directement un
 * objet [Call], d'où ce singleton — même schéma que [CurrentCallSession] pour le filtrage.
 */
object CallBridge {
    private val _call = MutableStateFlow<Call?>(null)
    val call: StateFlow<Call?> = _call

    private val _callState = MutableStateFlow(Call.STATE_DISCONNECTED)
    val callState: StateFlow<Int> = _callState

    private val _audioState = MutableStateFlow<CallAudioState?>(null)
    val audioState: StateFlow<CallAudioState?> = _audioState

    // Le numéro (et le reste de Call.Details) n'est pas toujours disponible dès onCallAdded —
    // Android le complète parfois après coup via onDetailsChanged. Sans écouter ce callback,
    // l'UI lisait details.handle une seule fois et pouvait rester bloquée sur un numéro vide.
    private val _phoneNumber = MutableStateFlow<String?>(null)
    val phoneNumber: StateFlow<String?> = _phoneNumber

    private var inCallService: InCallService? = null

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, newState: Int) {
            _callState.value = newState
        }

        override fun onDetailsChanged(call: Call, details: Call.Details) {
            details.handle?.schemeSpecificPart?.let { _phoneNumber.value = it }
        }
    }

    fun setCall(call: Call?) {
        _call.value?.unregisterCallback(callCallback)
        _call.value = call
        if (call != null) {
            call.registerCallback(callCallback)
            _callState.value = call.state
            _phoneNumber.value = call.details.handle?.schemeSpecificPart
        } else {
            _callState.value = Call.STATE_DISCONNECTED
            _phoneNumber.value = null
        }
    }

    fun attachService(service: InCallService) {
        inCallService = service
    }

    fun detachService(service: InCallService) {
        if (inCallService === service) inCallService = null
    }

    fun onAudioStateChanged(state: CallAudioState) {
        _audioState.value = state
    }

    fun setMuted(muted: Boolean) {
        inCallService?.setMuted(muted)
    }

    fun setSpeakerOn(on: Boolean) {
        inCallService?.setAudioRoute(
            if (on) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_EARPIECE,
        )
    }
}
