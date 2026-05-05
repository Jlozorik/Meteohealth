package com.meteohealth.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_log")
data class NotificationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val title: String,
    val body: String,
    val wellbeingIndex: Int,
    @ColumnInfo(defaultValue = "GENERAL") val eventType: String = "GENERAL",
    @ColumnInfo(defaultValue = "INFO") val severity: String = "INFO"
)
