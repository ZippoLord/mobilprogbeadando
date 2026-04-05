package com.example.mobilprog_beadando.data.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import java.util.Locale

class LocationHelper(private val activity: Activity) {

    fun getCityName(onResult: (String) -> Unit) {

        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
            return
        }

        val fused = LocationServices.getFusedLocationProviderClient(activity)

        fused.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                val geocoder = Geocoder(activity, Locale.getDefault())
                val addr = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                val city = addr?.firstOrNull()?.locality ?: "Unknown"
                onResult(city)
            }
        }
    }
}
