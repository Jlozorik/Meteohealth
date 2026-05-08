package com.meteohealth.data.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Long = 1,
    val name: String,
    val age: Int,
    val sensitivity: Int,
)

@Entity(
    tableName = "health_conditions",
    primaryKeys = ["profile_id", "condition"],
)
data class HealthConditionEntity(
    @ColumnInfo(name = "profile_id") val profileId: Long,
    val condition: String,
)

@Entity(tableName = "location", primaryKeys = ["profile_id"])
data class LocationEntity(
    @ColumnInfo(name = "profile_id") val profileId: Long,
    val city: String,
    val lat: Double,
    val lon: Double,
    @ColumnInfo(name = "auto_detect") val autoDetect: Int,
)

@Entity(
    tableName = "notification_pref",
    primaryKeys = ["profile_id", "kind"],
)
data class NotificationPrefEntity(
    @ColumnInfo(name = "profile_id") val profileId: Long,
    val kind: String,
    val enabled: Int,
)

@Entity(tableName = "display_pref", primaryKeys = ["profile_id"])
data class DisplayPrefEntity(
    @ColumnInfo(name = "profile_id") val profileId: Long,
    @ColumnInfo(name = "pressure_unit") val pressureUnit: String,
)
