package com.terangashield.app.debug

import android.content.Context
import com.terangashield.app.R
import com.terangashield.app.ServiceLocator
import com.terangashield.app.data.db.entity.CallRecordEntity
import com.terangashield.app.domain.engine.VoiceClassificationResult
import com.terangashield.app.domain.model.EventType
import com.terangashield.app.domain.model.RiskLevel
import com.terangashield.app.domain.model.ScoreBreakdown
import com.terangashield.app.domain.scoring.CallScoringInput
import com.terangashield.app.service.NotificationHelper
import com.terangashield.app.service.call.CurrentCallSession
import com.terangashield.app.service.sms.SmsProcessor
import kotlinx.coroutines.flow.first

/**
 * Outil de démonstration/QA, uniquement câblé dans les builds debug (voir `SettingsScreen`,
 * gardé derrière `BuildConfig.DEBUG`). Fait rejouer un scénario d'arnaque scripté à travers le
 * vrai pipeline (moteurs mock -> [com.terangashield.app.domain.scoring.RiskScorer] /
 * [com.terangashield.app.domain.scoring.SmsRiskAnalyzer] -> alertes -> SMS au contact de
 * confiance -> persistance) sans dépendre du micro ou d'un appel téléphonique réel — utile
 * puisque le filtre en cascade ne se déclenche jamais sur le silence d'un émulateur.
 */
class ScamSimulator(private val context: Context, private val locator: ServiceLocator) {

    private val silentVoice = VoiceClassificationResult(0f, 0f, 0f)
    private val scamVoice = VoiceClassificationResult(0.4f, 0.3f, 0.2f)

    /** Rejoue le schéma mise en confiance -> urgence fabriquée -> demande sensible jusqu'au risque élevé. */
    suspend fun simulateRiskyCall() {
        val phoneNumber = "+221771234567"
        val language = locator.userPreferencesRepository.language.first()
        val sensitivity = locator.userPreferencesRepository.sensitivity.first()
        locator.riskScorer.updateSensitivity(sensitivity)
        locator.riskScorer.reset()
        CurrentCallSession.start(phoneNumber, isKnownContact = false, isReportedNumber = false)

        val script = listOf(
            "Bonjour, je suis votre conseiller personnel. On m'a chargé de m'occuper spécialement de " +
                "votre dossier. Vous pouvez me faire confiance, je travaille pour votre banque. Je vous " +
                "appelle car nous avons remarqué une activité inhabituelle sur votre compte. Ne vous " +
                "inquiétez pas, je suis là pour vous aider.",
            "Vous devez agir immédiatement sinon votre compte sera bloqué. Il ne vous reste que quelques " +
                "minutes pour régulariser la situation. Si vous ne faites rien maintenant vous allez perdre " +
                "votre argent.",
            "Donnez-moi votre mot de passe pour que je puisse corriger le problème. Confirmez-moi votre " +
                "date de naissance et votre numéro de compte.",
            "C'est urgent, la police va intervenir si vous ne payez pas. J'ai besoin de votre numéro " +
                "de carte pour vérifier votre identité, quel est le code à 6 chiffres affiché sur votre " +
                "téléphone ? Nous sommes le service de sécurité de votre banque.",
        )

        var lastBreakdown: ScoreBreakdown? = null
        var highestRisk = RiskLevel.SAFE
        for (utterance in script) {
            val nluResult = locator.riskAnalysisEngine.analyze(utterance, language)
            val breakdown = locator.riskScorer.scoreCallWindow(
                CallScoringInput(nluResult, scamVoice, cascadeScore = 0.9f, detectedLanguage = language),
            )
            lastBreakdown = breakdown
            val level = locator.riskScorer.riskLevelFor(breakdown.finalScore)
            if (level.ordinal > highestRisk.ordinal) highestRisk = level
        }

        finishSimulatedCall(phoneNumber, highestRisk, lastBreakdown, durationSeconds = 96)
    }

    /** Rejoue une conversation anodine, ne devrait jamais dépasser le niveau "Sûr". */
    suspend fun simulateSafeCall() {
        val phoneNumber = "+221781112233"
        val language = locator.userPreferencesRepository.language.first()
        val sensitivity = locator.userPreferencesRepository.sensitivity.first()
        locator.riskScorer.updateSensitivity(sensitivity)
        locator.riskScorer.reset()
        CurrentCallSession.start(phoneNumber, isKnownContact = false, isReportedNumber = false)

        val script = listOf(
            "Salut, comment vas-tu ? On se voit toujours ce soir pour le dîner ?",
            "Super, à ce soir alors, à 19h comme d'habitude.",
        )

        var lastBreakdown: ScoreBreakdown? = null
        for (utterance in script) {
            val nluResult = locator.riskAnalysisEngine.analyze(utterance, language)
            lastBreakdown = locator.riskScorer.scoreCallWindow(
                CallScoringInput(nluResult, silentVoice, cascadeScore = 0.4f, detectedLanguage = language),
            )
        }

        finishSimulatedCall(phoneNumber, RiskLevel.SAFE, lastBreakdown, durationSeconds = 42)
    }

    private suspend fun finishSimulatedCall(
        phoneNumber: String,
        highestRisk: RiskLevel,
        lastBreakdown: ScoreBreakdown?,
        durationSeconds: Int,
    ) {
        val notified = highestRisk == RiskLevel.HIGH
        if (notified) {
            NotificationHelper.showHighRiskAlert(
                context,
                R.string.alert_high_risk_call_title,
                R.string.alert_high_risk_call_body,
            )
            locator.trustedContactNotifier.notifyHighRisk(EventType.CALL, ((lastBreakdown?.finalScore ?: 0f) * 100).toInt())
        }

        locator.callRepository.insert(
            CallRecordEntity(
                phoneNumber = phoneNumber,
                isKnownContact = false,
                timestampMillis = System.currentTimeMillis(),
                durationSeconds = durationSeconds,
                riskLevel = highestRisk,
                finalScore = lastBreakdown?.finalScore ?: 0f,
                triggeredCategories = lastBreakdown?.triggeredCategories.orEmpty(),
                detectedLanguage = lastBreakdown?.detectedLanguage ?: locator.userPreferencesRepository.language.first(),
                transcriptExcerpt = if (highestRisk == RiskLevel.HIGH) null else "(appel simulé)",
                trustedContactNotified = notified,
            ),
        )
        CurrentCallSession.reset()
    }

    /** Combine plusieurs déclencheurs (demande de code, usurpation de banque, lien) pour un SMS clairement à risque. */
    suspend fun simulateRiskySms() {
        val body = "Alerte sécurité : votre compte sera bloqué. Nous sommes le service de sécurité de " +
            "votre banque. J'ai besoin de votre numéro de carte pour vérifier votre identité. Donnez-moi " +
            "votre mot de passe pour que je puisse corriger le problème. Confirmez ici : " +
            "http://verification-compte-secure.example/confirm"
        SmsProcessor(context).process(sender = "+221701112233", body = body)
    }

    /** Un SMS ordinaire, ne devrait déclencher aucune alerte. */
    suspend fun simulateSafeSms() {
        SmsProcessor(context).process(
            sender = "+221701112233",
            body = "Bonjour, n'oublie pas d'acheter du pain en rentrant ce soir, merci !",
        )
    }
}
