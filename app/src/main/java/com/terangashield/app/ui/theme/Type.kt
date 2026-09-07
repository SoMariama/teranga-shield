package com.terangashield.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.terangashield.app.R

// Archivo (Google Fonts, licence OFL) : trois graisses statiques embarquées dans res/font — pas
// de police téléchargée à l'exécution, l'app reste 100% hors ligne. Voir Teranga Shield.dc.html.
val ArchivoFontFamily = FontFamily(
    Font(R.font.archivo_regular, FontWeight.Normal),
    Font(R.font.archivo_semibold, FontWeight.SemiBold),
    Font(R.font.archivo_extrabold, FontWeight.ExtraBold),
)

// Texte direct et lisible pour un public non technophile : pas de tailles trop petites,
// contrastes élevés (voir Theme.kt), pas de jargon dans les libellés (voir strings.xml).
// Titres extra-gras avec un tracking légèrement resserré, libellés en petites capitales espacées
// — direction "Modernist" du design system.
val TerangaTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = ArchivoFontFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp, lineHeight = 38.sp, letterSpacing = (-0.6).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = ArchivoFontFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 27.sp, lineHeight = 32.sp, letterSpacing = (-0.4).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = ArchivoFontFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 21.sp, lineHeight = 27.sp, letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = ArchivoFontFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 17.sp, lineHeight = 23.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = ArchivoFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 17.sp, lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = ArchivoFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 21.sp,
    ),
    // Rôle bouton (Button/OutlinedButton) et titres de section — gras, sans tracking, comme
    // .btn dans la maquette (font:800 15px Archivo, pas de letter-spacing).
    labelLarge = TextStyle(
        fontFamily = ArchivoFontFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 15.sp, lineHeight = 20.sp,
    ),
    // Rôle "eyebrow"/étiquette — petites capitales espacées, comme .lbl/.tag dans la maquette.
    labelMedium = TextStyle(
        fontFamily = ArchivoFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.9.sp,
    ),
)
