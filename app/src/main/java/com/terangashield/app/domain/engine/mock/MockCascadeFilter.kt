package com.terangashield.app.domain.engine.mock

import com.terangashield.app.domain.engine.AudioWindow
import com.terangashield.app.domain.engine.CascadeFilter
import kotlin.math.abs
import kotlin.math.min

/**
 * Filtre grossier factice : se contente de vérifier qu'il y a de la voix (amplitude au-dessus
 * d'un seuil) pour décider si le pipeline complet doit se déclencher. Le vrai modèle sera un
 * classifieur très léger dédié, entraîné pour repérer un doute, pas juste la présence de voix.
 */
class MockCascadeFilter : CascadeFilter {
    override suspend fun quickScore(window: AudioWindow): Float {
        if (window.pcm16.isEmpty()) return 0f
        val meanAbs = window.pcm16.map { abs(it.toInt()) }.average()
        return min(meanAbs / 15000.0, 1.0).toFloat()
    }
}
