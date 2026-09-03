package com.terangashield.app.data.bloom

import android.content.Context
import java.io.File
import java.io.IOException

/**
 * Charge la base compacte de numéros signalés en mémoire au démarrage, et applique les mises à
 * jour incrémentales (delta) reçues par [com.terangashield.app.worker.ReportedNumbersUpdateWorker].
 * Fonctionne entièrement hors ligne : l'asset embarqué au build sert de graine initiale.
 */
class ReportedNumbersIndex(private val context: Context) {

    @Volatile
    private var filter: BloomFilter = loadPersistedOrSeed()

    fun isNumberReported(phoneNumber: String): Boolean = filter.mightContain(normalize(phoneNumber))

    /** Fusionne un delta téléchargé (uniquement si une connexion existe, jamais bloquant). */
    @Synchronized
    fun applyDelta(newlyReportedNumbers: List<String>) {
        newlyReportedNumbers.forEach { filter.add(normalize(it)) }
        persist()
    }

    private fun loadPersistedOrSeed(): BloomFilter {
        val persisted = persistedFile()
        if (persisted.exists()) {
            return runCatching { BloomFilter.fromByteArray(persisted.readBytes()) }.getOrElse { seedFromAssetOrEmpty() }
        }
        return seedFromAssetOrEmpty()
    }

    private fun seedFromAssetOrEmpty(): BloomFilter =
        try {
            context.assets.open(SEED_ASSET_NAME).use { BloomFilter.fromByteArray(it.readBytes()) }
        } catch (e: IOException) {
            BloomFilter.empty()
        }

    private fun persist() {
        runCatching { persistedFile().writeBytes(filter.toByteArray()) }
    }

    private fun persistedFile(): File = File(context.filesDir, PERSISTED_FILE_NAME)

    private fun normalize(phoneNumber: String): String = phoneNumber.filter { it.isDigit() || it == '+' }

    companion object {
        private const val SEED_ASSET_NAME = "reported_numbers.bloom"
        private const val PERSISTED_FILE_NAME = "reported_numbers_index.bin"
    }
}
