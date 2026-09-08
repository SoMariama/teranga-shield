package com.terangashield.app.ui.incall

import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.VideoProfile
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.terangashield.app.ui.util.rememberContactNames

/**
 * Écran d'appel (entrant / en cours), lancé par [com.terangashield.app.service.call.TerangaInCallService]
 * quand Teranga Shield est le téléphone par défaut. Activity dédiée, distincte de `MainActivity`,
 * pour pouvoir apparaître par-dessus l'écran verrouillé comme tout numéroteur.
 */
class InCallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
    val phoneNumber by CallBridge.phoneNumber.collectAsStateWithLifecycle()

    val currentCall = call
    LaunchedEffect(currentCall, callState) {
        if (currentCall == null || callState == Call.STATE_DISCONNECTED) onFinish()
    }
    if (currentCall == null) return

    // Recherche de contact en direct sur le numéro affiché, indépendante du sens de l'appel — un
    // appel sortant vers un contact enregistré doit afficher son nom aussi bien qu'un entrant
    // (CurrentCallSession.isKnownContact était auparavant ignoré pour les sortants "pour ne pas
    // montrer un état obsolète", mais TerangaInCallService.ensureSessionStarted() le renseigne en
    // réalité de façon fiable et synchrone dès le début de l'appel, entrant comme sortant).
    val isIncoming = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
        currentCall.details.callDirection == Call.Details.DIRECTION_INCOMING
    val contactNames = rememberContactNames(listOfNotNull(phoneNumber))
    val contactName = phoneNumber?.let { contactNames[it] }
    // isReportedNumber reste réservé aux entrants : seul TerangaCallScreeningService vérifie la
    // base des numéros signalés, et uniquement pour un appel entrant.
    val isReportedNumber = isIncoming && CurrentCallSession.isReportedNumber
    val isSpeakerOn = audioState?.route == CallAudioState.ROUTE_SPEAKER
    // Proposition d'activer le haut-parleur pour vérification (flux "Appels", étape 4 du prompt
    // produit) : uniquement pour un numéro inconnu entrant, tant que le haut-parleur n'est pas
    // déjà activé.
    val showSpeakerPrompt = isIncoming && contactName == null && !isSpeakerOn

    when (callState) {
        Call.STATE_RINGING -> IncomingCallScreen(
            phoneNumber = phoneNumber ?: "",
            contactName = contactName,
            isReportedNumber = isReportedNumber,
            onAnswer = { currentCall.answer(VideoProfile.STATE_AUDIO_ONLY) },
            onDecline = { currentCall.reject(false, null) },
        )
        else -> ActiveCallScreen(
            phoneNumber = phoneNumber ?: "",
            contactName = contactName,
            isConnected = callState == Call.STATE_ACTIVE,
            isMuted = audioState?.isMuted ?: false,
            isSpeakerOn = isSpeakerOn,
            showSpeakerPrompt = showSpeakerPrompt,
            onToggleMute = { CallBridge.setMuted(!(audioState?.isMuted ?: false)) },
            onToggleSpeaker = { CallBridge.setSpeakerOn(!isSpeakerOn) },
            onHangup = { currentCall.disconnect() },
        )
    }
}
