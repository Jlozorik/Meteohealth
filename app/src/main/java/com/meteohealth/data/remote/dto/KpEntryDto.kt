package com.meteohealth.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// NOAA SWPC отдаёт массив объектов: [{"time_tag": "...", "kp_index": 1.33}, ...]
@Serializable
data class KpEntryDto(
    @SerialName("time_tag") val timeTag: String,
    @SerialName("kp_index") val kpIndex: Double? = null
)
