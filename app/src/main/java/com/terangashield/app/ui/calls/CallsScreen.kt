package com.terangashield.app.ui.calls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
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
import com.terangashield.app.domain.model.EventType
import com.terangashield.app.ui.TerangaViewModelFactory
import com.terangashield.app.ui.components.ActivityRow
import com.terangashield.app.ui.model.ActivityItem
import com.terangashield.app.ui.theme.IndigoNuit
import com.terangashield.app.ui.theme.OcreTeranga
import com.terangashield.app.ui.theme.White

@Composable
fun CallsScreen(
    locator: ServiceLocator,
    onOpenCall: (Long) -> Unit,
    onOpenDialer: () -> Unit,
    onOpenContacts: () -> Unit,
) {
    val viewModel: CallsViewModel = viewModel(factory = TerangaViewModelFactory(locator))
    val calls by viewModel.calls.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallFloatingActionButton(onClick = onOpenContacts, containerColor = White, contentColor = IndigoNuit) {
                    Icon(Icons.Filled.Contacts, contentDescription = stringResource(R.string.contacts_title))
                }
                FloatingActionButton(onClick = onOpenDialer, containerColor = OcreTeranga, contentColor = IndigoNuit) {
                    Icon(Icons.Filled.Dialpad, contentDescription = stringResource(R.string.dialer_title))
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item { Text(stringResource(R.string.calls_title), style = MaterialTheme.typography.headlineMedium) }
            items(calls, key = { it.id }) { call ->
                ActivityRow(
                    item = ActivityItem(call.id, EventType.CALL, call.phoneNumber, call.timestampMillis, call.riskLevel),
                    onClick = { onOpenCall(call.id) },
                )
            }
        }
    }
}
