package com.terangashield.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.terangashield.app.R
import com.terangashield.app.ServiceLocator
import com.terangashield.app.domain.model.EventType
import com.terangashield.app.domain.model.RiskLevel
import com.terangashield.app.ui.TerangaViewModelFactory
import com.terangashield.app.ui.components.RiskIconBadge
import com.terangashield.app.ui.util.relativeTimestamp

/** Numéros que l'utilisateur a lui-même signalés comme arnaque — voir ReportedNumbersViewModel. */
@Composable
fun ReportedNumbersScreen(locator: ServiceLocator, onBack: () -> Unit) {
    val viewModel: ReportedNumbersViewModel = viewModel(factory = TerangaViewModelFactory(locator))
    val items by viewModel.myReportedNumbers.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) }
                    Text(stringResource(R.string.settings_report_history), style = MaterialTheme.typography.titleLarge)
                }
                Text(
                    stringResource(R.string.reported_numbers_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
            if (items.isEmpty()) {
                item {
                    Text(stringResource(R.string.reported_numbers_empty), style = MaterialTheme.typography.bodyLarge)
                }
            }
            items(items, key = { "${it.eventType}_${it.phoneNumber}_${it.timestampMillis}" }) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RiskIconBadge(
                        riskLevel = RiskLevel.HIGH,
                        icon = if (item.eventType == EventType.CALL) Icons.Filled.Call else Icons.Filled.Sms,
                        contentDescription = null,
                    )
                    Column {
                        Text(item.phoneNumber, style = MaterialTheme.typography.titleMedium)
                        Text(
                            relativeTimestamp(context, item.timestampMillis),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
