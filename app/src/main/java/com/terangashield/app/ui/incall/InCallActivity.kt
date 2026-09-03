package com.terangashield.app.ui.incall

import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.VideoProfile
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.terangashield.app.TerangaShieldApp
import com.terangashield.app.domain.model.AppLanguage
import com.terangashield.app.service.call.CallBridge
import com.terangashield.app.service.call.CurrentCallSession
import com.terangashield.app.ui.theme.TerangaShieldTheme
import com.terangashield.app.ui.util.ProvideAppLocale

/**
 * Écran d'appel (entrant / en cours), lancé par [com.terangashield.app.service.call.TerangaInCallService]
 * quand Teranga Shield est le téléphone par défaut. Activity dédiée, distincte de `MainActivity`,
 * pour pouvoir apparaître par-dessus l'écran verrouillé comme tout numéroteur.
 */
class InCallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        val locator = (application as TerangaShieldApp).serviceLocator

        setContent {
            val language by locator.userPreferencesRepository.language
                .collectAsStateWithLifecycle(initialValue = AppLanguage.FRENCH)
            ProvideAppLocale(language) {
                TerangaShieldTheme {
                    InCallRoot(onFinish = { finish() })
                }
            }
        }
    }
}

@Composable
private fun InCallRoot(onFinish: () -> Unit) {
    val call by CallBridge.call.collectAsStateWithLifecycle()
    val callState by CallBridge.callState.collectAsStateWithLifecycle()
    val audioState by CallBridge.audioState.collectAsStateWithLifecycle()

    val currentCall = call
    LaunchedEffect(currentCall, callState) {
        if (currentCall == null || callState == Call.STATE_DISCONNECTED) onFinish()
    }
    if (currentCall == null) return

    val phoneNumber = currentCall.details.handle?.schemeSpecificPart.orEmpty()
    // CurrentCallSession n'est renseigné (et fiable) que pour les appels entrants, filtrés en
    // amont par TerangaCallScreeningService — pour un appel sortant (composé depuis l'app), on
    // n'affiche ni badge de contact ni badge de risque plutôt que de montrer un état obsolète.
    val isIncoming = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
        currentCall.details.callDirection == Call.Details.DIRECTION_INCOMING
    val isKnownContact = isIncoming && CurrentCallSession.isKnownContact
    val isReportedNumber = isIncoming && CurrentCallSession.isReportedNumber

    when (callState) {
        Call.STATE_RINGING -> IncomingCallScreen(
            phoneNumber = phoneNumber,
            isKnownContact = isKnownContact,
            isReportedNumber = isReportedNumber,
            onAnswer = { currentCall.answer(VideoProfile.STATE_AUDIO_ONLY) },
            onDecline = { currentCall.reject(false, null) },
        )
        else -> ActiveCallScreen(
            phoneNumber = phoneNumber,
            isKnownContact = isKnownContact,
            isConnected = callState == Call.STATE_ACTIVE,
            isMuted = audioState?.isMuted ?: false,
            isSpeakerOn = audioState?.route == CallAudioState.ROUTE_SPEAKER,
            onToggleMute = { CallBridge.setMuted(!(audioState?.isMuted ?: false)) },
            onToggleSpeaker = { CallBridge.setSpeakerOn(audioState?.route != CallAudioState.ROUTE_SPEAKER) },
            onHangup = { currentCall.disconnect() },
        )
    }
}
