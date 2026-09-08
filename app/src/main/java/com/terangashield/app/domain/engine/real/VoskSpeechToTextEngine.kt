package com.terangashield.app.domain.engine.real

import android.content.Context
import com.terangashield.app.R
import com.terangashield.app.domain.engine.SpeechToTextEngine
import com.terangashield.app.domain.engine.TranscriptionResult
import com.terangashield.app.domain.model.AppLanguage
import com.terangashield.app.service.NotificationHelper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Reconnaissance vocale hors-ligne via Vosk (modèle embarqué dans les assets), remplace le
 * SpeechRecognizer système d'Android : sur appareil de test réel, celui-ci ne captait jamais rien
 * pendant un appel (micro + haut-parleur + appel actif simultanés) alors que la logique de
 * détection elle-même fonctionnait bien (confirmé via les SMS). Vosk pilote directement
 * l'AudioRecord (source VOICE_RECOGNITION) au lieu de dépendre d'un service système dont on ne
 * contrôle ni la capture ni le traitement (suppression d'écho, etc.).
 *
 * Toute erreur d'initialisation ou de capture est remontée dans la notification d'analyse
 * (au lieu d'être avalée silencieusement) : sans accès à un débogueur sur l'appareil de test,
 * c'est le seul moyen de savoir POURQUOI rien ne s'affiche jamais dans "Entendu : « ... »".
 *
 * Un seul modèle est embarqué pour l'instant : français (~65 Mo, voir assets/vosk-model-fr).
 * Anglais et russe pourront être ajoutés de la même façon plus tard si besoin — voir [isAvailable].
 */
class VoskSpeechToTextEngine(private val context: Context) : SpeechToTextEngine {

    @Volatile private var cachedModel: Model? = null

    override fun isAvailable(language: AppLanguage): Boolean = language == AppLanguage.FRENCH

    override fun listen(languageHint: AppLanguage?): Flow<TranscriptionResult> = callbackFlow {
        val language = languageHint ?: AppLanguage.FRENCH
        if (!isAvailable(language)) {
            close()
            return@callbackFlow
        }

        val model = try {
            loadModel()
        } catch (e: Exception) {
            reportError("unpack: ${e.message}")
            close()
            return@callbackFlow
        }

        val recognizer = try {
            Recognizer(model, SAMPLE_RATE)
        } catch (e: Exception) {
            reportError("Recognizer: ${e.message}")
            close()
            return@callbackFlow
        }

        val speechService = try {
            SpeechService(recognizer, SAMPLE_RATE)
        } catch (e: Exception) {
            reportError("SpeechService: ${e.message}")
            recognizer.close()
            close()
            return@callbackFlow
        }

        val listener = object : RecognitionListener {
            override fun onPartialResult(hypothesis: String?) {
                extractText(hypothesis, "partial")?.let {
                    trySend(TranscriptionResult(it, language, confidence = 0.5f, isFinal = false))
                }
            }

            override fun onResult(hypothesis: String?) {
                extractText(hypothesis, "text")?.let {
                    trySend(TranscriptionResult(it, language, confidence = 0.8f, isFinal = true))
                }
            }

            override fun onFinalResult(hypothesis: String?) {
                extractText(hypothesis, "text")?.let {
                    trySend(TranscriptionResult(it, language, confidence = 0.8f, isFinal = true))
                }
            }

            // Auparavant un no-op : une erreur de capture (ex. AudioRecord refusé pendant un appel
            // actif) restait invisible, notification bloquée sur "analyse en cours" pour toujours.
            override fun onError(exception: Exception?) {
                reportError("capture: ${exception?.message ?: "inconnue"}")
            }

            override fun onTimeout() = Unit
        }

        val started = speechService.startListening(listener)
        if (!started) {
            reportError("startListening a retourné false")
        }

        awaitClose {
            runCatching { speechService.stop() }
            runCatching { speechService.shutdown() }
            runCatching { recognizer.close() }
        }
    }

    /** Décompresse le modèle depuis les assets vers le stockage interne au premier appel, puis le réutilise. */
    private suspend fun loadModel(): Model {
        cachedModel?.let { return it }
        return suspendCoroutine { continuation ->
            StorageService.unpack(
                context,
                MODEL_ASSET_DIR,
                MODEL_STORAGE_DIR,
                { model -> cachedModel = model; continuation.resume(model) },
                { exception -> continuation.resumeWithException(exception) },
            )
        }
    }

    private fun reportError(detail: String) {
        NotificationHelper.updateAnalysisNotificationText(
            context,
            context.getString(R.string.diagnostic_stt_error, detail),
        )
    }

    private fun extractText(hypothesisJson: String?, field: String): String? {
        if (hypothesisJson.isNullOrBlank()) return null
        return runCatching {
            Json.parseToJsonElement(hypothesisJson).jsonObject[field]?.jsonPrimitive?.content
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    companion object {
        private const val SAMPLE_RATE = 16000.0f
        private const val MODEL_ASSET_DIR = "vosk-model-fr"
        // Espace de rangement générique sur le stockage externe de l'app (StorageService.sync()
        // range le modèle sous <externalFilesDir>/<STORAGE_DIR>/<ASSET_DIR>) — distinct du nom du
        // modèle pour ne pas créer .../vosk-model-fr/vosk-model-fr, et permettre d'y ranger
        // d'autres modèles (anglais, russe) plus tard sans collision.
        private const val MODEL_STORAGE_DIR = "vosk-models"
    }
}
