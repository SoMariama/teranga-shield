package com.terangashield.app.domain.engine.mock

import com.terangashield.app.domain.engine.AudioWindow
import com.terangashield.app.domain.engine.VoiceClassificationResult
import com.terangashield.app.domain.engine.VoiceClassifierEngine
import kotlin.math.abs
import kotlin.math.min

/**
 * Implémentation factice du classificateur vocal : dérive un score déterministe et plausible
 * à partir de statistiques simples du signal (amplitude), sans aucun vrai modèle de classification.
 */
class MockVoiceClassifierEngine : VoiceClassifierEngine {

    override suspend fun initialize() = Unit

    override suspend fun classify(window: AudioWindow): VoiceClassificationResult {
        if (window.pcm16.isEmpty()) {
            return VoiceClassificationResult(0f, 0f, 0f)
        }
        val meanAbs = window.pcm16.map { abs(it.toInt()) }.average()
        val variance = window.pcm16.map { (abs(it.toInt()) - meanAbs) * (abs(it.toInt()) - meanAbs) }.average()

        // Une voix synthétique tend à avoir une amplitude très régulière (faible variance relative).
        val regularity = if (meanAbs > 0) 1.0 - min(variance / (meanAbs * meanAbs + 1.0), 1.0) else 0.0

        return VoiceClassificationResult(
            syntheticVoiceScore = regularity.toFloat() * 0.6f,
            scriptedPacingScore = regularity.toFloat() * 0.4f,
            callCenterNoiseScore = min(meanAbs / 20000.0, 0.3).toFloat(),
        )
    }

    override fun release() = Unit
}
