package com.meteohealth.data.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** hour_bucket_epoch = floor(ts / 3600) — естественный PK, заменяет singleton + history. */
@Entity(tableName = "weather_hour")
data class WeatherHourEntity(
    @PrimaryKey @ColumnInfo(name = "hour_bucket_epoch") val hourBucketEpoch: Long,
    val t: Double,
    val p: Double,
    val h: Int,
    val wind: Double,
    val descr: String,
    val icon: String,
    val city: String,
)

@Entity(tableName = "kp_minute")
data class KpMinuteEntity(
    @PrimaryKey val ts: Long,
    val kp: Double,
)

@Entity(tableName = "notification_log")
data class NotificationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ts: Long,
    val type: String,
    val severity: String,
    val body: String,
)
