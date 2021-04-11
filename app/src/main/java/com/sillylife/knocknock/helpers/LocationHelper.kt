package com.sillylife.knocknock.helpers

import android.location.Location
import com.sillylife.knocknock.models.Contact
import com.sillylife.knocknock.models.LatLng
import com.sillylife.knocknock.utils.CommonUtil

object LocationHelper {

    fun getDistanceBetweenTwoContacts(c1: Contact, c2: Contact): Float {
        val betweenTwoLatLng = getDistanceBetweenTwoLatLng(c1.getLatLong(), c2.getLatLong())
        val toTwoLatLng = getDistanceToTwoLatLng(c1.getLatLong(), c2.getLatLong())
        return if (betweenTwoLatLng > toTwoLatLng) {
            toTwoLatLng
        } else {
            betweenTwoLatLng
        }
    }

    fun getDistanceBetweenTwoLatLng(start: LatLng?, end: LatLng?, type: String = "M"): Float {
        if (start == null || end == null) {
            return 0F
        }
        return getDistanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, type)
    }

    fun getDistanceToTwoLatLng(start: LatLng?, end: LatLng?, type: String = "M"): Float {
        if (start == null || end == null) {
            return 0F
        }
        return getDistanceTo(start.latitude, start.longitude, end.latitude, end.longitude, type)
    }

    fun getDistanceTo(startLatitude: Double, startLongitude: Double, endLatitude: Double, endLongitude: Double, type: String): Float {
        if (CommonUtil.textIsEmpty(type) || !arrayOf("M", "KM").filter { it == type }.any()) {
            throw RuntimeException("The itemRadius can not be greater than itemWidth")
        }

        val startLocation = Location("")
        startLocation.latitude = startLatitude
        startLocation.longitude = startLongitude
        val endLocation = Location("")
        endLocation.latitude = endLatitude
        endLocation.longitude = endLongitude

        val distanceInMeters = startLocation.distanceTo(endLocation)
        return when {
            type.equals("M", ignoreCase = true) -> {
                distanceInMeters
            }
            type.equals("KM", ignoreCase = true) -> {
                distanceInMeters / 1000
            }
            else -> {
                0F
            }
        }
    }

    fun getDistanceBetween(startLatitude: Double, startLongitude: Double, endLatitude: Double, endLongitude: Double, type: String): Float {
        if (CommonUtil.textIsEmpty(type) || !arrayOf("M", "KM").filter { it == type }.any()) {
            throw RuntimeException("The itemRadius can not be greater than itemWidth")
        }

        val distanceInMeters = FloatArray(1)
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, distanceInMeters)
        return when {
            type.equals("M", ignoreCase = true) -> {
                distanceInMeters[0]
            }
            type.equals("KM", ignoreCase = true) -> {
                distanceInMeters[0] / 1000
            }
            else -> {
                0F
            }
        }
    }

}
