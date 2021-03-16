package com.sillylife.knocknock.views.module

import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sillylife.knocknock.MainApplication
import com.sillylife.knocknock.services.AppDisposable


open class BaseModule {
    val application = MainApplication.getInstance()
    val apiService = application.getAPIService()
    val localDatabase = application.getKnockNockDatabase()
    var appDisposable = AppDisposable()
    var firebaseDatabase = Firebase.database.reference
    var messagesListener: ValueEventListener? = null

    fun onDestroy() {
        if (appDisposable != null) {
            appDisposable.dispose()
        }
        if (messagesListener != null) {
            firebaseDatabase.removeEventListener(messagesListener!!)
        }
    }

    fun getDisposable(): AppDisposable {
        if (appDisposable == null) {
            appDisposable = AppDisposable()
        }
        return appDisposable
    }


}