package com.terangashield.app.domain.engine

import android.app.ActivityManager
import android.content.Context
import android.os.Build

enum class DetectionMode {
    /** NPU exploitable détecté : pipeline complet (transcription + NLU + analyse vocale). */
    FULL,

    /** Pas de NPU exploitable ou appareil bas de gamme : uniquement l'analyse du texte transcrit. */
    DEGRADED,
}

/**
 * Détection de capacité matérielle au premier lancement, mémorisée pour éviter de la refaire
 * à chaque appel. Heuristique : disponibilité NNAPI (API 27+) et appareil non "low RAM".
 * Une vraie détection NNAPI (liste des devices exploitables) est faite au chargement du modèle
 * TFLite réel ; ceci est un pré-filtre rapide utilisable avant tout chargement de modèle.
 */
object HardwareCapabilityDetector {
    fun detect(context: Context): DetectionMode {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val isLowRam = activityManager?.isLowRamDevice ?: true
        val hasNnApi = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 // NNAPI disponible depuis API 27
        return if (hasNnApi && !isLowRam) DetectionMode.FULL else DetectionMode.DEGRADED
    }
}
