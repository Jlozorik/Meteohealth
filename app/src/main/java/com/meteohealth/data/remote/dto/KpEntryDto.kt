package com.meteohealth.data.remote.dto

import kotlinx.serialization.Serializable

// NOAA SWPC отдаёт массив массивов: [timestamp_str, Kp_str]
@Serializable
data class KpEntryDto(
    val timestamp: String,
    val kpIndex: String
)
