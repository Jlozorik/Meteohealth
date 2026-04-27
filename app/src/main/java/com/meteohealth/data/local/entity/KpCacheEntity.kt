package com.meteohealth.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kp_cache")
data class KpCacheEntity(
    @PrimaryKey val timestamp: Long,
    val kpIndex: Float
)
