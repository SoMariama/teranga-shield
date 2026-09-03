package com.terangashield.app.data.repository

import com.terangashield.app.data.db.dao.CallDao
import com.terangashield.app.data.db.entity.CallRecordEntity
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class CallRepository(private val callDao: CallDao) {
    fun observeAll(): Flow<List<CallRecordEntity>> = callDao.observeAll()
    fun observeRecent(limit: Int = 20): Flow<List<CallRecordEntity>> = callDao.observeRecent(limit)
    fun observeBlockedCountLast7Days(): Flow<Int> =
        callDao.observeBlockedCountSince(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7))

    suspend fun getById(id: Long): CallRecordEntity? = callDao.getById(id)
    suspend fun insert(record: CallRecordEntity): Long = callDao.insert(record)
    suspend fun update(record: CallRecordEntity) = callDao.update(record)

    suspend fun recordFeedback(id: Long, wasScam: Boolean) {
        val record = callDao.getById(id) ?: return
        callDao.update(record.copy(userFeedbackWasScam = wasScam))
    }
}
