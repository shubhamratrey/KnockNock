package com.sillylife.knocknock.services.sharedpreference

import android.text.TextUtils
import com.google.gson.Gson
import com.sillylife.knocknock.models.UserProfile
import com.sillylife.knocknock.services.sharedpreference.SharedPreferences
import com.sillylife.knocknock.utils.CommonUtil


object SharedPreferenceManager {

    val sharedPreferences = SharedPreferences

    private val TAG = SharedPreferenceManager::class.java.simpleName

    private const val FIREBASE_AUTH_TOKEN = "firebase_auth_token"
    private const val FCM_REGISTERED_USER = "fcm_registered_user"
    private const val USER = "user"
    private const val CURRENT_LAT_LONG = "current_lat_long"


    fun storeFirebaseAuthToken(firebaseAuthToken: String) {
        SharedPreferences.setString(FIREBASE_AUTH_TOKEN, firebaseAuthToken)
    }

    fun getFirebaseAuthToken(): String {
        return SharedPreferences.getString(FIREBASE_AUTH_TOKEN, "")!!
    }


    fun isFCMRegisteredOnServer(userId: String?): Boolean {
        return if (userId == null || TextUtils.isEmpty(userId)) {
            false
        } else SharedPreferences.getBoolean(FCM_REGISTERED_USER + userId, false)
    }

    fun setFCMRegisteredOnServer(userId: String) {
        SharedPreferences.setBoolean(FCM_REGISTERED_USER + userId, true)
    }

    fun setUser(user: UserProfile) {
        SharedPreferences.setString(USER, Gson().toJson(user))
    }

    fun getUser(): UserProfile? {
        val raw: String = SharedPreferences.getString(USER, "")!!
        if (!CommonUtil.textIsEmpty(raw)) {
            return Gson().fromJson(raw, UserProfile::class.java)
        }
        return null
    }
}