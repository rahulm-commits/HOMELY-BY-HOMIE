package com.example.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale

data class GeoPoint(
  val latitude: Double,
  val longitude: Double,
)

fun hasLocationPermission(context: Context): Boolean {
  val hasFine = ContextCompat.checkSelfPermission(
    context,
    Manifest.permission.ACCESS_FINE_LOCATION,
  ) == PackageManager.PERMISSION_GRANTED
  val hasCoarse = ContextCompat.checkSelfPermission(
    context,
    Manifest.permission.ACCESS_COARSE_LOCATION,
  ) == PackageManager.PERMISSION_GRANTED
  return hasFine || hasCoarse
}

fun formatCoordinatePair(latitude: Double, longitude: Double): String =
  String.format(Locale.US, "%.4f° N, %.4f° E", latitude, longitude)

@SuppressLint("MissingPermission")
fun fetchCurrentDeviceLocation(
  context: Context,
  highAccuracy: Boolean = true,
  onSuccess: (lat: Double, lng: Double, statusMsg: String) -> Unit,
  onError: (String) -> Unit,
) {
  if (!hasLocationPermission(context)) {
    onError("Location permission not granted")
    return
  }

  try {
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    val tokenSource = CancellationTokenSource()
    val priority = if (highAccuracy) {
      Priority.PRIORITY_HIGH_ACCURACY
    } else {
      Priority.PRIORITY_BALANCED_POWER_ACCURACY
    }
    fusedClient.getCurrentLocation(priority, tokenSource.token)
      .addOnSuccessListener { loc ->
        if (loc != null) {
          onSuccess(
            loc.latitude,
            loc.longitude,
            "Location Locked: ${String.format(Locale.US, "%.4f, %.4f", loc.latitude, loc.longitude)}",
          )
        } else {
          fusedClient.lastLocation
            .addOnSuccessListener { lastLoc ->
              if (lastLoc != null) {
                onSuccess(
                  lastLoc.latitude,
                  lastLoc.longitude,
                  "Location Locked: ${String.format(Locale.US, "%.4f, %.4f", lastLoc.latitude, lastLoc.longitude)}",
                )
              } else {
                fallbackLocationManager(context, onSuccess, onError)
              }
            }
            .addOnFailureListener {
              fallbackLocationManager(context, onSuccess, onError)
            }
        }
      }
      .addOnFailureListener {
        fallbackLocationManager(context, onSuccess, onError)
      }
  } catch (e: Exception) {
    fallbackLocationManager(context, onSuccess, onError)
  }
}

@SuppressLint("MissingPermission")
private fun fallbackLocationManager(
  context: Context,
  onSuccess: (lat: Double, lng: Double, statusMsg: String) -> Unit,
  onError: (String) -> Unit,
) {
  try {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    val candidates: List<Location> = listOfNotNull(
      lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER),
      lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER),
      lm?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER),
    )
    val best = candidates.maxByOrNull { it.time }
    if (best != null) {
      onSuccess(
        best.latitude,
        best.longitude,
        "Location Locked: ${String.format(Locale.US, "%.4f, %.4f", best.latitude, best.longitude)}",
      )
    } else {
      onError("GPS signal unavailable right now. Turn on device Location and retry.")
    }
  } catch (e: Exception) {
    onError(e.localizedMessage ?: "Unable to read GPS coordinates")
  }
}
