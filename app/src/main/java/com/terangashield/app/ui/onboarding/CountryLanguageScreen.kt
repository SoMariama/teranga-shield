package com.terangashield.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.terangashield.app.R
import com.terangashield.app.ServiceLocator
import com.terangashield.app.ui.TerangaViewModelFactory
import com.terangashield.app.ui.components.CountryPicker
import com.terangashield.app.ui.components.LanguagePicker

@Composable
fun CountryLanguageScreen(locator: ServiceLocator, onNext: () -> Unit) {
    val viewModel: OnboardingViewModel = viewModel(factory = TerangaViewModelFactory(locator))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingScaffold(
        title = stringResource(R.string.onboarding_country_title),
        body = stringResource(R.string.onboarding_country_body),
        nextLabel = stringResource(R.string.onboarding_next),
        onNext = {
            viewModel.saveCountryAndLanguage()
            onNext()
        },
        extraContent = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CountryPicker(
                    selected = state.country,
                    onSelected = viewModel::setCountry,
                    modifier = Modifier.fillMaxWidth(),
                )
                LanguagePicker(
                    selected = state.language,
                    onSelected = viewModel::setLanguage,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    )
}
