package com.terangashield.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.terangashield.app.domain.model.RiskLevel

data class RiskColorPair(val background: Color, val foreground: Color)

// Un seul accent (voir Color.kt, direction "Modernist") : le risque élevé utilise l'accent plein
// (fond rouge-orangé, texte blanc, comme .tag dans la maquette), le risque moyen une version
// adoucie du même accent (comme .tag.w), le "sûr" un gris neutre (comme .tag.n).
// Prévu pour un badge/étiquette plein (icône, tag) — fond teinté + texte lisible dessus.
@Composable
fun riskColors(level: RiskLevel): RiskColorPair = when (level) {
    RiskLevel.SAFE -> RiskColorPair(SurfaceAlt, GreyMuted)
    RiskLevel.CAUTION -> RiskColorPair(AccentTerangaSoftBg, AccentTerangaDark)
    RiskLevel.HIGH -> RiskColorPair(AccentTeranga, White)
}

/**
 * Couleur de texte seul, sans fond teinté (ex. un score en grand sur la page) — jamais blanche
 * sur un fond clair, contrairement à [riskColors] dont le foreground n'a de sens qu'associé à son
 * background (badge HIGH : texte blanc sur fond accent plein).
 */
@Composable
fun riskTextColor(level: RiskLevel): androidx.compose.ui.graphics.Color = when (level) {
    RiskLevel.SAFE -> GreyMuted
    RiskLevel.CAUTION, RiskLevel.HIGH -> AccentTerangaDark
}
