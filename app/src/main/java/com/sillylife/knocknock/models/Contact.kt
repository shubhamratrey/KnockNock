package com.sillylife.knocknock.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.sillylife.knocknock.utils.TimeUtils
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created on 24/09/18.
 */

@Parcelize
data class Contact(
        @SerializedName("id") var id: Int? = null,
        @SerializedName("user_ptr_id") var userPtrId: Int? = null,
        @SerializedName("name") var name: String? = null,
        @SerializedName("first_name") var firstName: String? = null,
        @SerializedName("middle_name") var middleName: String? = null,
        @SerializedName("last_name") var lastName: String? = null,
        @SerializedName("phone") var phone: String? = null,
        @SerializedName("image") var image: String? = null,
        @SerializedName("last_connected") var lastConnected: Date? = null,
        @SerializedName("has_invited") var hasInvited: Boolean? = false,
        @SerializedName("available_on_platform") var availableOnPlatform: Boolean? = false,
) : Parcelable {

    fun lastConnectedDateString(): String {
        if (lastConnected == null)
            return "None"
        return TimeUtils.getTimeAgo(lastConnected?.time?.toString()!!)!!
    }

    fun getInitialsName(): String {
        if (name?.isEmpty() == true)
            return ""
        return name?.split(' ')!!
                .mapNotNull {
                    it.firstOrNull()?.toUpperCase().toString()
                }.take(2)
                .reduce {
                    acc, s -> acc + s
                }
    }
}