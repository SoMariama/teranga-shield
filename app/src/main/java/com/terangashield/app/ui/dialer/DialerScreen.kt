package com.terangashield.app.ui.dialer

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.telecom.TelecomManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.terangashield.app.R
import com.terangashield.app.ui.theme.IndigoNuit
import com.terangashield.app.ui.theme.OcreTeranga
import com.terangashield.app.ui.theme.White

/** Clavier numérique minimal pour passer un appel sortant via l'app devenue téléphone par défaut. */
@Composable
fun DialerScreen(initialNumber: String, onBack: () -> Unit) {
    var number by remember { mutableStateOf(initialNumber) }
    val context = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) }
                Text(stringResource(R.string.dialer_title), style = MaterialTheme.typography.titleLarge)
            }

            Text(
                text = number.ifEmpty { " " },
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("*", "0", "#"),
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { digit ->
                            OutlinedButton(
                                onClick = { number += digit },
                                modifier = Modifier.weight(1f).aspectRatio(1.4f),
                            ) { Text(digit, style = MaterialTheme.typography.titleLarge) }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                androidx.compose.material3.FloatingActionButton(
                    onClick = {
                        if (number.isNotBlank()) placeCall(context, number)
                    },
                    containerColor = OcreTeranga,
                    contentColor = IndigoNuit,
                ) { Icon(Icons.Filled.Call, contentDescription = stringResource(R.string.dialer_call)) }
                if (number.isNotEmpty()) {
                    IconButton(onClick = { number = number.dropLast(1) }) {
                        Icon(Icons.Filled.Backspace, contentDescription = stringResource(R.string.dialer_backspace))
                    }
                } else {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }
    }
}

private fun placeCall(context: android.content.Context, number: String) {
    val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) ==
        PackageManager.PERMISSION_GRANTED
    if (!hasPermission) return
    val telecomManager = context.getSystemService(TelecomManager::class.java) ?: return
    runCatching {
        telecomManager.placeCall(Uri.fromParts("tel", number, null), null)
    }
}
