package com.sillylife.knocknock.services.sharedpreference

import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sillylife.knocknock.models.Contact
import com.sillylife.knocknock.models.UserProfile
import com.sillylife.knocknock.utils.CommonUtil
import com.sillylife.knocknock.utils.TimeUtils


object SharedPreferenceManager {

    private val sharedPreferences = SharedPreferences

    private val TAG = SharedPreferenceManager::class.java.simpleName

    private const val FIREBASE_AUTH_TOKEN = "firebase_auth_token"
    private const val FCM_REGISTERED_USER = "fcm_registered_user"
    private const val USER = "user"
    private const val APP_LANGUAGE = "app_language"
    private const val RECENTLY_CONNECTED_CONTACT_LIST = "RECENTLY_CONNECTED_CONTACT_LIST"
    private const val CURRENT_LAT_LONG = "current_lat_long"


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

    fun storeRecentlyConnectedContacts(contacts: ArrayList<Contact>) {
        sharedPreferences.setString(RECENTLY_CONNECTED_CONTACT_LIST, Gson().toJson(contacts))
    }

    fun getRecentlyConnectedContacts(): ArrayList<Contact> {
        val contacts: ArrayList<Contact> = ArrayList()
        val raw = sharedPreferences.getString(RECENTLY_CONNECTED_CONTACT_LIST, "")
        if (CommonUtil.textIsNotEmpty(raw)) {
            contacts.addAll(Gson().fromJson(raw, object : TypeToken<ArrayList<Contact>>() {}.type))
        }
        return contacts
    }

    fun addRecentlyConnectedContact(contact: Contact): Boolean {
        val newContactsList: ArrayList<Contact> = ArrayList()
        val recentlyConnectedContacts = getRecentlyConnectedContacts()
        if (recentlyConnectedContacts.isNotEmpty() && recentlyConnectedContacts.size > 0) {
            newContactsList.addAll(recentlyConnectedContacts)
        }
        var isFound = false
        for (savedContact in newContactsList) {
            if (savedContact == contact) {
                isFound = true
                savedContact.lastConnected = TimeUtils.nowDate
                break
            }
        }
        if (!isFound) {
            contact.lastConnected = TimeUtils.nowDate
            newContactsList.add(contact)
        }
        storeRecentlyConnectedContacts(newContactsList)
        return isFound
    }

    fun removeRecentlyConnectedContact(contact: Contact): Boolean {
        val newContactsList: ArrayList<Contact> = ArrayList()
        val recentlyConnectedContacts = getRecentlyConnectedContacts()
        if (recentlyConnectedContacts.isNotEmpty() && recentlyConnectedContacts.size > 0) {
            newContactsList.addAll(recentlyConnectedContacts)
        }
        var isFound = false
        for (savedContact in newContactsList) {
            if (savedContact == contact) {
                isFound = true
                newContactsList.remove(contact)
                break
            }
        }
        storeRecentlyConnectedContacts(newContactsList)
        return isFound
    }

}