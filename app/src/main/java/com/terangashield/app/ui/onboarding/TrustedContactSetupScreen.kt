package com.terangashield.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.terangashield.app.R
import com.terangashield.app.ServiceLocator
import com.terangashield.app.ui.TerangaViewModelFactory

/**
 * Contact de confiance : une suggestion, pas une obligation — utile surtout pour quelqu'un de
 * moins familier avec les méthodes des arnaqueurs, qui bénéficie d'avoir un proche alerté en cas
 * de risque élevé détecté. Le bouton "Continuer" n'est donc jamais bloqué par ces champs : passer
 * cette étape sans rien remplir est un choix valide.
 */
@Composable
fun TrustedContactSetupScreen(locator: ServiceLocator, onNext: () -> Unit) {
    val viewModel: OnboardingViewModel = viewModel(factory = TerangaViewModelFactory(locator))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingScaffold(
        title = stringResource(R.string.onboarding_trusted_contact_title),
        body = stringResource(R.string.onboarding_trusted_contact_body),
        nextLabel = stringResource(R.string.onboarding_next),
        onNext = {
            viewModel.saveTrustedContact()
            onNext()
        },
        extraContent = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.onboarding_trusted_contact_optional_hint), style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = state.trustedContactName,
                    onValueChange = viewModel::setTrustedContactName,
                    label = { Text(stringResource(R.string.trusted_contact_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.trustedContactPhone,
                    onValueChange = viewModel::setTrustedContactPhone,
                    label = { Text(stringResource(R.string.trusted_contact_phone_label)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    )
}
