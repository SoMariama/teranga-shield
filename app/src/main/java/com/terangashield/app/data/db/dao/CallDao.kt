package com.terangashield.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.terangashield.app.data.db.entity.CallRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallDao {
    @Insert
    suspend fun insert(record: CallRecordEntity): Long

    @Update
    suspend fun update(record: CallRecordEntity)

    @Query("SELECT * FROM call_records ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<CallRecordEntity>>

    @Query("SELECT * FROM call_records WHERE id = :id")
    suspend fun getById(id: Long): CallRecordEntity?

    @Query("SELECT * FROM call_records ORDER BY timestampMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<CallRecordEntity>>

    @Query("SELECT COUNT(*) FROM call_records WHERE riskLevel != 'SAFE' AND timestampMillis >= :sinceMillis")
    fun observeBlockedCountSince(sinceMillis: Long): Flow<Int>
}
