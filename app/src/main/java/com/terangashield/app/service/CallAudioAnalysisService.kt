package com.terangashield.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.terangashield.app.R
import com.terangashield.app.ServiceLocator
import com.terangashield.app.domain.engine.SpeechToTextEngine
import com.terangashield.app.domain.engine.TranscriptionResult
import com.terangashield.app.domain.engine.VoiceClassificationResult
import com.terangashield.app.domain.model.AppLanguage
import com.terangashield.app.domain.model.EventType
import com.terangashield.app.domain.model.RiskLevel
import com.terangashield.app.domain.scoring.CallScoringInput
import com.terangashield.app.service.call.CurrentCallSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Analyse audio en direct pendant un appel avec un numéro inconnu, une fois le haut-parleur
 * activé. Tourne en service au premier plan (obligatoire pour l'accès micro en arrière-plan).
 * Étapes 5 à 10 du flux "Appels" du prompt produit.
 *
 * Le moteur de transcription ([SpeechToTextEngine]) gère lui-même la capture micro (fenêtres
 * glissantes internes au reconnaisseur) ; ce service se contente de démarrer/arrêter l'écoute
 * selon l'état du haut-parleur et de faire suivre chaque résultat au pipeline de score. Le
 * résultat de chaque fenêtre est écrit dans [CurrentCallSession] au fil de l'eau plutôt que
 * conservé localement : c'est [com.terangashield.app.service.call.TerangaInCallService] qui
 * enregistre l'appel dans l'historique à la fin, pour TOUS les appels (y compris sortants et
 * contacts connus, que ce service n'analyse jamais) — voir son commentaire pour le détail.
 */
class CallAudioAnalysisService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    private lateinit var locator: ServiceLocator

    override fun onCreate() {
        super.onCreate()
        locator = ServiceLocator.get(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ServiceCompat.startForeground(
            this,
            FOREGROUND_NOTIFICATION_ID,
            NotificationHelper.analysisForegroundNotification(applicationContext),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            } else {
                0
            },
        )
        serviceScope.launch { runAnalysisLoop() }
        return START_NOT_STICKY
    }

    private suspend fun runAnalysisLoop() {
        val micConsentGiven = locator.userPreferencesRepository.consentRecord.first().micAnalysisConsentGiven
        val micAnalysisEnabled = locator.userPreferencesRepository.micAnalysisEnabled.first()
        if (!micConsentGiven || !micAnalysisEnabled) return

        val engine = locator.speechToTextEngine
        // Sur les appareils sans reconnaisseur embarqué garanti hors-ligne (avant Android 12),
        // on ne lance pas d'analyse réelle plutôt que de risquer un envoi réseau — voir
        // RealSpeechToTextEngine.isAvailable(). Seul le simulateur de debug reste disponible.
        if (!engine.isAvailable()) return

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return

        val sensitivity = locator.userPreferencesRepository.sensitivity.first()
        locator.riskScorer.updateSensitivity(sensitivity)
        locator.smsRiskAnalyzer.updateSensitivity(sensitivity)
        locator.riskScorer.reset()

        val language = locator.userPreferencesRepository.language.first()
        locator.riskAnalysisEngine.initialize()

        var listeningJob: Job? = null
        while (serviceScope.isActive) {
            val speakerOn = audioManager.isSpeakerphoneOn
            if (speakerOn && listeningJob?.isActive != true) {
                listeningJob = serviceScope.launch { listenAndScore(engine, language) }
            } else if (!speakerOn) {
                listeningJob?.cancel()
                listeningJob = null
            }
            delay(1000)
        }
    }

    private suspend fun listenAndScore(engine: SpeechToTextEngine, language: AppLanguage) {
        engine.listen(language).collect { result ->
            if (result.isFinal && result.text.isNotBlank()) {
                processTranscription(result)
            }
        }
    }

    private suspend fun processTranscription(transcription: TranscriptionResult) {
        val nluResult = locator.riskAnalysisEngine.analyze(transcription.text, transcription.detectedLanguage)
        val breakdown = locator.riskScorer.scoreCallWindow(
            CallScoringInput(
                nluResult = nluResult,
                voiceResult = VoiceClassificationResult(0f, 0f, 0f),
                cascadeScore = 1f,
                detectedLanguage = transcription.detectedLanguage,
            ),
        )
        val riskLevel = locator.riskScorer.riskLevelFor(breakdown.finalScore)
        CurrentCallSession.updateRiskIfHigher(
            riskLevel = riskLevel,
            finalScore = breakdown.finalScore,
            triggeredCategories = breakdown.triggeredCategories,
            detectedLanguage = transcription.detectedLanguage,
            transcriptExcerpt = transcription.text.take(TRANSCRIPT_EXCERPT_MAX_CHARS),
        )

        if (riskLevel == RiskLevel.HIGH && !CurrentCallSession.trustedContactAlreadyNotified) {
            triggerHighRiskAlert(breakdown.finalScore)
        }
    }

    private suspend fun triggerHighRiskAlert(finalScore: Float) {
        CurrentCallSession.trustedContactAlreadyNotified = true
        NotificationHelper.showHighRiskAlert(
            applicationContext,
            R.string.alert_high_risk_call_title,
            R.string.alert_high_risk_call_body,
        )
        vibrate()
        locator.trustedContactNotifier.notifyHighRisk(EventType.CALL, (finalScore * 100).toInt())
    }

    private fun vibrate() {
        val vibrator = ContextCompat.getSystemService(applicationContext, Vibrator::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(600, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(600)
        }
    }

    override fun onDestroy() {
        locator.riskAnalysisEngine.release()
        serviceJob.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 2001
        private const val TRANSCRIPT_EXCERPT_MAX_CHARS = 240

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, CallAudioAnalysisService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, CallAudioAnalysisService::class.java))
        }
    }
}
