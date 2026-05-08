package com.meteohealth.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KpEntryDto(
    @SerialName("time_tag") val timeTag: String,
    @SerialName("kp_index") val kpIndex: Double,
)
