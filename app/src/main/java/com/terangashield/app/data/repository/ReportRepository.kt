package com.terangashield.app.data.repository

import com.terangashield.app.data.db.dao.ReportDao
import com.terangashield.app.data.db.entity.UserReportEntity
import com.terangashield.app.domain.model.EventType
import kotlinx.coroutines.flow.Flow

class ReportRepository(private val reportDao: ReportDao) {
    fun observeAll(): Flow<List<UserReportEntity>> = reportDao.observeAll()

    suspend fun submit(eventType: EventType, relatedEventId: Long, wasActuallyScam: Boolean) {
        reportDao.insert(
            UserReportEntity(
                eventType = eventType,
                relatedEventId = relatedEventId,
                timestampMillis = System.currentTimeMillis(),
                wasActuallyScam = wasActuallyScam,
            ),
        )
    }
}
