package com.terangashield.app.domain.engine

/**
 * Fenêtre glissante de quelques secondes d'audio PCM 16 bits, jamais un flux continu.
 * Voir la contrainte de légèreté : la capture est découpée pour limiter la charge CPU.
 */
data class AudioWindow(
    val pcm16: ShortArray,
    val sampleRateHz: Int,
    val startOffsetMillis: Long,
    val durationMillis: Long,
)
