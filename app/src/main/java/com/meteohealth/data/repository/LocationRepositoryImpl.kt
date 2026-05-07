package com.meteohealth.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.meteohealth.domain.repository.LocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class LocationRepositoryImpl(
    private val context: Context
) : LocationRepository {

    private val geocoderLocale: Locale = Locale.forLanguageTag("ru")

    override suspend fun getCurrentLocation(): LocationRepository.GeoLocation? {
        if (!hasPermission()) return null
        val client = LocationServices.getFusedLocationProviderClient(context)
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .build()
        val raw = suspendCancellableCoroutine<android.location.Location?> { cont ->
            try {
                client.getCurrentLocation(request, null)
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resume(null) }
                    .addOnCanceledListener { cont.resume(null) }
            } catch (_: SecurityException) {
                cont.resume(null)
            }
        } ?: return null

        val cityName = reverseGeocode(raw.latitude, raw.longitude)
        return LocationRepository.GeoLocation(raw.latitude, raw.longitude, cityName)
    }

    override suspend fun searchCity(query: String): List<LocationRepository.GeoLocation> {
        val trimmed = query.trim()
        if (trimmed.length < 2) return emptyList()
        if (!Geocoder.isPresent()) return emptyList()
        val geocoder = Geocoder(context, geocoderLocale)
        val addresses: List<Address> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                try {
                    geocoder.getFromLocationName(trimmed, 5) { result ->
                        cont.resume(result ?: emptyList())
                    }
                } catch (_: Exception) {
                    cont.resume(emptyList())
                }
            }
        } else {
            withContext(Dispatchers.IO) {
                @Suppress("DEPRECATION")
                runCatching { geocoder.getFromLocationName(trimmed, 5) }.getOrNull().orEmpty()
            }
        }
        return addresses.mapNotNull { addr ->
            val name = addr.bestCityName() ?: return@mapNotNull null
            LocationRepository.GeoLocation(
                latitude = addr.latitude,
                longitude = addr.longitude,
                cityName = name
            )
        }.distinctBy { it.cityName to it.latitude.toString().take(6) to it.longitude.toString().take(6) }
    }

    private suspend fun reverseGeocode(lat: Double, lon: Double): String? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context, geocoderLocale)
        val addresses: List<Address> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                try {
                    geocoder.getFromLocation(lat, lon, 1) { result ->
                        cont.resume(result ?: emptyList())
                    }
                } catch (_: Exception) {
                    cont.resume(emptyList())
                }
            }
        } else {
            withContext(Dispatchers.IO) {
                @Suppress("DEPRECATION")
                runCatching { geocoder.getFromLocation(lat, lon, 1) }.getOrNull().orEmpty()
            }
        }
        return addresses.firstOrNull()?.bestCityName()
    }

    /**
     * Имя ближайшего населённого пункта: сначала [Address.locality]
     * (например, «Реутов»), затем округ ([subAdminArea]),
     * и только после — регион ([adminArea]) — иначе для пригородов
     * подставлялась бы Москва.
     */
    private fun Address.bestCityName(): String? =
        locality?.takeIf { it.isNotBlank() }
            ?: subAdminArea?.takeIf { it.isNotBlank() }
            ?: subLocality?.takeIf { it.isNotBlank() }
            ?: adminArea?.takeIf { it.isNotBlank() }

    private fun hasPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }
}
