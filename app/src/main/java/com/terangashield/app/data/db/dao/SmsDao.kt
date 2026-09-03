package com.terangashield.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.terangashield.app.data.db.entity.SmsRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsDao {
    @Insert
    suspend fun insert(record: SmsRecordEntity): Long

    @Update
    suspend fun update(record: SmsRecordEntity)

    @Query("SELECT * FROM sms_records ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<SmsRecordEntity>>

    @Query("SELECT * FROM sms_records WHERE id = :id")
    suspend fun getById(id: Long): SmsRecordEntity?

    @Query("SELECT * FROM sms_records ORDER BY timestampMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<SmsRecordEntity>>

    @Query("SELECT COUNT(*) FROM sms_records WHERE riskLevel != 'SAFE' AND timestampMillis >= :sinceMillis")
    fun observeFilteredCountSince(sinceMillis: Long): Flow<Int>
}
