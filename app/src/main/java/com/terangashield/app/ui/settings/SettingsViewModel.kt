package com.terangashield.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terangashield.app.data.db.entity.UserReportEntity
import com.terangashield.app.data.prefs.UserPreferencesRepository
import com.terangashield.app.data.repository.ReportRepository
import com.terangashield.app.domain.model.AppLanguage
import com.terangashield.app.domain.model.Country
import com.terangashield.app.domain.model.DetectionSensitivity
import com.terangashield.app.domain.model.TrustedContact
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val country: Country = Country.SENEGAL,
    val language: AppLanguage = AppLanguage.FRENCH,
    val sensitivity: DetectionSensitivity = DetectionSensitivity.MEDIUM,
    val trustedContact: TrustedContact? = null,
    val micAnalysisEnabled: Boolean = true,
    val reportedDatabaseLastUpdateMillis: Long? = null,
)

class SettingsViewModel(
    private val prefs: UserPreferencesRepository,
    reportRepository: ReportRepository,
) : ViewModel() {

    private val basePrefs = combine(
        prefs.country,
        prefs.language,
        prefs.sensitivity,
        prefs.trustedContact,
        prefs.micAnalysisEnabled,
    ) { country, language, sensitivity, contact, micEnabled ->
        SettingsUiState(
            country = country,
            language = language,
            sensitivity = sensitivity,
            trustedContact = contact,
            micAnalysisEnabled = micEnabled,
        )
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        basePrefs,
        prefs.reportedDatabaseLastUpdateMillis,
    ) { base, lastUpdate -> base.copy(reportedDatabaseLastUpdateMillis = lastUpdate) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    val reportHistory: StateFlow<List<UserReportEntity>> =
        reportRepository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCountry(country: Country) = viewModelScope.launch { prefs.setCountry(country) }
    fun setLanguage(language: AppLanguage) = viewModelScope.launch { prefs.setLanguage(language) }
    fun setSensitivity(sensitivity: DetectionSensitivity) = viewModelScope.launch { prefs.setSensitivity(sensitivity) }
    fun setTrustedContact(contact: TrustedContact) = viewModelScope.launch { prefs.setTrustedContact(contact) }
    fun setMicAnalysisEnabled(enabled: Boolean) = viewModelScope.launch { prefs.setMicAnalysisEnabled(enabled) }
    fun revokeAllAndResetOnboarding() = viewModelScope.launch { prefs.revokeAllConsentsAndReset() }
}
