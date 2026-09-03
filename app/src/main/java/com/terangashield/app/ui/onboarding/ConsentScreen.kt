package com.terangashield.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.terangashield.app.R
import com.terangashield.app.ServiceLocator
import com.terangashield.app.ui.TerangaViewModelFactory

/**
 * Écran de consentement dédié, distinct des demandes de permission système. Le texte de
 * consentement pour l'analyse audio est isolé visuellement (carte séparée), pas fondu dans un
 * paragraphe générique. Aucune case n'est pré-cochée.
 */
@Composable
fun ConsentScreen(locator: ServiceLocator, onNext: () -> Unit) {
    val viewModel: OnboardingViewModel = viewModel(factory = TerangaViewModelFactory(locator))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(stringResource(R.string.consent_title), style = MaterialTheme.typography.headlineMedium)

            ConsentCard(
                title = null,
                body = stringResource(R.string.consent_default_apps_body),
                checked = state.defaultAppsConsentChecked,
                onCheckedChange = viewModel::setDefaultAppsConsentChecked,
            )

            ConsentCard(
                title = stringResource(R.string.consent_mic_title),
                body = stringResource(R.string.consent_mic_body),
                checked = state.micConsentChecked,
                onCheckedChange = viewModel::setMicConsentChecked,
                emphasized = true,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { /* reste sur l'écran : aucun consentement implicite */ }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.consent_decline))
                }
                Button(
                    onClick = {
                        viewModel.acceptDefaultAppsConsent()
                        viewModel.acceptMicConsent()
                        onNext()
                    },
                    enabled = state.defaultAppsConsentChecked,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.consent_accept))
                }
            }
        }
    }
}

@Composable
private fun ConsentCard(
    title: String?,
    body: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    emphasized: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (emphasized) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(16.dp),
            )
            .padding(16.dp),
    ) {
        if (title != null) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
        }
        Text(body, style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
            Text(stringResource(R.string.consent_checkbox_label), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
