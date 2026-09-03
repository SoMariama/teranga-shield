package com.terangashield.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.terangashield.app.domain.model.TrustedContact
import com.terangashield.app.ui.theme.OcreTeranga
import com.terangashield.app.ui.theme.White

@Composable
fun TrustedContactRow(contact: TrustedContact?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(44.dp).background(OcreTeranga, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                (contact?.name?.firstOrNull() ?: '+').uppercase().toString(),
                style = MaterialTheme.typography.titleMedium,
                color = White,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(contact?.name ?: "—", style = MaterialTheme.typography.titleMedium)
            Text(
                contact?.phoneNumber ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
