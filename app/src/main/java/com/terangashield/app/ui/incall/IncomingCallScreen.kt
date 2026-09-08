package com.terangashield.app.ui.incall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
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
import com.terangashield.app.ui.theme.AccentTeranga
import com.terangashield.app.ui.theme.AccentTerangaDark
import com.terangashield.app.ui.theme.SurfaceDark
import com.terangashield.app.ui.theme.White

/** Écran d'appel entrant — affiché quand l'app est le téléphone par défaut. */
@Composable
fun IncomingCallScreen(
    phoneNumber: String,
    contactName: String?,
    isReportedNumber: Boolean,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = SurfaceDark) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(32.dp),
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
                            .background(AccentTeranga)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = White, modifier = Modifier.size(16.dp))
                        Text(
                            stringResource(R.string.incall_reported_number),
                            color = White,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                    androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 16.dp))
                }
                Text(
                    text = contactName ?: phoneNumber,
                    color = White,
                    style = MaterialTheme.typography.headlineLarge,
                )
                if (contactName != null) {
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                CallActionButton(
                    icon = Icons.Filled.CallEnd,
                    background = AccentTerangaDark,
                    contentDescription = stringResource(R.string.incall_decline),
                    onClick = onDecline,
                )
                CallActionButton(
                    icon = Icons.Filled.Call,
                    background = AccentTeranga,
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
