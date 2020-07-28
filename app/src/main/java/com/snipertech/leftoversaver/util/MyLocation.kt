package com.snipertech.leftoversaver.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import java.util.*


class MyLocation {
    var timer1: Timer? = null
    var lm: LocationManager? = null
    var locationResult: LocationResult? = null
    var gpsEnabled = false
    var networkEnabled = false

    fun getLocation(context: Activity, result: LocationResult?): Boolean {
        //I use LocationResult callback class to pass location value from MyLocation to user code.
        locationResult = result
        if (lm == null) lm =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //exceptions will be thrown if provider is not permitted.
        try {
            gpsEnabled = lm!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }
        try {
            networkEnabled = lm!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
        }
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            ActivityCompat.requestPermissions(
                context,
                permissions,
                Constants.REQUEST_FINE_LOCATION
            )
        }
        //don't start listeners if no provider is enabled
        if (!gpsEnabled && !networkEnabled) return false
        if (gpsEnabled) lm!!.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            0f,
            locationListenerGps
        )
        if (networkEnabled) lm!!.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            0,
            0f,
            locationListenerNetwork
        )
        timer1 = Timer()
        timer1!!.schedule(GetLastLocation(context), 20000)
        return true
    }

    var locationListenerGps = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                timer1!!.cancel()
                locationResult!!.gotLocation(location)
                lm!!.removeUpdates(this)
                lm!!.removeUpdates(locationListenerNetwork)
            }

            override fun onProviderDisabled(provider: String) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onStatusChanged(
                provider: String,
                status: Int,
                extras: Bundle
            ) {
            }
        }

    var locationListenerNetwork: LocationListener =
        object : LocationListener {
            override fun onLocationChanged(location: Location) {
                timer1!!.cancel()
                locationResult!!.gotLocation(location)
                lm!!.removeUpdates(this)
                lm!!.removeUpdates(locationListenerGps)
            }

            override fun onProviderDisabled(provider: String) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onStatusChanged(
                provider: String,
                status: Int,
                extras: Bundle
            ) {
            }
        }

    internal inner class GetLastLocation(val context: Activity) : TimerTask() {
        override fun run() {
            lm!!.removeUpdates(locationListenerGps)
            lm!!.removeUpdates(locationListenerNetwork)
            var netLoc: Location? = null
            var gpsLoc: Location? = null

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val permissions = arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                ActivityCompat.requestPermissions(
                    context,
                    permissions,
                    Constants.REQUEST_FINE_LOCATION
                )
            }
            if (gpsEnabled) gpsLoc = lm!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (networkEnabled) netLoc =
                lm!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            //if there are both values use the latest one
            if (gpsLoc != null && netLoc != null) {
                if (gpsLoc.time > netLoc.time) {
                    locationResult!!.gotLocation(gpsLoc)
                } else {
                    locationResult!!.gotLocation(netLoc)
                }
                return
            } else {
                if (gpsLoc != null) {
                    locationResult!!.gotLocation(gpsLoc)
                    return
                }
                if (netLoc != null) {
                    locationResult!!.gotLocation(netLoc)
                    return
                }
                locationResult!!.gotLocation(null)
            }
        }
    }

    abstract class LocationResult {
        abstract fun gotLocation(location: Location?)
    }

    fun cancelTimer() {
        timer1?.cancel()
        lm?.removeUpdates(locationListenerGps)
        lm?.removeUpdates(locationListenerNetwork)
        lm = null
        locationResult = null
    }
}