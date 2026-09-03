package com.terangashield.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.terangashield.app.domain.model.AppLanguage
import com.terangashield.app.domain.model.RiskLevel
import com.terangashield.app.domain.model.ScenarioCategory

@Entity(tableName = "call_records")
data class CallRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val isKnownContact: Boolean,
    val timestampMillis: Long,
    val durationSeconds: Int,
    val riskLevel: RiskLevel,
    val finalScore: Float,
    val triggeredCategories: List<ScenarioCategory>,
    val detectedLanguage: AppLanguage,
    /**
     * Extrait de transcription conservé UNIQUEMENT quand le score n'est pas élevé.
     * Pour un appel à risque élevé, on ne conserve que les catégories déclenchées,
     * jamais le contenu verbatim — voir la contrainte de confidentialité du produit.
     */
    val transcriptExcerpt: String?,
    val trustedContactNotified: Boolean,
    val userFeedbackWasScam: Boolean? = null,
)
