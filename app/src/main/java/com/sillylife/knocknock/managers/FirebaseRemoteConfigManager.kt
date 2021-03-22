package com.sillylife.knocknock.managers

import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue
import com.google.firebase.remoteconfig.ktx.get
import com.sillylife.knocknock.BuildConfig
import com.sillylife.knocknock.MainApplication
import com.sillylife.knocknock.R
import com.sillylife.knocknock.constants.Constants

object FirebaseRemoteConfigManager {

    private var remoteConfig: FirebaseRemoteConfig? = null

    init {
        init()
    }

    private fun init() {
        try {
            FirebaseApp.getInstance()
        } catch (e: Exception) {
            FirebaseApp.initializeApp(MainApplication.getInstance())
        }
        remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 60 else 4L * 60 * 60)
                .build()
        remoteConfig!!.setConfigSettingsAsync(configSettings)
        remoteConfig!!.setDefaultsAsync(R.xml.remote_config_defaults)
        fetchRemoteConfig()
    }

    fun fetchRemoteConfig() {
        if (remoteConfig == null) {
            init()
        }
        var cacheExpiration = Constants.FIREBASE_REMOTE_CONFIG_CACHE_EXPIRATION

        if (BuildConfig.DEBUG) {
            cacheExpiration = 0
        }

        remoteConfig!!.fetch(cacheExpiration).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                remoteConfig!!.activate()
            }
        }
    }

    fun getString(param: String): String {
        if (remoteConfig == null) {
            init()
        }
        return remoteConfig!!.getString(param)
    }

    fun getBoolean(param: String): Boolean {
        if (remoteConfig == null) {
            init()
        }
        return remoteConfig!!.getBoolean(param)
    }

    fun getLong(param: String): Long? {
        if (remoteConfig == null) {
            init()
        }
        return remoteConfig!!.getLong(param)
    }

    fun getDouble(param: String): Double? {
        if (remoteConfig == null) {
            init()
        }
        return remoteConfig!!.getDouble(param)
    }

    fun getValue(param: String): String {
        if (remoteConfig == null) {
            init()
        }
        return remoteConfig!![param].asString()
    }
}