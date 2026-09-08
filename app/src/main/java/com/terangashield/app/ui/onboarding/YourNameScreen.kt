package com.terangashield.app.ui.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.terangashield.app.R
import com.terangashield.app.ServiceLocator
import com.terangashield.app.ui.TerangaViewModelFactory

/**
 * Étape dédiée au prénom du propriétaire de l'appareil, séparée de l'écran du contact de
 * confiance (voir [TrustedContactSetupScreen]) : ce sont deux personnes différentes, mélanger
 * leurs champs sur un seul écran prêtait à confusion.
 */
@Composable
fun YourNameScreen(locator: ServiceLocator, onNext: () -> Unit) {
    val viewModel: OnboardingViewModel = viewModel(factory = TerangaViewModelFactory(locator))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingScaffold(
        title = stringResource(R.string.onboarding_your_name_title),
        body = stringResource(R.string.onboarding_your_name_body),
        nextLabel = stringResource(R.string.onboarding_next),
        onNext = {
            viewModel.saveProfile()
            onNext()
        },
        extraContent = {
            OutlinedTextField(
                value = state.firstName,
                onValueChange = viewModel::setFirstName,
                label = { Text(stringResource(R.string.onboarding_first_name_label)) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}
