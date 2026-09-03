package com.terangashield.app.domain.engine.real

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.terangashield.app.domain.engine.SpeechToTextEngine
import com.terangashield.app.domain.engine.TranscriptionResult
import com.terangashield.app.domain.model.AppLanguage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Transcription vocale réelle via le reconnaisseur embarqué d'Android
 * (`SpeechRecognizer.createOnDeviceSpeechRecognizer`), garanti 100% local depuis Android 12
 * (API 31) — voir [isAvailable]. Sur les appareils plus anciens, ce moteur n'est pas utilisé
 * (voir `ServiceLocator`) : on préfère rester en mode simulation plutôt que de risquer un envoi
 * réseau, un reconnaisseur classique n'offrant pas de garantie stricte de fonctionnement hors
 * ligne avant cette version.
 *
 * `SpeechRecognizer` doit être piloté depuis le thread principal ; toutes les interactions avec
 * l'instance passent donc par un `Handler` lié au `Looper` principal.
 */
class RealSpeechToTextEngine(private val context: Context) : SpeechToTextEngine {

    override fun isAvailable(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            runCatching { SpeechRecognizer.isOnDeviceRecognitionAvailable(context) }.getOrDefault(false)

    override fun listen(languageHint: AppLanguage?): Flow<TranscriptionResult> = callbackFlow {
        if (!isAvailable()) {
            close()
            return@callbackFlow
        }

        val mainHandler = Handler(Looper.getMainLooper())
        var recognizer: SpeechRecognizer? = null
        val language = languageHint ?: AppLanguage.FRENCH

        fun startListening() {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, bcp47(language))
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            runCatching { recognizer?.startListening(intent) }
        }

        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit

            override fun onError(error: Int) {
                if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                    close()
                } else {
                    // Silence, pas de correspondance, moteur occupé : normal en cours d'appel,
                    // on relance l'écoute après un court délai pour ne pas boucler à vide.
                    mainHandler.postDelayed({ startListening() }, 250)
                }
            }

            override fun onResults(results: Bundle?) {
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!text.isNullOrBlank()) {
                    trySend(TranscriptionResult(text, language, confidence = 0.8f, isFinal = true))
                }
                mainHandler.post { startListening() }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!text.isNullOrBlank()) {
                    trySend(TranscriptionResult(text, language, confidence = 0.5f, isFinal = false))
                }
            }
        }

        mainHandler.post {
            recognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(context).apply {
                setRecognitionListener(listener)
            }
            startListening()
        }

        awaitClose {
            mainHandler.post {
                runCatching { recognizer?.stopListening() }
                runCatching { recognizer?.destroy() }
            }
        }
    }

    private fun bcp47(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "en-US"
        AppLanguage.RUSSIAN -> "ru-RU"
        AppLanguage.FRENCH -> "fr-FR"
    }
}
