package com.sillylife.knocknock.constants

import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.LinearLayoutManager


object Constants {

    const val GALLERY: Int = 1000
    const val FIREBASE_REMOTE_CONFIG_CACHE_EXPIRATION: Long = 3600

    const val AWS_BUCKET_NAME = "zoopzam"
    const val S3_BASE_URL = "https://zoopzam.s3.ap-south-1.amazonaws.com"

    const val PROFILE = "profile"
    const val RETAILER = "retailer"

    const val IMAGE_LIST: String = "image_list"
    const val PRODUCT_ID: String = "product_id"
    const val PRODUCT_SLUG: String = "product_slug"
    const val RETAILER_ID: String = "retailer_id"
    const val ORDER_ID: String = "order_id"
    const val PRODUCT_UPDATED: String = "product_updated"
    const val PRODUCT_DELETED: String = "product_deleted"
    const val ORDER_TYPE: String = "order_type"
    const val PRODUCT_PAGINATE: String = "product_paginate"
    const val PRODUCT_SCROLL: String = "product_scroll"
    const val PRODUCT_HAS_MORE: String = "product_has_more"

    const val HOME_PAGINATE: String = "home_paginate"
    const val RETAILER_PRODUCTS_PAGINATE: String = "retailer_products_paginate"
    const val ORDER_LIST_PAGINATE: String = "order_list_paginate"
    const val HOME_SCROLL: String = "home_scroll"
    const val RETAILER_PRODUCTS_SCROLL: String = "retailer_products_scroll"
    const val ORDER_LIST_SCROLL: String = "order_list_scroll"
    const val ORDER_ACCEPT: String = "accepted"
    const val ORDER_REJECT: String = "declined"
    const val TASK: String = "task"

    const val OPEN = "open"
    const val CLOSED = "closed"
    const val ADDRESS: String = "address"
    const val ADDRESS_PAGINATE: String = "address_paginate"
    const val ADDRESS_SCROLL: String = "address_scroll"
    const val ADD_ADDRESS: String = "add_address"
    const val EDIT_ADDRESS: String = "edit_address"
    const val REMOVE_ADDRESS: String = "remove_address"
    const val SET_AS_DEFAULT_ADDRESS: String = "set_as_default_address"

    const val KEY_EVENT_ACTION = "key_event_action"
    const val IMPRESSION = "impression"
    const val KEY_EVENT_EXTRA = "key_event_extra"
    const val IMMERSIVE_FLAG_TIMEOUT = 500L
    const val RECENTLY_LOWER_LIMIT = 4
    val EXTENSION_WHITELIST = arrayOf("JPG")

    interface HomeType {
        companion object {
            const val RECENTLY_CONNECTED_CONTACTS = "recently_connected_contacts"
            const val AVAILABLE_CONTACTS = "available_contacts"
        }
    }

    interface ContactLayoutType {
        companion object {
            const val VERTICAL_LAYOUT = "vertical_layout"
            const val HORIZONTAL_LAYOUT = "horizontal_layout"
        }
    }

    interface ORDER_STATUS {
        companion object {
            const val ON_WAY = "on-way"
            const val ARRIVED_AT_SHOP = "arrived-at-shop"
            const val ORDER_PICKED = "order-picked"
            const val ARRIVED_AT_CONSUMER = "arrived-at-consumer"
            const val DELIVERED = "delivered"
            const val NOT_DELIVERED = "not-delivered"
        }
    }

    interface SocialLinks {
        companion object {
            const val INSTAGRAM = "https://www.instagram.com//"
            const val FACEBOOK = "https://www.facebook.com/"
            const val TWITTER = "https://twitter.com/"
        }
    }

}