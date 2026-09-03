package com.terangashield.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.terangashield.app.ui.theme.IndigoNuit
import com.terangashield.app.ui.theme.OcreTeranga
import com.terangashield.app.ui.theme.White

/** Carte héro sombre avec un grand chiffre et une barre de progression fine — pas de graphique complexe. */
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
            .background(IndigoNuit, RoundedCornerShape(24.dp))
            .padding(24.dp),
    ) {
        Text(statusTitle, style = MaterialTheme.typography.titleMedium, color = White)
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
                        fontWeight = FontWeight.Bold,
                        color = OcreTeranga,
                    )
                    Text(label, style = MaterialTheme.typography.bodyMedium, color = White.copy(alpha = 0.8f))
                }
            }
        }
        LinearProgressIndicator(
            progress = { protectionCoverage.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            color = OcreTeranga,
            trackColor = Color.White.copy(alpha = 0.15f),
        )
    }
}
