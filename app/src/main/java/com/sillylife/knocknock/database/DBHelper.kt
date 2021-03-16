package com.sillylife.knocknock.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.sillylife.knocknock.MainApplication
import com.sillylife.knocknock.constants.Constants.RECENTLY_LOWER_LIMIT
import com.sillylife.knocknock.database.dao.ContactsDao
import com.sillylife.knocknock.database.entities.ContactsEntity

class DBHelper(application: Application) : AndroidViewModel(application) {

    val mContactsDao: ContactsDao = MainApplication.getInstance().getKnockNockDatabase()?.contactsDao()!!

    fun searchContactsByPhoneNumberOrName(query: String): ArrayList<ContactsEntity>? {
        return mContactsDao.searchContacts(query) as ArrayList<ContactsEntity>?
    }

    fun getLiveDBRecentlyConnectedContactList(): LiveData<List<ContactsEntity>>? {
        val liveData: LiveData<List<ContactsEntity>> = mContactsDao.getLastConnectedContactsLiveListByLimit(RECENTLY_LOWER_LIMIT)
        val result: MediatorLiveData<List<ContactsEntity>>? = MediatorLiveData()
        var initialized = false
        var lastObj: List<ContactsEntity>? = null
        result?.addSource(liveData) {
            if (!initialized) {
                initialized = true
                lastObj = it
                result.postValue(lastObj)
            } else if ((it == null && lastObj != null) || it != lastObj) {
                lastObj = it
                result.postValue(lastObj)
            }
        }
        return result

    }
}


