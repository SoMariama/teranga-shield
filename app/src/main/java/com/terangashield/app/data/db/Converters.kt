package com.terangashield.app.data.db

import androidx.room.TypeConverter
import com.terangashield.app.domain.model.AppLanguage
import com.terangashield.app.domain.model.EventType
import com.terangashield.app.domain.model.RiskLevel
import com.terangashield.app.domain.model.ScenarioCategory
import com.terangashield.app.domain.model.SmsRiskReason

class Converters {
    @TypeConverter
    fun fromRiskLevel(value: RiskLevel): String = value.name

    @TypeConverter
    fun toRiskLevel(value: String): RiskLevel = RiskLevel.valueOf(value)

    @TypeConverter
    fun fromAppLanguage(value: AppLanguage): String = value.code

    @TypeConverter
    fun toAppLanguage(value: String): AppLanguage = AppLanguage.fromCode(value)

    @TypeConverter
    fun fromEventType(value: EventType): String = value.name

    @TypeConverter
    fun toEventType(value: String): EventType = EventType.valueOf(value)

    @TypeConverter
    fun fromSmsRiskReason(value: SmsRiskReason): String = value.name

    @TypeConverter
    fun toSmsRiskReason(value: String): SmsRiskReason = SmsRiskReason.valueOf(value)

    @TypeConverter
    fun fromCategoryList(value: List<ScenarioCategory>): String = value.joinToString(",") { it.name }

    @TypeConverter
    fun toCategoryList(value: String): List<ScenarioCategory> =
        if (value.isBlank()) emptyList() else value.split(",").map { ScenarioCategory.valueOf(it) }
}
