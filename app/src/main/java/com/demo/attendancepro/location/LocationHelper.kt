package com.demo.attendancepro.location

import android.annotation.SuppressLint
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

data class GeoPoint(val latitude: Double, val longitude: Double)

/**
 * Thin wrapper around [FusedLocationProviderClient] so screens don't talk to Play Services
 * directly. Caller is responsible for having already requested location permissions.
 */
class LocationHelper @Inject constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient
) {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): GeoPoint? = suspendCancellableCoroutine { continuation ->
        val cancellationTokenSource = CancellationTokenSource()
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        fusedLocationProviderClient.getCurrentLocation(request, cancellationTokenSource.token)
            .addOnSuccessListener { location ->
                if (continuation.isActive) {
                    continuation.resume(location?.let { GeoPoint(it.latitude, it.longitude) })
                }
            }
            .addOnFailureListener {
                if (continuation.isActive) continuation.resume(null)
            }

        continuation.invokeOnCancellation { cancellationTokenSource.cancel() }
    }
}
