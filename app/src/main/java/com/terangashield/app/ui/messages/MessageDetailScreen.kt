package com.terangashield.app.ui.messages

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.terangashield.app.R
import com.terangashield.app.ServiceLocator
import com.terangashield.app.domain.model.SmsRiskReason
import com.terangashield.app.ui.TerangaViewModelFactory
import com.terangashield.app.ui.theme.riskColors

@Composable
fun MessageDetailScreen(locator: ServiceLocator, messageId: Long, onBack: () -> Unit) {
    val viewModel: MessagesViewModel = viewModel(factory = TerangaViewModelFactory(locator))
    val message by viewModel.selectedMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showLinkWarning by remember { mutableStateOf(false) }

    LaunchedEffect(messageId) { viewModel.loadMessage(messageId) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) }
                val record = message
                Text(record?.sender ?: "", style = MaterialTheme.typography.titleLarge)
            }

            val record = message ?: return@Column
            val colors = riskColors(record.riskLevel)

            Text(
                "${(record.finalScore * 100).toInt()}%",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.foreground,
            )

            if (record.reason != SmsRiskReason.NONE) {
                Text(reasonLabel(record.reason), style = MaterialTheme.typography.titleMedium, color = colors.foreground)
            }

            if (!record.bodyExcerpt.isNullOrBlank()) {
                Text(record.bodyExcerpt, style = MaterialTheme.typography.bodyLarge)
            }

            if (record.containsSuspiciousLink && record.suspiciousLinkUrl != null) {
                OutlinedButton(onClick = { showLinkWarning = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(record.suspiciousLinkUrl)
                }
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

            if (showLinkWarning) {
                AlertDialog(
                    onDismissRequest = { showLinkWarning = false },
                    title = { Text(stringResource(R.string.sms_link_warning_title)) },
                    text = { Text(stringResource(R.string.sms_link_warning_body)) },
                    confirmButton = {
                        TextButton(onClick = {
                            showLinkWarning = false
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(record.suspiciousLinkUrl)))
                            }
                        }) { Text(stringResource(R.string.sms_link_open_anyway)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLinkWarning = false }) { Text(stringResource(R.string.sms_link_cancel)) }
                    },
                )
            }
        }
    }
}

@Composable
private fun reasonLabel(reason: SmsRiskReason): String {
    val resId = when (reason) {
        SmsRiskReason.SUSPICIOUS_LINK -> R.string.sms_reason_suspicious_link
        SmsRiskReason.OTP_REQUEST -> R.string.sms_reason_otp_request
        SmsRiskReason.BRAND_IMPERSONATION -> R.string.sms_reason_brand_impersonation
        SmsRiskReason.NONE -> return ""
    }
    return stringResource(resId)
}
