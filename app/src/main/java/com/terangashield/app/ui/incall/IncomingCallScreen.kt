package com.terangashield.app.ui.incall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.terangashield.app.R
import com.terangashield.app.ui.theme.IndigoNuit
import com.terangashield.app.ui.theme.OcreTeranga
import com.terangashield.app.ui.theme.RiskHighBg
import com.terangashield.app.ui.theme.RiskHighFg
import com.terangashield.app.ui.theme.White

/** Écran d'appel entrant — affiché quand l'app est le téléphone par défaut. */
@Composable
fun IncomingCallScreen(
    phoneNumber: String,
    isKnownContact: Boolean,
    isReportedNumber: Boolean,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = IndigoNuit) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (isReportedNumber) {
                    Row(
                        modifier = Modifier
                            .background(RiskHighBg, androidx.compose.foundation.shape.RoundedCornerShape(999.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = RiskHighFg, modifier = Modifier.size(16.dp))
                        Text(
                            stringResource(R.string.incall_reported_number),
                            color = RiskHighFg,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                    androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 16.dp))
                }
                Text(
                    text = if (isKnownContact) stringResource(R.string.incall_known_contact) else phoneNumber,
                    color = White,
                    style = MaterialTheme.typography.headlineLarge,
                )
                if (isKnownContact) {
                    Text(phoneNumber, color = White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyLarge)
                }
                Text(
                    stringResource(R.string.incall_incoming_label),
                    color = White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxSize().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                CallActionButton(
                    icon = Icons.Filled.CallEnd,
                    background = RiskHighFg,
                    contentDescription = stringResource(R.string.incall_decline),
                    onClick = onDecline,
                )
                CallActionButton(
                    icon = Icons.Filled.Call,
                    background = OcreTeranga,
                    contentDescription = stringResource(R.string.incall_answer),
                    onClick = onAnswer,
                )
            }
        }
    }
}

@Composable
fun CallActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    background: androidx.compose.ui.graphics.Color,
    contentDescription: String?,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.size(72.dp).background(background, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = contentDescription, tint = White, modifier = Modifier.size(32.dp))
        }
    }
}
