package com.terangashield.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

/**
 * Étape dédiée au prénom du propriétaire de l'appareil, séparée de l'écran du contact de
 * confiance (voir [TrustedContactSetupScreen]) : ce sont deux personnes différentes, mélanger
 * leurs champs sur un seul écran prêtait à confusion.
 *
 * Présentation volontairement plus discrète que les autres étapes (titre plus petit, pas de
 * grand écran plein — voir [OnboardingScaffold]) : demander un prénom pour une simple formule de
 * salutation n'a pas le même poids qu'un choix de confidentialité ou de rôle système.
 */
@Composable
fun YourNameScreen(locator: ServiceLocator, onNext: () -> Unit) {
    val viewModel: OnboardingViewModel = viewModel(factory = TerangaViewModelFactory(locator))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(stringResource(R.string.onboarding_your_name_title), style = MaterialTheme.typography.titleLarge)
                Text(
                    stringResource(R.string.onboarding_your_name_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = state.firstName,
                    onValueChange = viewModel::setFirstName,
                    label = { Text(stringResource(R.string.onboarding_first_name_label)) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Button(
                    onClick = { viewModel.saveProfile(); onNext() },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.onboarding_next)) }
            }
        }
    }
}
