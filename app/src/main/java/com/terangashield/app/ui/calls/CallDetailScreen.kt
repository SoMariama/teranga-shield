package com.terangashield.app.ui.calls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.terangashield.app.R
import com.terangashield.app.ServiceLocator
import com.terangashield.app.domain.model.RiskLevel
import com.terangashield.app.ui.TerangaViewModelFactory
import com.terangashield.app.ui.theme.riskColors

@Composable
fun CallDetailScreen(locator: ServiceLocator, callId: Long, onBack: () -> Unit) {
    val viewModel: CallsViewModel = viewModel(factory = TerangaViewModelFactory(locator))
    val call by viewModel.selectedCall.collectAsStateWithLifecycle()

    LaunchedEffect(callId) { viewModel.loadCall(callId) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) }
                val record = call
                Text(record?.phoneNumber ?: "", style = MaterialTheme.typography.titleLarge)
            }

            val record = call ?: return@Column
            val colors = riskColors(record.riskLevel)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            ) {
                Text(stringResource(R.string.call_detail_score), style = MaterialTheme.typography.labelLarge)
                Text(
                    "${(record.finalScore * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineLarge,
                    color = colors.foreground,
                )
            }

            if (record.triggeredCategories.isNotEmpty()) {
                Column {
                    Text(stringResource(R.string.call_detail_triggers), style = MaterialTheme.typography.labelLarge)
                    record.triggeredCategories.forEach { category ->
                        Text("• ${categoryLabel(category)}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // La transcription verbatim n'est jamais affichée pour un risque élevé (confidentialité).
            if (record.riskLevel != RiskLevel.HIGH && !record.transcriptExcerpt.isNullOrBlank()) {
                Text(record.transcriptExcerpt, style = MaterialTheme.typography.bodyMedium)
            }

            if (record.userFeedbackWasScam == null) {
                Text(stringResource(R.string.call_feedback_question), style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { viewModel.submitFeedback(record.id, true) }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.call_feedback_yes))
                    }
                    OutlinedButton(onClick = { viewModel.submitFeedback(record.id, false) }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.call_feedback_no))
                    }
                }
            }
        }
    }
}

@Composable
private fun categoryLabel(category: com.terangashield.app.domain.model.ScenarioCategory): String {
    val resId = when (category) {
        com.terangashield.app.domain.model.ScenarioCategory.TRUST_BUILDING -> R.string.category_trust_building
        com.terangashield.app.domain.model.ScenarioCategory.FABRICATED_URGENCY -> R.string.category_fabricated_urgency
        com.terangashield.app.domain.model.ScenarioCategory.SENSITIVE_INFO_REQUEST -> R.string.category_sensitive_info_request
        com.terangashield.app.domain.model.ScenarioCategory.INSTITUTION_IMPERSONATION -> R.string.category_institution_impersonation
    }
    return stringResource(resId)
}
