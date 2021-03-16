package com.sillylife.knocknock.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.sillylife.knocknock.utils.PhoneNumberUtils
import com.sillylife.knocknock.utils.TimeUtils
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created on 24/09/18.
 */

@Parcelize
data class Contact(
        @SerializedName("id") var id: Int? = null,
        @SerializedName("name") var name: String? = null,
        @SerializedName("phone") var phone: String? = null,
        @SerializedName("image") var image: String? = null,
        @SerializedName("last_connected") var lastConnected: Date? = null
) : Parcelable {

    fun isValidPhone(): Boolean {
        return PhoneNumberUtils.isValid(phone!!)
    }

    fun lastConnectedDateString(): String {
        if (lastConnected == null)
            return "None"
        return TimeUtils.getTimeAgo(lastConnected?.time?.toString()!!)!!
    }
}