package com.terangashield.app.domain.scoring

import com.terangashield.app.domain.engine.NluResult
import com.terangashield.app.domain.engine.RiskAnalysisEngine
import com.terangashield.app.domain.model.AppLanguage
import com.terangashield.app.domain.model.DetectionSensitivity
import com.terangashield.app.domain.model.RiskLevel
import com.terangashield.app.domain.model.ScenarioCategory
import com.terangashield.app.domain.model.SmsRiskReason
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Moteur NLU factice pour les tests : renvoie un résultat préprogrammé, sans dépendance Android. */
private class StubRiskAnalysisEngine(private val result: NluResult) : RiskAnalysisEngine {
    override suspend fun initialize() = Unit
    override suspend fun analyze(text: String, language: AppLanguage): NluResult = result
    override fun release() = Unit
}

class SmsRiskAnalyzerTest {

    @Test
    fun `benign message with a link stays safe`() = runBlocking {
        val analyzer = SmsRiskAnalyzer(
            StubRiskAnalysisEngine(NluResult(0f, emptyList(), 0.1f)),
            DetectionSensitivity.MEDIUM,
        )
        val result = analyzer.analyze("Votre colis arrive demain : http://livraison.example/track", AppLanguage.FRENCH)
        assertEquals(RiskLevel.SAFE, result.riskLevel)
        assertEquals(SmsRiskReason.NONE, result.reason)
    }

    @Test
    fun `otp request is flagged with the right reason`() = runBlocking {
        val analyzer = SmsRiskAnalyzer(
            StubRiskAnalysisEngine(NluResult(0.85f, listOf(ScenarioCategory.SENSITIVE_INFO_REQUEST), 0.6f)),
            DetectionSensitivity.MEDIUM,
        )
        val result = analyzer.analyze("Renvoyez-nous le code reçu par SMS pour confirmer.", AppLanguage.FRENCH)
        assertEquals(RiskLevel.HIGH, result.riskLevel)
        assertEquals(SmsRiskReason.OTP_REQUEST, result.reason)
    }

    @Test
    fun `suspicious link combined with risk signal is flagged`() = runBlocking {
        val analyzer = SmsRiskAnalyzer(
            StubRiskAnalysisEngine(NluResult(0.6f, listOf(ScenarioCategory.FABRICATED_URGENCY), 0.5f)),
            DetectionSensitivity.MEDIUM,
        )
        val result = analyzer.analyze("Agissez maintenant : www.suspect-example.tld/verify", AppLanguage.FRENCH)
        assertEquals(SmsRiskReason.SUSPICIOUS_LINK, result.reason)
        assertEquals("www.suspect-example.tld/verify", result.suspiciousLinkUrl)
    }

    @Test
    fun `no link means no suspicious link reported`() = runBlocking {
        val analyzer = SmsRiskAnalyzer(
            StubRiskAnalysisEngine(NluResult(0.2f, emptyList(), 0.2f)),
            DetectionSensitivity.MEDIUM,
        )
        val result = analyzer.analyze("Bonjour, comment vas-tu ?", AppLanguage.FRENCH)
        assertNull(result.suspiciousLinkUrl)
    }

    @Test
    fun `higher sensitivity flags a borderline message that low sensitivity would not`() = runBlocking {
        val nlu = NluResult(0.35f, listOf(ScenarioCategory.INSTITUTION_IMPERSONATION), 0.4f)
        val highSensitivity = SmsRiskAnalyzer(StubRiskAnalysisEngine(nlu), DetectionSensitivity.HIGH)
            .analyze("message limite", AppLanguage.FRENCH)
        val lowSensitivity = SmsRiskAnalyzer(StubRiskAnalysisEngine(nlu), DetectionSensitivity.LOW)
            .analyze("message limite", AppLanguage.FRENCH)
        assertTrue(highSensitivity.riskLevel.ordinal >= lowSensitivity.riskLevel.ordinal)
    }
}
