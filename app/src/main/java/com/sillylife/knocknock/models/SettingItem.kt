package com.sillylife.knocknock.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SettingItem(var title: String? = null,
                       var subtitle: String? = null,
                       val web_url: String? = null,
                       var type: String? = null) : Parcelable {

    constructor(title: String?, type: String?) : this(null, null, null) {
        this.title = title
        this.type = type
    }
}