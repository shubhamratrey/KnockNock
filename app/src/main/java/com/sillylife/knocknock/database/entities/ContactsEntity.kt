package com.sillylife.knocknock.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created on 24/09/18.
 */
@Entity(tableName = "contacts")
data class ContactsEntity(
        @PrimaryKey(autoGenerate = true) var id: Int = 0,
        @ColumnInfo(name = "user_ptr_id") var userPtrId: Int? = null,
        var name: String? = null,
        @ColumnInfo(name = "first_name") var firstName: String? = null,
        @ColumnInfo(name = "middle_name") var middleName: String? = null,
        @ColumnInfo(name = "last_name") var lastName: String? = null,
        var phone: String? = null,
        var image: String? = null,
        @ColumnInfo(name = "last_connected") var lastConnected: Long? = null,
        @ColumnInfo(name = "has_invited") var hasInvited: Boolean? = false,
        @ColumnInfo(name = "available_on_platform") var availableOnPlatform: Boolean? = false,
        var raw: String? = null)