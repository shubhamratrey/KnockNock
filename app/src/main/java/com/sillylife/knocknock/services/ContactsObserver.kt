package com.sillylife.knocknock.services

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import com.sillylife.knocknock.services.sharedpreference.SharedPreferenceManager

class ContactsObserver : ContentObserver {
    private var context: Context? = null

    constructor(handler: Handler?) : super(handler) {}
    constructor(handler: Handler?, context: Context?) : super(handler) {
        this.context = context
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        if (!selfChange) {
            try {
                SharedPreferenceManager.enableContactSyncWithNetwork()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}