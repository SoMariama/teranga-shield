package com.terangashield.app.ui.messages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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

@Composable
fun MessagesScreen(locator: ServiceLocator, onOpenMessage: (Long) -> Unit, onNewMessage: () -> Unit) {
    val viewModel: MessagesViewModel = viewModel(factory = TerangaViewModelFactory(locator))
    val messages by viewModel.messages.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(onClick = onNewMessage, containerColor = OcreTeranga, contentColor = IndigoNuit) {
                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.new_message_title))
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item { Text(stringResource(R.string.messages_title), style = MaterialTheme.typography.headlineMedium) }
            items(messages, key = { it.id }) { sms ->
                val title = if (sms.isOutgoing) {
                    stringResource(R.string.outgoing_message_prefix, sms.sender)
                } else {
                    sms.sender
                }
                ActivityRow(
                    item = ActivityItem(sms.id, EventType.SMS, title, sms.timestampMillis, sms.riskLevel),
                    onClick = { onOpenMessage(sms.id) },
                )
            }
        }
    }
}
