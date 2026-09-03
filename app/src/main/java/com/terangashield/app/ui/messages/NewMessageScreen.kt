package com.terangashield.app.ui.messages

import android.os.Build
import android.telephony.SmsManager
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.terangashield.app.R
import com.terangashield.app.ServiceLocator
import com.terangashield.app.data.db.entity.SmsRecordEntity
import com.terangashield.app.domain.model.RiskLevel
import com.terangashield.app.domain.model.SmsRiskReason
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Écran minimal de composition d'un SMS sortant. */
@Composable
fun NewMessageScreen(locator: ServiceLocator, initialRecipient: String, onSent: () -> Unit, onBack: () -> Unit) {
    var recipient by remember { mutableStateOf(initialRecipient) }
    var body by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) }
                Text(stringResource(R.string.new_message_title), style = MaterialTheme.typography.titleLarge)
            }

            OutlinedTextField(
                value = recipient,
                onValueChange = { recipient = it },
                label = { Text(stringResource(R.string.new_message_recipient)) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text(stringResource(R.string.new_message_body)) },
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    if (recipient.isNotBlank() && body.isNotBlank()) {
                        sendSms(context, recipient, body)
                        scope.launch {
                            val language = locator.userPreferencesRepository.language.first()
                            locator.smsRepository.insert(
                                SmsRecordEntity(
                                    sender = recipient,
                                    isKnownContact = false,
                                    timestampMillis = System.currentTimeMillis(),
                                    riskLevel = RiskLevel.SAFE,
                                    finalScore = 0f,
                                    reason = SmsRiskReason.NONE,
                                    detectedLanguage = language,
                                    bodyExcerpt = body.take(500),
                                    containsSuspiciousLink = false,
                                    suspiciousLinkUrl = null,
                                    opened = true,
                                    trustedContactNotified = false,
                                    isOutgoing = true,
                                ),
                            )
                            onSent()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.new_message_send))
            }
        }
    }
}

private fun sendSms(context: android.content.Context, recipient: String, body: String) {
    runCatching {
        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
        smsManager.sendTextMessage(recipient, null, body, null, null)
    }
}
