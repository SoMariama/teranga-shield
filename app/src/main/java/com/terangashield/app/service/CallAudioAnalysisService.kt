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
import com.terangashield.app.data.db.entity.CallRecordEntity
import com.terangashield.app.domain.engine.AudioWindow
import com.terangashield.app.domain.engine.CascadeFilter
import com.terangashield.app.domain.engine.DetectionMode
import com.terangashield.app.domain.engine.HardwareCapabilityDetector
import com.terangashield.app.domain.model.EventType
import com.terangashield.app.domain.model.RiskLevel
import com.terangashield.app.domain.model.ScoreBreakdown
import com.terangashield.app.domain.scoring.CallScoringInput
import com.terangashield.app.service.call.CurrentCallSession
import com.terangashield.app.service.call.SlidingWindowAudioCapture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Analyse audio en direct pendant un appel avec un numéro inconnu, une fois le haut-parleur
 * activé. Tourne en service au premier plan (obligatoire pour l'accès micro en arrière-plan).
 * Étapes 5 à 10 du flux "Appels" du prompt produit.
 */
class CallAudioAnalysisService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    private lateinit var locator: ServiceLocator
    private var lastBreakdown: ScoreBreakdown? = null
    private var lastTranscriptExcerpt: String? = null
    private var highestRiskLevel: RiskLevel = RiskLevel.SAFE

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

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        val capture = SlidingWindowAudioCapture(applicationContext)
        if (!capture.hasMicPermission()) return

        val sensitivity = locator.userPreferencesRepository.sensitivity.first()
        locator.riskScorer.updateSensitivity(sensitivity)
        locator.smsRiskAnalyzer.updateSensitivity(sensitivity)
        locator.riskScorer.reset()

        val detectionMode = HardwareCapabilityDetector.detect(applicationContext)
        locator.speechToTextEngine.initialize(null)
        locator.riskAnalysisEngine.initialize()
        if (detectionMode == DetectionMode.FULL) locator.voiceClassifierEngine.initialize()

        capture.captureWindows().collect { window ->
            if (!audioManager.isSpeakerphoneOn) return@collect
            processWindow(window, locator.cascadeFilter, detectionMode)
        }
    }

    private suspend fun processWindow(window: AudioWindow, cascade: CascadeFilter, mode: DetectionMode) {
        val doubt = cascade.quickScore(window)
        if (doubt < CascadeFilter.DOUBT_THRESHOLD) return

        val transcription = locator.speechToTextEngine.transcribe(window)
        if (transcription.text.isBlank()) return

        val nluResult = locator.riskAnalysisEngine.analyze(transcription.text, transcription.detectedLanguage)
        val voiceResult = if (mode == DetectionMode.FULL) {
            locator.voiceClassifierEngine.classify(window)
        } else {
            com.terangashield.app.domain.engine.VoiceClassificationResult(0f, 0f, 0f)
        }

        val breakdown = locator.riskScorer.scoreCallWindow(
            CallScoringInput(
                nluResult = nluResult,
                voiceResult = voiceResult,
                cascadeScore = doubt,
                detectedLanguage = transcription.detectedLanguage,
            ),
        )
        lastBreakdown = breakdown
        lastTranscriptExcerpt = transcription.text.take(TRANSCRIPT_EXCERPT_MAX_CHARS)

        val riskLevel = locator.riskScorer.riskLevelFor(breakdown.finalScore)
        if (riskLevel.ordinal > highestRiskLevel.ordinal) highestRiskLevel = riskLevel

        if (riskLevel == RiskLevel.HIGH && !CurrentCallSession.trustedContactAlreadyNotified) {
            triggerHighRiskAlert(breakdown)
        }
    }

    private suspend fun triggerHighRiskAlert(breakdown: ScoreBreakdown) {
        CurrentCallSession.trustedContactAlreadyNotified = true
        NotificationHelper.showHighRiskAlert(
            applicationContext,
            R.string.alert_high_risk_call_title,
            R.string.alert_high_risk_call_body,
        )
        vibrate()
        locator.trustedContactNotifier.notifyHighRisk(EventType.CALL, (breakdown.finalScore * 100).toInt())
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
        persistCallRecord()
        locator.speechToTextEngine.release()
        locator.riskAnalysisEngine.release()
        locator.voiceClassifierEngine.release()
        serviceJob.cancel()
        CurrentCallSession.reset()
        super.onDestroy()
    }

    private fun persistCallRecord() {
        val session = CurrentCallSession
        val phoneNumber = session.phoneNumber ?: return
        val durationSeconds = if (session.callStartMillis > 0) {
            ((System.currentTimeMillis() - session.callStartMillis) / 1000).toInt()
        } else {
            0
        }
        val breakdown = lastBreakdown
        val finalScore = breakdown?.finalScore ?: 0f
        val transcript = if (highestRiskLevel == RiskLevel.HIGH) null else lastTranscriptExcerpt

        serviceScope.launch(Dispatchers.IO) {
            locator.callRepository.insert(
                CallRecordEntity(
                    phoneNumber = phoneNumber,
                    isKnownContact = session.isKnownContact,
                    timestampMillis = if (session.callStartMillis > 0) session.callStartMillis else System.currentTimeMillis(),
                    durationSeconds = durationSeconds,
                    riskLevel = highestRiskLevel,
                    finalScore = finalScore,
                    triggeredCategories = breakdown?.triggeredCategories.orEmpty(),
                    detectedLanguage = breakdown?.detectedLanguage ?: com.terangashield.app.domain.model.AppLanguage.FRENCH,
                    transcriptExcerpt = transcript,
                    trustedContactNotified = session.trustedContactAlreadyNotified,
                ),
            )
        }
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
