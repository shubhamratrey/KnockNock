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
        @SerializedName("name") var name: String? = null,
        @SerializedName("slug") var slug: String? = null,
        @SerializedName("sign_up_source") var signUpSource: String? = null,
        @SerializedName("original_avatar") var originalAvatar: String? = null,
        @SerializedName("email") var email: String?,
        @SerializedName("phone") var mobile: String?
) : Parcelable