package com.terangashield.app.domain.engine

data class VoiceClassificationResult(
    /** Probabilité que la voix soit synthétique (text-to-speech). */
    val syntheticVoiceScore: Float,
    /** Probabilité d'un débit de parole scripté / mécanique. */
    val scriptedPacingScore: Float,
    /** Probabilité d'un bruit de fond typique de centre d'appel. */
    val callCenterNoiseScore: Float,
) {
    /** Signal indépendant de la langue — combiné une seule fois, pas dupliqué par langue. */
    val combinedScore: Float
        get() = (syntheticVoiceScore * 0.5f + scriptedPacingScore * 0.3f + callCenterNoiseScore * 0.2f)
}

/**
 * Abstraction du classificateur vocal (voix synthétique, débit scripté, bruit de centre d'appel).
 * Implémentation V1 : [com.terangashield.app.domain.engine.mock.MockVoiceClassifierEngine].
 * Le signal est largement indépendant de la langue : un seul modèle pour toutes les langues.
 *
 * Non câblée dans le flux d'appel réel actuel ([com.terangashield.app.service.CallAudioAnalysisService]) :
 * le reconnaisseur vocal système s'approprie le micro pendant l'écoute, donc pas d'accès à
 * l'audio brut en parallèle pour cette analyse complémentaire. Reste utilisable par le
 * simulateur de debug et prête à être branchée si un futur moteur STT expose l'audio brut.
 */
interface VoiceClassifierEngine {
    suspend fun initialize()
    suspend fun classify(window: AudioWindow): VoiceClassificationResult
    fun release()
}
