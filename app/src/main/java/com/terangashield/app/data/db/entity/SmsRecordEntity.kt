package com.terangashield.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.terangashield.app.domain.model.AppLanguage
import com.terangashield.app.domain.model.RiskLevel
import com.terangashield.app.domain.model.SmsRiskReason

@Entity(tableName = "sms_records")
data class SmsRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val isKnownContact: Boolean,
    val timestampMillis: Long,
    val riskLevel: RiskLevel,
    val finalScore: Float,
    val reason: SmsRiskReason,
    val detectedLanguage: AppLanguage,
    /** Aperçu conservé uniquement quand nécessaire à l'affichage ; jamais le corps complet pour un risque élevé. */
    val bodyExcerpt: String?,
    val containsSuspiciousLink: Boolean,
    val suspiciousLinkUrl: String?,
    val opened: Boolean,
    val trustedContactNotified: Boolean,
    val userFeedbackWasScam: Boolean? = null,
    /** Vrai pour un SMS envoyé par l'utilisateur depuis l'app (voir NewMessageScreen) — `sender` porte alors le destinataire. */
    val isOutgoing: Boolean = false,
)
