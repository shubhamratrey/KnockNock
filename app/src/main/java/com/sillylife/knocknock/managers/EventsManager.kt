package com.sillylife.knocknock.managers

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.sillylife.knocknock.BuildConfig
import com.sillylife.knocknock.MainApplication
import kotlin.math.min


object EventsManager {

    private val TAG = EventsManager::class.java.simpleName

    @Volatile
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private var mSessionId = ""

    init {
        init()
    }

    @Synchronized
    private fun init() {
        val context = MainApplication.getInstance()
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
        mFirebaseAnalytics!!.setAnalyticsCollectionEnabled(BuildConfig.DEBUG)
        mSessionId = System.currentTimeMillis().toString()
    }

    fun setEventName(eventName: String): EventBuilder {
        val eventBuilder = EventBuilder()
        eventBuilder.setEventName(eventName)
        return eventBuilder
    }

    fun sendEvent(eventName: String, eventBundle: Bundle) {
        sendEvent(eventName, 1.0, eventBundle)
    }

    @Synchronized
    private fun sendEvent(eventName: String, value: Double, eventBundle: Bundle) {

//        if (!eventBundle.containsKey(BundleConstants.TIMESTAMP)) {
//            eventBundle.putString(BundleConstants.TIMESTAMP, Calendar.getInstance().timeInMillis.toString())
//        }
        if (BuildConfig.DEBUG) {
            val stringBuilder = StringBuilder()
            stringBuilder.append("eventName : $eventName").append(" || ")
            for (key in eventBundle.keySet()) {
                stringBuilder.append("$key : ${eventBundle.get(key)}").append(" || ")
            }
            Log.d(TAG, stringBuilder.toString())
        }
        if (BuildConfig.DEBUG) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
//                eventBundle.putString(Constants.USER_ID, user.uid)
                mFirebaseAnalytics?.setUserId(user.uid)
            }

//            val isLoggedIn = FirebaseAuthUserManager.isUserLoggedIn()
//            eventBundle.putString(BundleConstants.IS_LOGGED_IN, isLoggedIn.toString())
//
//            val appLanguage = SharedPreferenceManager.getAppLanguageEnum().id
//            val appLanguageSlug = SharedPreferenceManager.getAppLanguageEnum().slug

//            eventBundle.putInt(BundleConstants.APP_LANGUAGE_ID, appLanguage)
//            eventBundle.putString(BundleConstants.APP_LANGUAGE_SLUG, appLanguageSlug)

            mFirebaseAnalytics?.logEvent(eventName, eventBundle)
        }
    }

    class EventBuilder {

        private var mBundle = Bundle()
        private var eventName: String = ""
        private var extraValue: Double = 1.0

        fun setEventName(eventName: String) {
            this.eventName = eventName
        }

        fun setExtraValue(value: Double): EventBuilder {
            extraValue = value
            return this
        }

        fun addProperty(key: String, value: Any?): EventBuilder {
            if (value != null) {
                when (value) {
                    is String -> {
                        var a = value.toString()
                        a = a.substring(0, min(a.length, 100))
                        mBundle.putString(key, a)
                    }
                    is Int -> mBundle.putInt(key, value)
                    is Boolean -> mBundle.putBoolean(key, value)
                    is Long -> mBundle.putLong(key, value)
                    is Float -> mBundle.putFloat(key, value)
                }
            } else {
                mBundle.putString(key, "")
            }
            return this
        }

        fun addProperty(key: String, bundle: Bundle): EventBuilder {
            mBundle.putBundle(key, bundle)
            return this
        }

        fun addBundle(bundle: Bundle): EventBuilder {
            mBundle.putAll(bundle)
            return this
        }

        fun send() {
            sendEvent(eventName, extraValue, mBundle)
        }
    }
}