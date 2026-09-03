package com.terangashield.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.terangashield.app.data.db.entity.UserReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Insert
    suspend fun insert(report: UserReportEntity): Long

    @Query("SELECT * FROM user_reports ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<UserReportEntity>>

    @Query("SELECT * FROM user_reports WHERE syncedToCommunity = 0")
    suspend fun getUnsynced(): List<UserReportEntity>
}
