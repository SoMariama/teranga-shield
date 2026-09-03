package com.terangashield.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.terangashield.app.data.db.dao.CallDao
import com.terangashield.app.data.db.dao.ReportDao
import com.terangashield.app.data.db.dao.SmsDao
import com.terangashield.app.data.db.entity.CallRecordEntity
import com.terangashield.app.data.db.entity.SmsRecordEntity
import com.terangashield.app.data.db.entity.UserReportEntity

@Database(
    entities = [CallRecordEntity::class, SmsRecordEntity::class, UserReportEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class TerangaDatabase : RoomDatabase() {
    abstract fun callDao(): CallDao
    abstract fun smsDao(): SmsDao
    abstract fun reportDao(): ReportDao

    companion object {
        @Volatile private var instance: TerangaDatabase? = null

        fun getInstance(context: Context): TerangaDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TerangaDatabase::class.java,
                    "teranga_shield.db",
                ).build().also { instance = it }
            }
    }
}
