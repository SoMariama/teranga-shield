package com.terangashield.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.terangashield.app.domain.model.RiskLevel

data class RiskColorPair(val background: Color, val foreground: Color)

@Composable
fun riskColors(level: RiskLevel): RiskColorPair = when (level) {
    RiskLevel.SAFE -> RiskColorPair(RiskSafeBg, RiskSafeFg)
    RiskLevel.CAUTION -> RiskColorPair(RiskMediumBg, RiskMediumFg)
    RiskLevel.HIGH -> RiskColorPair(RiskHighBg, RiskHighFg)
}
