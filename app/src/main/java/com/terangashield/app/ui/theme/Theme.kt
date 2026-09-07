package com.terangashield.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Palette "Modernist" (direction retenue du design system, voir Teranga Shield.dc.html) : un seul
 * accent rouge-orangé (#EC3013) sur encre quasi noire et fond gris clair, angles à zéro, filets de
 * 2px au lieu d'ombres/élévation. Remplace la précédente palette Indigo/Ocre comme thème principal.
 */
private val TerangaColorScheme = lightColorScheme(
    primary = AccentTeranga,
    onPrimary = White,
    secondary = InkModernist,
    onSecondary = White,
    background = SurfaceAppBg,
    onBackground = InkModernist,
    surface = White,
    onSurface = InkModernist,
    surfaceVariant = SurfaceAlt,
    onSurfaceVariant = GreyMuted,
    outline = GreyBorder,
    error = AccentTerangaDark,
    errorContainer = AccentTerangaSoftBg,
    onErrorContainer = AccentTerangaDark,
)

@Composable
fun TerangaShieldTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = TerangaColorScheme,
        typography = TerangaTypography,
        shapes = TerangaShapes,
        content = content,
    )
}
