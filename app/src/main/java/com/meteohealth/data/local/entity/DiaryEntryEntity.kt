package com.meteohealth.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_entry")
data class DiaryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val wellbeingLevel: String,
    val symptoms: String = "",
    val notes: String = "",
    val temperatureCelsius: Float? = null,
    val pressureHpa: Float? = null,
    val kpIndex: Float? = null
)
