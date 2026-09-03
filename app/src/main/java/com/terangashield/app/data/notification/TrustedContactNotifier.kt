package com.terangashield.app.data.notification

import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import com.terangashield.app.data.prefs.UserPreferencesRepository
import com.terangashield.app.domain.model.AppLanguage
import com.terangashield.app.domain.model.EventType
import kotlinx.coroutines.flow.first

/**
 * Envoie une alerte par SMS (pas de push, pour fonctionner sans data) au contact de confiance.
 * Ne transmet JAMAIS le contenu de la conversation/du message — uniquement un score et un
 * contexte minimal, conformément à la contrainte de confidentialité du produit.
 */
class TrustedContactNotifier(
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend fun notifyHighRisk(eventType: EventType, scorePercent: Int): Boolean {
        val contact = userPreferencesRepository.trustedContact.first() ?: return false
        val language = userPreferencesRepository.language.first()
        val message = buildMessage(eventType, scorePercent, language)
        return sendSms(contact.phoneNumber, message)
    }

    private fun buildMessage(eventType: EventType, scorePercent: Int, language: AppLanguage): String {
        val kind = when (eventType) {
            EventType.CALL -> when (language) {
                AppLanguage.FRENCH -> "un appel en cours"
                AppLanguage.ENGLISH -> "an ongoing call"
                AppLanguage.RUSSIAN -> "текущий звонок"
            }
            EventType.SMS -> when (language) {
                AppLanguage.FRENCH -> "un SMS reçu"
                AppLanguage.ENGLISH -> "a received SMS"
                AppLanguage.RUSSIAN -> "полученное СМС"
            }
        }
        return when (language) {
            AppLanguage.FRENCH -> "Teranga Shield : risque élevé détecté ($scorePercent%) sur $kind, numéro inconnu. Aucun contenu n'est partagé."
            AppLanguage.ENGLISH -> "Teranga Shield: high risk detected ($scorePercent%) on $kind, unknown number. No content is shared."
            AppLanguage.RUSSIAN -> "Teranga Shield: обнаружен высокий риск ($scorePercent%), $kind, неизвестный номер. Содержание не передаётся."
        }
    }

    private fun sendSms(phoneNumber: String, message: String): Boolean = try {
        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        true
    } catch (e: SecurityException) {
        false
    } catch (e: IllegalArgumentException) {
        false
    }
}
