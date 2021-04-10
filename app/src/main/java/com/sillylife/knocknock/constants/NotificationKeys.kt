package com.sillylife.knocknock.constants

object NotificationKeys {
    const val NOTIFICATION_CHANNEL_SHOW_PODCAST = "notification_podcast_channel"
    const val N_CHANNEL_NAME = "n_channel_name"
    const val N_CHANNEL_DESCRIPTION = "n_channel_description"
    const val IMAGE = "image"
    const val TITLE = "title"
    const val N_CHANNEL_PRIORITY = "n_channel_priority"
    const val URI = "uri"
    const val N_CHANNEL_ID = "n_channel_id"
    const val LAYOUT_TYPE = "layout_type"
    const val DE_NOTIFICATION_TYPE = "de_notification_type"
    const val TYPE = "type"
    const val DESCRIPTION = "description"
    const val SUB_TEXT = "sub_text"
    const val NOTIFICATION_ID = "notification_id"
    const val IS_KNOCK = "is_knock"
    const val IS_SILENT = "is_silent"
    const val BUTTON_LABEL = "button_label"
    const val NOTIFICATION_SOUND = "notification_sound"
    const val DELIVERY_MEDIUM = "delivery_medium"
    const val N_SOUND_CUCKOO = "cuckoo"
    const val BG_COLOR = "color"
    const val ORDER_STATUS = "order_status"
    const val ORDER_ID = "order_id"

    interface LAYOUT_TYPES {
        companion object {
            const val TEXT_DESC_ACTION_BTN = "text_desc_action_btn"
        }
    }


}