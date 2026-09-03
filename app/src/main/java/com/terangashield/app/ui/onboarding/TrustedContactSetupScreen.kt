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

@Composable
fun TrustedContactSetupScreen(locator: ServiceLocator, onNext: () -> Unit) {
    val viewModel: OnboardingViewModel = viewModel(factory = TerangaViewModelFactory(locator))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingScaffold(
        title = stringResource(R.string.onboarding_trusted_contact_title),
        body = stringResource(R.string.onboarding_trusted_contact_body),
        nextLabel = stringResource(R.string.onboarding_next),
        nextEnabled = state.trustedContactName.isNotBlank() && state.trustedContactPhone.isNotBlank(),
        onNext = {
            viewModel.saveProfileAndTrustedContact()
            onNext()
        },
        extraContent = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.firstName,
                    onValueChange = viewModel::setFirstName,
                    label = { Text(stringResource(R.string.onboarding_first_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
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
