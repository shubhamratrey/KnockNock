package com.sillylife.knocknock.constants

enum class RxEventType {
    PROFILE_UPDATED,
    CONTACT_SYNCED_WITH_NETWORK,
    CONTACT_CLICKED,
    PRODUCT_UPDATED,
    PRODUCT_DELETED,
    DEFAULT_ADDRRESS_UPDATE;

    companion object {

        fun get(name: String): RxEventType? {
            for (le in values()) {
                if (le.name.equals(name, ignoreCase = true)) {
                    return le
                }
            }
            return null
        }
    }

}