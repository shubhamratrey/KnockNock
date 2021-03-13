package com.sillylife.knocknock.constants

enum class RxEventType {
    RETAILER_DETAIL_UPDATED,
    ORDER_SWIPED_PROCESSED,
    REFRESH_HOME,
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