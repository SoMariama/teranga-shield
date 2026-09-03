package com.terangashield.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Palette validée pour Teranga Shield : chaleureuse et accessible plutôt que "sécurité
 * informatique" austère. Un seul thème clair est défini — pas de dégradés, pas de glassmorphism.
 */
private val TerangaColorScheme = lightColorScheme(
    primary = OcreTeranga,
    onPrimary = IndigoNuit,
    secondary = IndigoNuit,
    onSecondary = White,
    background = SableClair,
    onBackground = Ink,
    surface = White,
    onSurface = Ink,
    surfaceVariant = SableClair,
    onSurfaceVariant = InkMuted,
    error = RiskHighFg,
    errorContainer = RiskHighBg,
)

@Composable
fun TerangaShieldTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = TerangaColorScheme,
        typography = TerangaTypography,
        content = content,
    )
}
