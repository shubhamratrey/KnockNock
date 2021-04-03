package com.sillylife.knocknock.services.sharedpreference

import android.text.TextUtils
import com.google.gson.Gson
import com.sillylife.knocknock.models.UserProfile
import com.sillylife.knocknock.utils.CommonUtil


object SharedPreferenceManager {

    private val sharedPreferences = SharedPreferences

    private val TAG = SharedPreferenceManager::class.java.simpleName

    private const val FIREBASE_AUTH_TOKEN = "firebase_auth_token"
    private const val FCM_REGISTERED_USER = "fcm_registered_user"
    private const val USER = "user"
    private const val APP_LANGUAGE = "app_language"
    private const val PAUSE_NOTIFICATIONS = "pause_notifications"
    private const val KNOCK_TONE = "knock_tone"
    private const val ADVERTISING_ID = "advertising_id"
    private const val CONTACT_SYNC_WITH_NETWORK = "contact_sync_with_network"


    fun storeFirebaseAuthToken(firebaseAuthToken: String) {
        sharedPreferences.setString(FIREBASE_AUTH_TOKEN, firebaseAuthToken)
    }

    fun getFirebaseAuthToken(): String {
        return sharedPreferences.getString(FIREBASE_AUTH_TOKEN, "")!!
    }

    fun isFCMRegisteredOnServer(userId: String?): Boolean {
        return if (userId == null || TextUtils.isEmpty(userId)) {
            false
        } else sharedPreferences.getBoolean(FCM_REGISTERED_USER + userId, false)
    }

    fun setFCMRegisteredOnServer(userId: String) {
        sharedPreferences.setBoolean(FCM_REGISTERED_USER + userId, true)
    }

    fun setUser(user: UserProfile) {
        sharedPreferences.setString(USER, Gson().toJson(user))
    }

    fun getUser(): UserProfile? {
        val raw: String = sharedPreferences.getString(USER, "")!!
        if (!CommonUtil.textIsEmpty(raw)) {
            return Gson().fromJson(raw, UserProfile::class.java)
        }
        return null
    }

    fun getAppLanguage(): String? {
        return sharedPreferences.getString(APP_LANGUAGE, "en")
    }

    fun setAppLanguage(language: String) {
        sharedPreferences.setString(APP_LANGUAGE, language)
    }

    fun isNotificationsPaused(): Boolean {
        return sharedPreferences.getBoolean(PAUSE_NOTIFICATIONS, false)
    }

    fun pauseNotifications() {
        sharedPreferences.setBoolean(PAUSE_NOTIFICATIONS, false)
    }

    fun resumeNotifications() {
        sharedPreferences.setBoolean(PAUSE_NOTIFICATIONS, true)
    }

    fun getKnockTone(): String? {
        return sharedPreferences.getString(KNOCK_TONE, null)
    }

    fun setKnockTone(url: String) {
        sharedPreferences.setString(KNOCK_TONE, url)
    }

    fun setAdvertisingId(id: String) {
        sharedPreferences.setString(ADVERTISING_ID, id)
    }

    fun getAdvertisingId(): String {
        return sharedPreferences.getString(ADVERTISING_ID, "") ?: ""
    }

    fun isContactSyncingWithNetwork(): Boolean {
        return sharedPreferences.getBoolean(CONTACT_SYNC_WITH_NETWORK, true)
    }

    fun enableContactSyncWithNetwork() {
        sharedPreferences.setBoolean(CONTACT_SYNC_WITH_NETWORK, true)
    }

    fun disableContactSyncWithNetwork() {
        sharedPreferences.setBoolean(CONTACT_SYNC_WITH_NETWORK, false)
    }
}