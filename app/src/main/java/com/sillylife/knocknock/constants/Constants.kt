package com.sillylife.knocknock.constants


object Constants {

    const val GALLERY: Int = 1000
    const val AUDIO_LIBRARY: Int = 1001
    const val FIREBASE_REMOTE_CONFIG_CACHE_EXPIRATION: Long = 3600

    const val AWS_BUCKET_NAME = "zoopzam"
    const val S3_BASE_URL = "https://zoopzam.s3.ap-south-1.amazonaws.com"

    const val HOME_PAGINATE: String = "home_paginate"

    const val KEY_EVENT_ACTION = "key_event_action"
    const val IMPRESSION = "impression"
    const val KEY_EVENT_EXTRA = "key_event_extra"
    const val IMMERSIVE_FLAG_TIMEOUT = 500L
    const val RECENTLY_LOWER_LIMIT = 6
    val EXTENSION_WHITELIST = arrayOf("JPG")

    interface SocialLinks {
        companion object {
            const val INSTAGRAM = "https://www.instagram.com//"
            const val FACEBOOK = "https://www.facebook.com/"
            const val TWITTER = "https://twitter.com/"
        }
    }

}