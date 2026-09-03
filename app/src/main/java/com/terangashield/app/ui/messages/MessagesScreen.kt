package com.terangashield.app.ui.messages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
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
import com.terangashield.app.domain.model.EventType
import com.terangashield.app.ui.TerangaViewModelFactory
import com.terangashield.app.ui.components.ActivityRow
import com.terangashield.app.ui.model.ActivityItem

@Composable
fun MessagesScreen(locator: ServiceLocator, onOpenMessage: (Long) -> Unit) {
    val viewModel: MessagesViewModel = viewModel(factory = TerangaViewModelFactory(locator))
    val messages by viewModel.messages.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item { Text(stringResource(R.string.messages_title), style = MaterialTheme.typography.headlineMedium) }
            items(messages, key = { it.id }) { sms ->
                ActivityRow(
                    item = ActivityItem(sms.id, EventType.SMS, sms.sender, sms.timestampMillis, sms.riskLevel),
                    onClick = { onOpenMessage(sms.id) },
                )
            }
        }
    }
}
