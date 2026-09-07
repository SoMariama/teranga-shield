package com.terangashield.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.terangashield.app.R
import com.terangashield.app.domain.model.EventType
import com.terangashield.app.domain.model.RiskLevel
import com.terangashield.app.ui.model.ActivityItem
import com.terangashield.app.ui.theme.riskColors
import com.terangashield.app.ui.util.relativeTimestamp
import java.util.Locale

@Composable
fun ActivityRow(item: ActivityItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RiskIconBadge(
            riskLevel = item.riskLevel,
            icon = if (item.type == EventType.CALL) Icons.Filled.Call else Icons.Filled.Sms,
            contentDescription = null,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium)
            Text(
                relativeTimestamp(context, item.timestampMillis),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val colors = riskColors(item.riskLevel)
        Text(
            text = riskLabel(item.riskLevel).uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.labelMedium,
            color = colors.foreground,
            modifier = Modifier
                .background(colors.background)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun riskLabel(level: RiskLevel): String = when (level) {
    RiskLevel.SAFE -> stringResource(R.string.risk_safe)
    RiskLevel.CAUTION -> stringResource(R.string.risk_medium)
    RiskLevel.HIGH -> stringResource(R.string.risk_high)
}
