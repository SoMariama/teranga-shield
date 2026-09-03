package com.terangashield.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.terangashield.app.ui.components.HeroStatusCard

@Composable
fun HomeScreen(locator: ServiceLocator, onOpenCall: (Long) -> Unit, onOpenMessage: (Long) -> Unit) {
    val viewModel: HomeViewModel = viewModel(factory = TerangaViewModelFactory(locator))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    val greetingName = state.firstName.ifBlank { "" }
                    Text(
                        stringResource(R.string.home_greeting, greetingName).trim(),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    HeroStatusCard(
                        statusTitle = stringResource(R.string.home_protection_active),
                        metrics = listOf(
                            stringResource(R.string.home_calls_blocked_7d) to state.callsBlocked7d,
                            stringResource(R.string.home_sms_filtered_7d) to state.smsFiltered7d,
                        ),
                        protectionCoverage = 1f,
                    )
                    Text(stringResource(R.string.home_recent_activity), style = MaterialTheme.typography.titleLarge)
                }
            }
            items(state.recentActivity, key = { "${it.type}_${it.id}" }) { activityItem ->
                ActivityRow(
                    item = activityItem,
                    onClick = {
                        if (activityItem.type == EventType.CALL) onOpenCall(activityItem.id) else onOpenMessage(activityItem.id)
                    },
                )
            }
        }
    }
}
