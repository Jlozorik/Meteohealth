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
import com.meteohealth.data.local.dao.WeatherHistoryDao
import com.meteohealth.data.local.entity.DiaryEntryEntity
import com.meteohealth.data.local.entity.KpCacheEntity
import com.meteohealth.data.local.entity.NotificationLogEntity
import com.meteohealth.data.local.entity.UserProfileEntity
import com.meteohealth.data.local.entity.WeatherCacheEntity
import com.meteohealth.data.local.entity.WeatherHistoryEntity

@Database(
    entities = [
        UserProfileEntity::class,
        WeatherCacheEntity::class,
        DiaryEntryEntity::class,
        KpCacheEntity::class,
        NotificationLogEntity::class,
        WeatherHistoryEntity::class
    ],
    version = 5,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun weatherCacheDao(): WeatherCacheDao
    abstract fun diaryEntryDao(): DiaryEntryDao
    abstract fun kpCacheDao(): KpCacheDao
    abstract fun notificationLogDao(): NotificationLogDao
    abstract fun weatherHistoryDao(): WeatherHistoryDao

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
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `weather_history` (" +
                        "`timestamp` INTEGER NOT NULL, " +
                        "`pressureHpa` REAL NOT NULL, " +
                        "`temperatureCelsius` REAL NOT NULL, " +
                        "`humidity` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`timestamp`))"
                )
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // user_profile: расширение под персонализацию + типы уведомлений + геолокацию
                db.execSQL("ALTER TABLE user_profile ADD COLUMN age INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN sensitivity TEXT NOT NULL DEFAULT 'MODERATE'")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN notifyPressureJump INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN notifyGeomagneticStorm INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN notifyFrost INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN notifyHeat INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN cityName TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN latitude REAL DEFAULT NULL")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN longitude REAL DEFAULT NULL")

                // notification_log: типизация события и уровень критичности
                db.execSQL("ALTER TABLE notification_log ADD COLUMN eventType TEXT NOT NULL DEFAULT 'GENERAL'")
                db.execSQL("ALTER TABLE notification_log ADD COLUMN severity TEXT NOT NULL DEFAULT 'INFO'")
            }
        }
    }
}
