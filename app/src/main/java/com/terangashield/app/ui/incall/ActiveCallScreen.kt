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
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.terangashield.app.R
import com.terangashield.app.ui.theme.AccentTeranga
import com.terangashield.app.ui.theme.AccentTerangaDark
import com.terangashield.app.ui.theme.SurfaceDark
import com.terangashield.app.ui.theme.SurfaceDarkCard
import com.terangashield.app.ui.theme.White
import kotlinx.coroutines.delay

/** Écran d'appel en cours — minuteur, muet, haut-parleur, raccrocher. */
@Composable
fun ActiveCallScreen(
    phoneNumber: String,
    isKnownContact: Boolean,
    isConnected: Boolean,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    showSpeakerPrompt: Boolean,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onHangup: () -> Unit,
) {
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(isConnected) {
        if (isConnected) {
            while (true) {
                delay(1000)
                elapsedSeconds += 1
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = SurfaceDark) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = if (isKnownContact) stringResource(R.string.incall_known_contact) else phoneNumber,
                    color = White,
                    style = MaterialTheme.typography.headlineLarge,
                )
                Text(
                    text = if (isConnected) formatDuration(elapsedSeconds) else stringResource(R.string.incall_connecting),
                    color = White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )

                if (showSpeakerPrompt) {
                    Column(
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .fillMaxWidth()
                            .background(SurfaceDarkCard)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            stringResource(R.string.incall_speaker_prompt),
                            style = MaterialTheme.typography.bodyMedium,
                            color = White.copy(alpha = 0.85f),
                        )
                        Button(
                            onClick = onToggleSpeaker,
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = AccentTeranga, contentColor = White),
                            shape = androidx.compose.ui.graphics.RectangleShape,
                            modifier = Modifier.padding(top = 10.dp),
                        ) {
                            Text(stringResource(R.string.incall_speaker_prompt_action))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                ToggleCallButton(
                    icon = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                    active = isMuted,
                    contentDescription = stringResource(R.string.incall_mute),
                    onClick = onToggleMute,
                )
                ToggleCallButton(
                    icon = Icons.Filled.VolumeUp,
                    active = isSpeakerOn,
                    contentDescription = stringResource(R.string.incall_speaker),
                    onClick = onToggleSpeaker,
                )
            }

            CallActionButton(
                icon = Icons.Filled.CallEnd,
                background = AccentTerangaDark,
                contentDescription = stringResource(R.string.incall_hangup),
                onClick = onHangup,
            )
        }
    }
}

@Composable
private fun ToggleCallButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    contentDescription: String?,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .background(if (active) White else Color.White.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = contentDescription, tint = if (active) SurfaceDark else White)
        }
    }
}

private fun formatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
