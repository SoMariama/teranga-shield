package com.terangashield.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terangashield.app.data.prefs.UserPreferencesRepository
import com.terangashield.app.domain.model.AppLanguage
import com.terangashield.app.domain.model.Country
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val country: Country = Country.SENEGAL,
    val language: AppLanguage = AppLanguage.FRENCH,
    val firstName: String = "",
    val trustedContactName: String = "",
    val trustedContactPhone: String = "",
    val defaultAppsConsentChecked: Boolean = false,
    val micConsentChecked: Boolean = false,
)

class OnboardingViewModel(private val prefs: UserPreferencesRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState

    fun setCountry(country: Country) = _uiState.update { it.copy(country = country) }
    fun setLanguage(language: AppLanguage) = _uiState.update { it.copy(language = language) }
    fun setFirstName(name: String) = _uiState.update { it.copy(firstName = name) }
    fun setTrustedContactName(name: String) = _uiState.update { it.copy(trustedContactName = name) }
    fun setTrustedContactPhone(phone: String) = _uiState.update { it.copy(trustedContactPhone = phone) }
    fun setDefaultAppsConsentChecked(checked: Boolean) = _uiState.update { it.copy(defaultAppsConsentChecked = checked) }
    fun setMicConsentChecked(checked: Boolean) = _uiState.update { it.copy(micConsentChecked = checked) }

    fun saveCountryAndLanguage() = viewModelScope.launch {
        prefs.setCountry(_uiState.value.country)
        prefs.setLanguage(_uiState.value.language)
    }

    fun saveProfileAndTrustedContact() = viewModelScope.launch {
        val state = _uiState.value
        prefs.setUserFirstName(state.firstName)
        if (state.trustedContactName.isNotBlank() && state.trustedContactPhone.isNotBlank()) {
            prefs.setTrustedContact(
                com.terangashield.app.domain.model.TrustedContact(state.trustedContactName, state.trustedContactPhone),
            )
        }
    }

    fun acceptDefaultAppsConsent() = viewModelScope.launch { prefs.recordDefaultAppsConsent(true) }

    fun acceptMicConsent() = viewModelScope.launch { prefs.recordMicConsent(_uiState.value.micConsentChecked) }

    fun completeOnboarding() = viewModelScope.launch { prefs.setOnboardingComplete(true) }
}
