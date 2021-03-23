package com.sillylife.knocknock.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created on 24/09/18.
 */

@Parcelize
data class UserProfile(
        @SerializedName("id") var id: Int? = null,
        @SerializedName("username") var username: String? = null,
        @SerializedName("first_name") var firstName: String? = null,
        @SerializedName("last_name") var lastName: String? = null,
        @SerializedName("original_avatar") var originalAvatar: String? = null,
        @SerializedName("phone") var phone: String? = null,
        @SerializedName("last_available") var last_available: String? = null
) : Parcelable {

    fun getFullName(): String {
        return "$firstName $lastName"
    }

    fun getUserName(): String {
        return "@${username}"
    }

    fun getInitialsName(): String {
        val name = getFullName()
        if (name.isEmpty())
            return ""
        return name.split(' ')
                .mapNotNull {
                    it.firstOrNull()?.toUpperCase().toString()
                }.take(2)
                .reduce { acc, s ->
                    acc + s
                }
    }
}