package com.terangashield.app.ui.settings

import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.terangashield.app.BuildConfig
import com.terangashield.app.R
import com.terangashield.app.ServiceLocator
import com.terangashield.app.domain.model.TrustedContact
import com.terangashield.app.ui.TerangaViewModelFactory
import com.terangashield.app.ui.components.CountryPicker
import com.terangashield.app.ui.components.LanguagePicker
import com.terangashield.app.ui.components.SensitivitySlider
import com.terangashield.app.ui.components.TrustedContactRow
import java.util.Date

@Composable
fun SettingsScreen(locator: ServiceLocator, onResetOnboarding: () -> Unit, onOpenReportHistory: () -> Unit) {
    val viewModel: SettingsViewModel = viewModel(factory = TerangaViewModelFactory(locator))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val reportHistory by viewModel.reportHistory.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var editingContact by remember { mutableStateOf(false) }
    var contactName by remember(state.trustedContact) { mutableStateOf(state.trustedContact?.name ?: "") }
    var contactPhone by remember(state.trustedContact) { mutableStateOf(state.trustedContact?.phoneNumber ?: "") }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)

            CountryPicker(selected = state.country, onSelected = viewModel::setCountry, modifier = Modifier.fillMaxWidth())
            LanguagePicker(
                selected = state.language,
                onSelected = {
                    viewModel.setLanguage(it)
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(it.code))
                },
                modifier = Modifier.fillMaxWidth(),
            )

            HorizontalDivider()

            Text(stringResource(R.string.settings_trusted_contact), style = MaterialTheme.typography.titleMedium)
            TrustedContactRow(contact = state.trustedContact, onClick = { editingContact = !editingContact })
            if (editingContact) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = contactName,
                        onValueChange = { contactName = it },
                        label = { Text(stringResource(R.string.trusted_contact_name_label)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text(stringResource(R.string.trusted_contact_phone_label)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedButton(
                        onClick = {
                            if (contactName.isNotBlank() && contactPhone.isNotBlank()) {
                                viewModel.setTrustedContact(TrustedContact(contactName, contactPhone))
                                editingContact = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(R.string.onboarding_next)) }
                }
            }

            HorizontalDivider()

            Text(stringResource(R.string.settings_sensitivity), style = MaterialTheme.typography.titleMedium)
            SensitivitySlider(value = state.sensitivity, onValueChange = viewModel::setSensitivity)

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(R.string.settings_mic_consent_toggle), style = MaterialTheme.typography.titleMedium)
                Switch(checked = state.micAnalysisEnabled, onCheckedChange = viewModel::setMicAnalysisEnabled)
            }

            HorizontalDivider()

            Text(stringResource(R.string.settings_database_status), style = MaterialTheme.typography.titleMedium)
            val lastUpdate = state.reportedDatabaseLastUpdateMillis
            Text(
                if (lastUpdate != null) {
                    stringResource(R.string.settings_database_last_update, DateFormat.getDateFormat(context).format(Date(lastUpdate)))
                } else {
                    stringResource(R.string.settings_database_offline_note)
                },
                style = MaterialTheme.typography.bodyMedium,
            )

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenReportHistory),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(stringResource(R.string.settings_report_history), style = MaterialTheme.typography.titleMedium)
                    Text("${reportHistory.size}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null)
            }

            HorizontalDivider()

            OutlinedButton(onClick = { viewModel.revokeAllAndResetOnboarding(); onResetOnboarding() }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_manage_default_apps))
            }

            if (BuildConfig.DEBUG) {
                HorizontalDivider()
                DebugSimulationSection(viewModel)
            }
        }
    }
}

/**
 * Outils de démonstration/QA, uniquement visibles en build debug : rejoue un scénario d'arnaque
 * (ou un échange anodin) à travers le vrai pipeline mocké, sans dépendre du micro ni d'un appel
 * téléphonique réel — voir [com.terangashield.app.debug.ScamSimulator].
 */
@Composable
private fun DebugSimulationSection(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    fun runAndNotify(label: String, action: () -> Unit) {
        action()
        Toast.makeText(context, "$label — voir Accueil / Appels / Messages", Toast.LENGTH_SHORT).show()
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Outils de démonstration (debug)", style = MaterialTheme.typography.titleMedium)
        Text(
            "Rejoue un scénario à travers le vrai pipeline de détection (mock), sans micro ni appel réel.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { runAndNotify("Appel à risque simulé") { viewModel.simulateRiskyCall() } },
                modifier = Modifier.weight(1f),
            ) { Text("Appel à risque") }
            OutlinedButton(
                onClick = { runAndNotify("Appel sûr simulé") { viewModel.simulateSafeCall() } },
                modifier = Modifier.weight(1f),
            ) { Text("Appel sûr") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { runAndNotify("SMS à risque simulé") { viewModel.simulateRiskySms() } },
                modifier = Modifier.weight(1f),
            ) { Text("SMS à risque") }
            OutlinedButton(
                onClick = { runAndNotify("SMS sûr simulé") { viewModel.simulateSafeSms() } },
                modifier = Modifier.weight(1f),
            ) { Text("SMS sûr") }
        }
    }
}
