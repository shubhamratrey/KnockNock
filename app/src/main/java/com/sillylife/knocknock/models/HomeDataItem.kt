package com.sillylife.knocknock.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HomeDataItem(
        var type: String? = null,
        var title: String? = null,
        @SerializedName("contacts") val contacts: ArrayList<Contact>? = null,
        @SerializedName("has_next") var hasNext: Boolean? = false,
) : Parcelable