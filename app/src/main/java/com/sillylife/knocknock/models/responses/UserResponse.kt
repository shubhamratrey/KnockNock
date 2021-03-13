package com.sillylife.knocknock.models.responses

import com.google.gson.annotations.SerializedName
import com.sillylife.knocknock.models.UserProfile

/**
 * Created on 24/09/18.
 */
class UserResponse(
        @SerializedName("user") val user: UserProfile? = null,
        @SerializedName("is_self") val isSelf: Boolean? = null,
)