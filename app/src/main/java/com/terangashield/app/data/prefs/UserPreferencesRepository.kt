package com.terangashield.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.terangashield.app.domain.model.AppLanguage
import com.terangashield.app.domain.model.ConsentRecord
import com.terangashield.app.domain.model.Country
import com.terangashield.app.domain.model.CURRENT_CONSENT_TEXT_VERSION
import com.terangashield.app.domain.model.DetectionSensitivity
import com.terangashield.app.domain.model.TrustedContact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "teranga_shield_prefs")

/** Préférences utilisateur : pays, langue, sensibilité, contact de confiance, consentement. */
class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val COUNTRY = stringPreferencesKey("country_iso")
        val LANGUAGE = stringPreferencesKey("language_code")
        val SENSITIVITY = stringPreferencesKey("sensitivity")
        val TRUSTED_CONTACT_NAME = stringPreferencesKey("trusted_contact_name")
        val TRUSTED_CONTACT_PHONE = stringPreferencesKey("trusted_contact_phone")
        val USER_FIRST_NAME = stringPreferencesKey("user_first_name")

        val DEFAULT_APPS_CONSENT = booleanPreferencesKey("consent_default_apps")
        val DEFAULT_APPS_CONSENT_TS = longPreferencesKey("consent_default_apps_ts")
        val MIC_CONSENT = booleanPreferencesKey("consent_mic")
        val MIC_CONSENT_TS = longPreferencesKey("consent_mic_ts")
        val CONSENT_VERSION = intPreferencesKey("consent_text_version")
        val MIC_ANALYSIS_ENABLED = booleanPreferencesKey("mic_analysis_enabled")

        val SPEAKER_REQUIRES_EXPLICIT_CONSENT = booleanPreferencesKey("speaker_requires_explicit_consent")
        val REPORTED_DB_LAST_UPDATE_TS = longPreferencesKey("reported_db_last_update_ts")
    }

    val onboardingComplete: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.ONBOARDING_COMPLETE] ?: false }

    val country: Flow<Country> =
        context.dataStore.data.map { Country.fromIso(it[Keys.COUNTRY] ?: Country.SENEGAL.isoCode) }

    val language: Flow<AppLanguage> =
        context.dataStore.data.map { AppLanguage.fromCode(it[Keys.LANGUAGE] ?: AppLanguage.FRENCH.code) }

    val sensitivity: Flow<DetectionSensitivity> =
        context.dataStore.data.map {
            DetectionSensitivity.entries.firstOrNull { s -> s.name == it[Keys.SENSITIVITY] }
                ?: DetectionSensitivity.MEDIUM
        }

    val trustedContact: Flow<TrustedContact?> =
        context.dataStore.data.map {
            val name = it[Keys.TRUSTED_CONTACT_NAME]
            val phone = it[Keys.TRUSTED_CONTACT_PHONE]
            if (name.isNullOrBlank() || phone.isNullOrBlank()) null else TrustedContact(name, phone)
        }

    val userFirstName: Flow<String> = context.dataStore.data.map { it[Keys.USER_FIRST_NAME] ?: "" }

    val consentRecord: Flow<ConsentRecord> = context.dataStore.data.map {
        ConsentRecord(
            defaultAppsConsentGiven = it[Keys.DEFAULT_APPS_CONSENT] ?: false,
            defaultAppsConsentTimestamp = it[Keys.DEFAULT_APPS_CONSENT_TS],
            micAnalysisConsentGiven = it[Keys.MIC_CONSENT] ?: false,
            micAnalysisConsentTimestamp = it[Keys.MIC_CONSENT_TS],
            consentTextVersion = it[Keys.CONSENT_VERSION] ?: 0,
        )
    }

    val micAnalysisEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.MIC_ANALYSIS_ENABLED] ?: true }

    val speakerRequiresExplicitConsent: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.SPEAKER_REQUIRES_EXPLICIT_CONSENT] ?: false }

    val reportedDatabaseLastUpdateMillis: Flow<Long?> =
        context.dataStore.data.map { it[Keys.REPORTED_DB_LAST_UPDATE_TS] }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETE] = complete }
    }

    suspend fun setCountry(country: Country) {
        context.dataStore.edit { it[Keys.COUNTRY] = country.isoCode }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setSensitivity(sensitivity: DetectionSensitivity) {
        context.dataStore.edit { it[Keys.SENSITIVITY] = sensitivity.name }
    }

    suspend fun setTrustedContact(contact: TrustedContact) {
        context.dataStore.edit {
            it[Keys.TRUSTED_CONTACT_NAME] = contact.name
            it[Keys.TRUSTED_CONTACT_PHONE] = contact.phoneNumber
        }
    }

    suspend fun setUserFirstName(name: String) {
        context.dataStore.edit { it[Keys.USER_FIRST_NAME] = name }
    }

    suspend fun recordDefaultAppsConsent(granted: Boolean) {
        context.dataStore.edit {
            it[Keys.DEFAULT_APPS_CONSENT] = granted
            it[Keys.DEFAULT_APPS_CONSENT_TS] = System.currentTimeMillis()
            it[Keys.CONSENT_VERSION] = CURRENT_CONSENT_TEXT_VERSION
        }
    }

    suspend fun recordMicConsent(granted: Boolean) {
        context.dataStore.edit {
            it[Keys.MIC_CONSENT] = granted
            it[Keys.MIC_CONSENT_TS] = System.currentTimeMillis()
            it[Keys.MIC_ANALYSIS_ENABLED] = granted
            it[Keys.CONSENT_VERSION] = CURRENT_CONSENT_TEXT_VERSION
        }
    }

    suspend fun setMicAnalysisEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MIC_ANALYSIS_ENABLED] = enabled }
    }

    suspend fun setReportedDatabaseLastUpdate(timestampMillis: Long) {
        context.dataStore.edit { it[Keys.REPORTED_DB_LAST_UPDATE_TS] = timestampMillis }
    }

    /** Retire tous les consentements et réinitialise l'onboarding — utilisé quand l'utilisateur désactive l'app depuis les Réglages. */
    suspend fun revokeAllConsentsAndReset() {
        context.dataStore.edit {
            it[Keys.DEFAULT_APPS_CONSENT] = false
            it[Keys.MIC_CONSENT] = false
            it[Keys.MIC_ANALYSIS_ENABLED] = false
            it[Keys.ONBOARDING_COMPLETE] = false
        }
    }

    suspend fun currentLanguageBlocking(): AppLanguage = language.first()
}
