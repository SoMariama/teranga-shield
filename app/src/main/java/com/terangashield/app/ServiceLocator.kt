package com.terangashield.app

import android.content.Context
import com.terangashield.app.data.bloom.ReportedNumbersIndex
import com.terangashield.app.data.db.TerangaDatabase
import com.terangashield.app.data.notification.TrustedContactNotifier
import com.terangashield.app.data.prefs.UserPreferencesRepository
import com.terangashield.app.data.remote.NoOpReportedNumbersRemoteDataSource
import com.terangashield.app.data.remote.ReportedNumbersRemoteDataSource
import com.terangashield.app.data.repository.CallRepository
import com.terangashield.app.data.repository.ReportRepository
import com.terangashield.app.data.repository.SmsRepository
import com.terangashield.app.data.sms.SmsHistoryImporter
import com.terangashield.app.debug.ScamSimulator
import com.terangashield.app.domain.engine.CascadeFilter
import com.terangashield.app.domain.engine.RiskAnalysisEngine
import com.terangashield.app.domain.engine.SpeechToTextEngine
import com.terangashield.app.domain.engine.VoiceClassifierEngine
import com.terangashield.app.domain.engine.mock.MockCascadeFilter
import com.terangashield.app.domain.engine.mock.MockRiskAnalysisEngine
import com.terangashield.app.domain.engine.mock.MockSpeechToTextEngine
import com.terangashield.app.domain.engine.mock.MockVoiceClassifierEngine
import com.terangashield.app.domain.engine.real.RealSpeechToTextEngine
import com.terangashield.app.domain.model.DetectionSensitivity
import com.terangashield.app.domain.patterns.PatternRepository
import com.terangashield.app.domain.scoring.RiskScorer
import com.terangashield.app.domain.scoring.SmsRiskAnalyzer

/**
 * Localisateur de services manuel (pas de Hilt) : garde le build léger et rapide, cohérent avec
 * la contrainte de légèreté du produit. Toutes les dépendances sont interfacées
 * ([SpeechToTextEngine], [RiskAnalysisEngine], [VoiceClassifierEngine]) pour permettre de
 * remplacer les implémentations mock par les vrais modèles ML sans toucher au reste du code.
 */
class ServiceLocator private constructor(context: Context) {

    val database: TerangaDatabase = TerangaDatabase.getInstance(context)
    val userPreferencesRepository = UserPreferencesRepository(context)
    val patternRepository = PatternRepository(context)
    val reportedNumbersIndex = ReportedNumbersIndex(context)
    val reportedNumbersRemoteDataSource: ReportedNumbersRemoteDataSource = NoOpReportedNumbersRemoteDataSource()

    // Reconnaisseur vocal réel si l'appareil le garantit hors ligne (Android 12+), sinon repli
    // sur le mock : voir RealSpeechToTextEngine.isAvailable() et CallAudioAnalysisService.
    private val realSpeechToTextEngine = RealSpeechToTextEngine(context)
    val speechToTextEngine: SpeechToTextEngine =
        if (realSpeechToTextEngine.isAvailable()) realSpeechToTextEngine else MockSpeechToTextEngine()
    val riskAnalysisEngine: RiskAnalysisEngine = MockRiskAnalysisEngine(patternRepository)
    val voiceClassifierEngine: VoiceClassifierEngine = MockVoiceClassifierEngine()
    val cascadeFilter: CascadeFilter = MockCascadeFilter()

    val callRepository = CallRepository(database.callDao())
    val smsRepository = SmsRepository(database.smsDao())
    val reportRepository = ReportRepository(database.reportDao())

    val trustedContactNotifier = TrustedContactNotifier(context, userPreferencesRepository)
    val smsHistoryImporter = SmsHistoryImporter(context, smsRepository, userPreferencesRepository)

    val riskScorer = RiskScorer(DetectionSensitivity.MEDIUM)
    val smsRiskAnalyzer = SmsRiskAnalyzer(riskAnalysisEngine, DetectionSensitivity.MEDIUM)

    /** Outil de démonstration/QA (builds debug uniquement, voir `SettingsScreen`) : voir [ScamSimulator]. */
    val scamSimulator = ScamSimulator(context, this)

    companion object {
        @Volatile private var instance: ServiceLocator? = null

        fun get(context: Context): ServiceLocator =
            instance ?: synchronized(this) {
                instance ?: ServiceLocator(context.applicationContext).also { instance = it }
            }
    }
}
