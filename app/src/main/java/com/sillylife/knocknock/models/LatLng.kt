//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
package com.sillylife.knocknock.models

import android.annotation.SuppressLint
import android.os.Parcel
import com.google.android.gms.common.internal.ReflectedParcelable
import com.google.android.gms.common.internal.safeparcel.AbstractSafeParcelable
import com.google.android.gms.common.internal.safeparcel.SafeParcelWriter
import com.google.android.gms.common.internal.safeparcel.SafeParcelable
import com.google.android.gms.common.internal.safeparcel.SafeParcelable.Reserved
import kotlin.math.max
import kotlin.math.min

@SuppressLint("ParcelCreator")
@Reserved(1)
@SafeParcelable.Class(creator = "LatLngCreator")
class LatLng @SafeParcelable.Constructor constructor(
        @SafeParcelable.Param(id = 2) var1: Double,
        @SafeParcelable.Param(id = 3) var3: Double
) : AbstractSafeParcelable(), ReflectedParcelable {

    @SafeParcelable.Field(id = 2)
    val latitude: Double

    @SafeParcelable.Field(id = 3)
    var longitude = 0.0
    override fun writeToParcel(var1: Parcel, var2: Int) {
        val var5 = SafeParcelWriter.beginObjectHeader(var1)
        SafeParcelWriter.writeDouble(var1, 2, latitude)
        SafeParcelWriter.writeDouble(var1, 3, longitude)
        SafeParcelWriter.finishObjectHeader(var1, var5)
    }

    override fun hashCode(): Int {
        var var2 = java.lang.Double.doubleToLongBits(latitude)
        val var1 = 31 + (var2 xor var2 ushr 32).toInt()
        var2 = java.lang.Double.doubleToLongBits(longitude)
        return var1 * 31 + (var2 xor var2 ushr 32).toInt()
    }

    override fun equals(var1: Any?): Boolean {
        return when {
            this === var1 -> {
                true
            }
            var1 !is LatLng -> {
                false
            }
            else -> {
                val var2 = var1
                java.lang.Double.doubleToLongBits(latitude) == java.lang.Double.doubleToLongBits(var2.latitude) && java.lang.Double.doubleToLongBits(
                        longitude
                ) == java.lang.Double.doubleToLongBits(var2.longitude)
            }
        }
    }

    override fun toString(): String {
        val var1 = latitude
        val var3 = longitude
        return StringBuilder(60).append("lat/lng: (").append(var1).append(",").append(var3)
                .append(")").toString()
    }

    init {
        longitude = if (-180.0 <= var3 && var3 < 180.0) {
            var3
        } else {
            ((var3 - 180.0) % 360.0 + 360.0) % 360.0 - 180.0
        }
        latitude = max(-90.0, min(90.0, var1))
    }
}