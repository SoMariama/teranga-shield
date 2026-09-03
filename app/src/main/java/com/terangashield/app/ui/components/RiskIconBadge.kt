package com.terangashield.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.terangashield.app.domain.model.RiskLevel
import com.terangashield.app.ui.theme.riskColors

/** Pastille arrondie colorée par niveau de risque, plutôt qu'un badge texte bordé. */
@Composable
fun RiskIconBadge(
    riskLevel: RiskLevel,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val colors = riskColors(riskLevel)
    Box(
        modifier = modifier
            .size(38.dp)
            .background(colors.background, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = colors.foreground)
    }
}
