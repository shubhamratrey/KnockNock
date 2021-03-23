package com.sillylife.knocknock.models.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.sillylife.knocknock.models.Contact
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SyncedContactsResponse(
        @SerializedName("contacts") var contacts: ArrayList<Contact>? = null,
        @SerializedName("has_more") var hasMore: Boolean? = null
) : Parcelable