package com.terangashield.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.terangashield.app.ServiceLocator

/** Fabrique manuelle de ViewModels (pas de Hilt), cohérente avec [ServiceLocator]. */
class TerangaViewModelFactory(private val locator: ServiceLocator) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(com.terangashield.app.ui.onboarding.OnboardingViewModel::class.java) ->
                com.terangashield.app.ui.onboarding.OnboardingViewModel(locator.userPreferencesRepository) as T

            modelClass.isAssignableFrom(com.terangashield.app.ui.home.HomeViewModel::class.java) ->
                com.terangashield.app.ui.home.HomeViewModel(
                    locator.callRepository,
                    locator.smsRepository,
                    locator.userPreferencesRepository,
                ) as T

            modelClass.isAssignableFrom(com.terangashield.app.ui.calls.CallsViewModel::class.java) ->
                com.terangashield.app.ui.calls.CallsViewModel(locator.callRepository, locator.reportRepository) as T

            modelClass.isAssignableFrom(com.terangashield.app.ui.messages.MessagesViewModel::class.java) ->
                com.terangashield.app.ui.messages.MessagesViewModel(locator.smsRepository, locator.reportRepository) as T

            modelClass.isAssignableFrom(com.terangashield.app.ui.settings.SettingsViewModel::class.java) ->
                com.terangashield.app.ui.settings.SettingsViewModel(
                    locator.userPreferencesRepository,
                    locator.reportRepository,
                ) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
