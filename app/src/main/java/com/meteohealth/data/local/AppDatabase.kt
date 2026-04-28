package com.meteohealth.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.meteohealth.data.local.dao.DiaryEntryDao
import com.meteohealth.data.local.dao.KpCacheDao
import com.meteohealth.data.local.dao.NotificationLogDao
import com.meteohealth.data.local.dao.UserProfileDao
import com.meteohealth.data.local.dao.WeatherCacheDao
import com.meteohealth.data.local.entity.DiaryEntryEntity
import com.meteohealth.data.local.entity.KpCacheEntity
import com.meteohealth.data.local.entity.NotificationLogEntity
import com.meteohealth.data.local.entity.UserProfileEntity
import com.meteohealth.data.local.entity.WeatherCacheEntity

@Database(
    entities = [
        UserProfileEntity::class,
        WeatherCacheEntity::class,
        DiaryEntryEntity::class,
        KpCacheEntity::class,
        NotificationLogEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun weatherCacheDao(): WeatherCacheDao
    abstract fun diaryEntryDao(): DiaryEntryDao
    abstract fun kpCacheDao(): KpCacheDao
    abstract fun notificationLogDao(): NotificationLogDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN pressureUnit TEXT NOT NULL DEFAULT 'MMHG'")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN isDarkTheme INTEGER NOT NULL DEFAULT 1")
            }
        }
    }
}
