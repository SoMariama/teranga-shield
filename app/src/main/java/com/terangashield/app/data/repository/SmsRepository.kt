package com.terangashield.app.data.repository

import com.terangashield.app.data.db.dao.SmsDao
import com.terangashield.app.data.db.entity.SmsRecordEntity
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class SmsRepository(private val smsDao: SmsDao) {
    fun observeAll(): Flow<List<SmsRecordEntity>> = smsDao.observeAll()
    fun observeRecent(limit: Int = 20): Flow<List<SmsRecordEntity>> = smsDao.observeRecent(limit)
    fun observeFilteredCountLast7Days(): Flow<Int> =
        smsDao.observeFilteredCountSince(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7))

    suspend fun getById(id: Long): SmsRecordEntity? = smsDao.getById(id)
    suspend fun insert(record: SmsRecordEntity): Long = smsDao.insert(record)
    suspend fun update(record: SmsRecordEntity) = smsDao.update(record)

    suspend fun markOpened(id: Long) {
        val record = smsDao.getById(id) ?: return
        smsDao.update(record.copy(opened = true))
    }

    suspend fun recordFeedback(id: Long, wasScam: Boolean) {
        val record = smsDao.getById(id) ?: return
        smsDao.update(record.copy(userFeedbackWasScam = wasScam))
    }
}
