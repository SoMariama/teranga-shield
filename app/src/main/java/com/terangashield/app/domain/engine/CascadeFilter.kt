package com.terangashield.app.domain.engine

/**
 * Premier filtre grossier, très léger, qui tourne en continu sur chaque fenêtre audio.
 * Le pipeline complet (transcription + NLU + analyse vocale) ne se déclenche que si ce filtre
 * remonte un doute — voir la contrainte de légèreté / absence de surchauffe.
 */
interface CascadeFilter {
    /** @return un niveau de doute entre 0 (rien à signaler) et 1 (déclenche le pipeline complet). */
    suspend fun quickScore(window: AudioWindow): Float

    companion object {
        /** Au-delà de ce doute, le pipeline complet (STT + NLU + vocal) se déclenche. */
        const val DOUBT_THRESHOLD = 0.35f
    }
}
