package com.terangashield.app.data.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.terangashield.app.data.db.entity.SmsRecordEntity
import com.terangashield.app.data.prefs.UserPreferencesRepository
import com.terangashield.app.data.repository.SmsRepository
import com.terangashield.app.domain.model.AppLanguage
import com.terangashield.app.domain.model.RiskLevel
import com.terangashield.app.domain.model.SmsRiskReason
import com.terangashield.app.service.call.ContactsLookup

/**
 * Importe une fois les SMS déjà présents dans le fournisseur système (`content://sms`) au moment
 * où l'app devient gestionnaire SMS par défaut — sans ça, l'onglet Messages reste vide jusqu'à
 * la réception d'un nouveau SMS, puisque Room ne contient que ce que
 * [com.terangashield.app.service.sms.SmsDeliverReceiver] a traité depuis l'installation.
 *
 * Les messages importés ne sont volontairement PAS passés dans le pipeline d'analyse de risque
 * (potentiellement des centaines de messages, et une analyse rétroactive n'a pas de sens pour un
 * historique) : ils apparaissent comme "Sûr", non analysés. Seuls les nouveaux SMS reçus après
 * l'import bénéficient de l'analyse réelle.
 */
class SmsHistoryImporter(
    private val context: Context,
    private val smsRepository: SmsRepository,
    private val prefs: UserPreferencesRepository,
) {
    suspend fun importIfNeeded() {
        if (prefs.isSmsHistoryImported()) return
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) ==
            PackageManager.PERMISSION_GRANTED
        // Pas encore la permission (onboarding pas terminé) : on retentera au prochain démarrage,
        // ne surtout pas marquer "importé" sans avoir réellement pu lire quoi que ce soit.
        if (!hasPermission) return

        val language = prefs.currentLanguageBlocking()
        val projection = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
        )

        val imported = runCatching {
            context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                projection,
                null,
                null,
                "${Telephony.Sms.DATE} DESC LIMIT $MAX_IMPORTED_MESSAGES",
            )?.use { cursor ->
                val addressIndex = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = cursor.getColumnIndex(Telephony.Sms.DATE)

                while (cursor.moveToNext()) {
                    val address = cursor.getString(addressIndex) ?: continue
                    val body = cursor.getString(bodyIndex) ?: ""
                    val date = cursor.getLong(dateIndex)
                    val isKnownContact = ContactsLookup.isKnownContact(context, address)

                    smsRepository.insert(
                        SmsRecordEntity(
                            sender = address,
                            isKnownContact = isKnownContact,
                            timestampMillis = date,
                            riskLevel = RiskLevel.SAFE,
                            finalScore = 0f,
                            reason = SmsRiskReason.NONE,
                            detectedLanguage = language,
                            bodyExcerpt = body.take(BODY_EXCERPT_MAX_CHARS),
                            containsSuspiciousLink = false,
                            suspiciousLinkUrl = null,
                            opened = true,
                            trustedContactNotified = false,
                        ),
                    )
                }
            }
        }.isSuccess

        if (imported) prefs.setSmsHistoryImported()
    }

    companion object {
        private const val MAX_IMPORTED_MESSAGES = 500
        private const val BODY_EXCERPT_MAX_CHARS = 500
    }
}
