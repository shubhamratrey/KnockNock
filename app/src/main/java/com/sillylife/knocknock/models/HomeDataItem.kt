package com.sillylife.knocknock.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HomeDataItem(
        var type: String? = null,
        var title: String? = null,
//        @SerializedName("revenue") val revenue: Revenue? = null,
//        @SerializedName("products") val products: ArrayList<Product>? = null
) : Parcelable