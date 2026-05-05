package com.meteohealth.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.meteohealth.domain.repository.LocationRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationRepositoryImpl(
    private val context: Context
) : LocationRepository {

    override suspend fun getCurrentLocation(): LocationRepository.LatLon? {
        if (!hasPermission()) return null
        val client = LocationServices.getFusedLocationProviderClient(context)
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .build()
        return suspendCancellableCoroutine { cont ->
            try {
                client.getCurrentLocation(request, null)
                    .addOnSuccessListener { location ->
                        cont.resume(
                            location?.let { LocationRepository.LatLon(it.latitude, it.longitude) }
                        )
                    }
                    .addOnFailureListener { cont.resume(null) }
                    .addOnCanceledListener { cont.resume(null) }
            } catch (_: SecurityException) {
                cont.resume(null)
            }
        }
    }

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
