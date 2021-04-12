package com.sillylife.knocknock.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.sillylife.knocknock.database.entities.ContactsEntity

@Dao
interface ContactsDao : BaseDao<ContactsEntity> {

    @Query("SELECT * FROM contacts WHERE phone LIKE :query OR name LIKE :query ORDER BY id ASC")
    fun searchContacts(query: String): List<ContactsEntity>?

    @Query("SELECT * FROM contacts ORDER BY id ASC")
    fun getContactsList(): List<ContactsEntity>?

    @Query("SELECT * from contacts WHERE last_connected IS NOT NULL ORDER BY last_connected DESC LIMIT :limit")
    fun getLastConnectedContactsListByLimit(limit: Int): List<ContactsEntity>

    @Query("SELECT * from contacts WHERE last_connected IS NOT NULL ORDER BY last_connected DESC LIMIT :limit")
    fun getLastConnectedContactsLiveListByLimit(limit: Int): LiveData<List<ContactsEntity>>

    @Query("UPDATE contacts SET last_connected = :timeStamp where phone = :phone")
    fun updateLastConnected(timeStamp: Long, phone: String)

    @Query("UPDATE contacts SET available_on_platform = :availableOnPlatform, image =:image, username =:username, latitude =:lat, longitude =:long, user_ptr_id=:userPtrId where phone = :phone")
    fun updateSomeData(availableOnPlatform: Boolean, image: String, username: String, userPtrId: Int, phone: String, lat:String?, long:String?)

    @Query("UPDATE contacts SET has_invited = :invited where phone = :phone")
    fun updateContactInvited(invited: Boolean, phone: String)

    @Query("SELECT * from contacts WHERE available_on_platform IS NOT NULL AND available_on_platform == 1 ORDER BY name DESC LIMIT :limit")
    fun getAvailableContactsListByLimit(limit: Int): List<ContactsEntity>
}

