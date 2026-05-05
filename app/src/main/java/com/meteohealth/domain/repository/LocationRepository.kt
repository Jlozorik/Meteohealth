package com.meteohealth.domain.repository

interface LocationRepository {
    suspend fun getCurrentLocation(): LatLon?

    data class LatLon(val latitude: Double, val longitude: Double)
}
