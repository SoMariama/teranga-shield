package com.terangashield.app.ui.model

import com.terangashield.app.domain.model.EventType
import com.terangashield.app.domain.model.RiskLevel

/** Modèle unifié appel/SMS pour la liste "Activité récente" de l'Accueil. */
data class ActivityItem(
    val id: Long,
    val type: EventType,
    val title: String,
    val timestampMillis: Long,
    val riskLevel: RiskLevel,
)
