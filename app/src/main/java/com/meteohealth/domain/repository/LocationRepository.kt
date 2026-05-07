package com.meteohealth.domain.repository

interface LocationRepository {
    /** Текущая геопозиция устройства с резолвом названия города (при возможности). */
    suspend fun getCurrentLocation(): GeoLocation?

    /** Поиск города по строке (forward-geocoding). Возвращает до 5 кандидатов. */
    suspend fun searchCity(query: String): List<GeoLocation>

    data class GeoLocation(
        val latitude: Double,
        val longitude: Double,
        val cityName: String?
    )
}
