package com.terangashield.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.terangashield.app.domain.model.EventType

/** Historique des signalements envoyés par l'utilisateur (feedback post-appel/SMS), pour l'écran Réglages. */
@Entity(tableName = "user_reports")
data class UserReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: EventType,
    val relatedEventId: Long,
    val timestampMillis: Long,
    val wasActuallyScam: Boolean,
    /** Vrai une fois envoyé au pipeline de ré-entraînement communautaire (uniquement si une connexion existe). */
    val syncedToCommunity: Boolean = false,
)
