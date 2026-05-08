package com.meteohealth.data.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.meteohealth.data.storage.converters.Converters
import com.meteohealth.data.storage.dao.JournalDao
import com.meteohealth.data.storage.dao.KpDao
import com.meteohealth.data.storage.dao.NotificationLogDao
import com.meteohealth.data.storage.dao.ProfileDao
import com.meteohealth.data.storage.dao.WeatherDao
import com.meteohealth.data.storage.entity.DisplayPrefEntity
import com.meteohealth.data.storage.entity.HealthConditionEntity
import com.meteohealth.data.storage.entity.JournalEntryEntity
import com.meteohealth.data.storage.entity.JournalEntryMetricEntity
import com.meteohealth.data.storage.entity.JournalEntrySymptomEntity
import com.meteohealth.data.storage.entity.KpMinuteEntity
import com.meteohealth.data.storage.entity.LocationEntity
import com.meteohealth.data.storage.entity.NotificationLogEntity
import com.meteohealth.data.storage.entity.NotificationPrefEntity
import com.meteohealth.data.storage.entity.ProfileEntity
import com.meteohealth.data.storage.entity.SymptomEntity
import com.meteohealth.data.storage.entity.WeatherHourEntity

@Database(
    entities = [
        ProfileEntity::class,
        HealthConditionEntity::class,
        LocationEntity::class,
        NotificationPrefEntity::class,
        DisplayPrefEntity::class,
        JournalEntryEntity::class,
        SymptomEntity::class,
        JournalEntrySymptomEntity::class,
        JournalEntryMetricEntity::class,
        WeatherHourEntity::class,
        KpMinuteEntity::class,
        NotificationLogEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun journalDao(): JournalDao
    abstract fun weatherDao(): WeatherDao
    abstract fun kpDao(): KpDao
    abstract fun notificationLogDao(): NotificationLogDao

    companion object {
        const val NAME = "meteohealth_v2.db"
    }
}
