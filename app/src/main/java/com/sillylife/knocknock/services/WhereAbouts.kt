package com.sillylife.knocknock.services

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import android.util.Log
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat


class WhereAbouts : Service(), LocationListener {

    companion object {
        private const val TAG = "WhereAbouts"

        // The minimum distance to change Updates in meters
        const val LOCATION_CHANGE_THRESHOLD = 50 // meters

        // The minimum time between updates in milliseconds
        const val MIN_TIME_BW_UPDATES = (1000 * 60 * 1).toLong() // 1 minute
    }

    // Declaring a Location Manager
    private var locationManager: LocationManager? = null

    // location
    private var location: Location? = null

    // flag for GPS and network status
    private var canGetLocation = false

    private var mBestLocationProvider = ""

    //Service methods
    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate Start")
        if (locationManager == null) {
            locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
        setBestProvider()
        Log.d(TAG, "onCreate End")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand Start")
        // checking network states and permissions
        if (canGetLocation && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager?.requestLocationUpdates(mBestLocationProvider, MIN_TIME_BW_UPDATES, LOCATION_CHANGE_THRESHOLD.toFloat(), this)
            if (locationManager != null) {
                location = locationManager!!.getLastKnownLocation(mBestLocationProvider)
            }
        }
        Log.d(TAG, "onStartCommand end")
        // If we get killed, after returning from here, restart
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy Start")
        if (locationManager != null) {
            locationManager!!.removeUpdates(this)
        }
        Log.d(TAG, "onDestroy End")
        super.onDestroy()
    }


    // Location methods
    override fun onLocationChanged(location: Location) {
        var updateLocation = false
        if (this.location != null) {
            updateLocation = location.distanceTo(this.location) >= LOCATION_CHANGE_THRESHOLD
        }
        if (updateLocation) {
            this.location = location
        }
    }

    override fun onProviderEnabled(provider: String) {
        setBestProvider()
        super.onProviderEnabled(provider)
    }

    override fun onProviderDisabled(provider: String) {
        setBestProvider()
        super.onProviderDisabled(provider)
    }

    private fun setBestProvider() {
        if (locationManager == null) {
            return
        }
        // getting GPS status
        val isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        // getting network status
        val isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        // setting canGetLocation based on network status or GPS status
        mBestLocationProvider = when {
            isGPSEnabled -> {
                LocationManager.GPS_PROVIDER
            }
            isNetworkEnabled -> {
                LocationManager.NETWORK_PROVIDER
            }
            else -> {
                ""
            }
        }
        this.canGetLocation = isGPSEnabled || isNetworkEnabled
    }
}