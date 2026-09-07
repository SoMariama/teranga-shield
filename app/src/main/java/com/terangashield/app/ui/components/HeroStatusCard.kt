package com.terangashield.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.terangashield.app.ui.theme.AccentTeranga
import com.terangashield.app.ui.theme.White

/** Bandeau héro plein accent avec un grand chiffre — direction "Modernist", angles à zéro. */
@Composable
fun HeroStatusCard(
    statusTitle: String,
    metrics: List<Pair<String, Int>>,
    protectionCoverage: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AccentTeranga)
            .padding(24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.layout.Box(modifier = Modifier.size(9.dp).background(White))
            Text(
                statusTitle.uppercase(java.util.Locale.getDefault()),
                style = MaterialTheme.typography.labelMedium,
                color = White,
                modifier = Modifier.padding(start = 10.dp),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            metrics.forEach { (label, value) ->
                Column {
                    Text(
                        value.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                    )
                    Text(label, style = MaterialTheme.typography.bodyMedium, color = White.copy(alpha = 0.9f))
                }
            }
        }
        LinearProgressIndicator(
            progress = { protectionCoverage.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            color = White,
            trackColor = White.copy(alpha = 0.25f),
        )
    }
}
