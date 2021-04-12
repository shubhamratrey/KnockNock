package com.sillylife.knocknock.helpers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import android.util.LongSparseArray
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.sillylife.knocknock.MainApplication
import com.sillylife.knocknock.constants.Constants.RECENTLY_LOWER_LIMIT
import com.sillylife.knocknock.constants.RxEventType
import com.sillylife.knocknock.database.MapDbEntities
import com.sillylife.knocknock.database.entities.ContactsEntity
import com.sillylife.knocknock.events.RxBus
import com.sillylife.knocknock.events.RxEvent
import com.sillylife.knocknock.models.Contact
import com.sillylife.knocknock.models.responses.SyncedContactsResponse
import com.sillylife.knocknock.services.AppDisposable
import com.sillylife.knocknock.services.CallbackWrapper
import com.sillylife.knocknock.services.sharedpreference.SharedPreferenceManager
import com.sillylife.knocknock.utils.AsyncTaskAlternative.executeAsyncTask
import com.sillylife.knocknock.utils.CommonUtil
import com.sillylife.knocknock.utils.PhoneNumberUtils.getPhoneNumberWithCountryCode
import com.sillylife.knocknock.utils.PhoneNumberUtils.isValid
import com.sillylife.knocknock.utils.TimeUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import retrofit2.Response

object ContactsHelper {

    private val context = MainApplication.getInstance()
    val mContactsDao = context.getKnockNockDatabase()?.contactsDao()
    private var appDisposable: AppDisposable? = null

    @RequiresPermission(allOf = [Manifest.permission.READ_CONTACTS])
    fun getPhoneContactList(): ArrayList<Contact> {
        val googleAccount = "com.google"
        //https://stackoverflow.com/a/44802016/878126
        val prioritizedAccountTypes = hashSetOf(
                "vnd.sec.contact.phone",
                "com.htc.android.pcsc",
                "com.sonyericsson.localcontacts",
                "com.lge.sync",
                "com.lge.phone",
                "vnd.tmobileus.contact.phone",
                "com.android.huawei.phone",
                "Local Phone Account", "")

        val contactList: ArrayList<Contact> = ArrayList()
        val cursor: Cursor? = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.RawContacts.ACCOUNT_TYPE,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                        ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,
                        ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                ),
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
        if (cursor != null) {
            val mobileNoSet = HashSet<String>()
            val contactIdToAccountTypeMap = LongSparseArray<String>()
            cursor.use {
                val contactIdIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID)
                val accountTypeIndex = it.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE)

                val nameIndex: Int = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val firstNameIndex: Int? = it.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)
                val middleNameIndex: Int? = it.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME)
                val familyNameIndex: Int? = it.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME)

                val numberIndex: Int = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val imageIndex: Int? = it.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)

                var contactId: Long
                var name: String
                var firstName: String?
                var middleName: String?
                var lastName: String?
                var number: String
                var image: String?
                var accountType: String

                while (it.moveToNext()) {
                    contactId = it.getLong(contactIdIndex)
                    accountType = it.getString(accountTypeIndex).orEmpty()

                    number = it.getString(numberIndex)
                    image = if (imageIndex != null) it.getString(imageIndex) else null
                    Log.d("ContactsHelper", "Func: imageIndex - $imageIndex")

                    //Names
                    name = it.getString(nameIndex)
                    firstName = if (firstNameIndex != null && CommonUtil.textIsNotEmpty(it.getString(firstNameIndex))) it.getString(firstNameIndex)?.trim()!! else null
                    middleName = if (middleNameIndex != null && CommonUtil.textIsNotEmpty(it.getString(middleNameIndex))) it.getString(middleNameIndex)?.trim()!! else null
                    lastName = if (familyNameIndex != null && CommonUtil.textIsNotEmpty(it.getString(familyNameIndex))) it.getString(familyNameIndex)?.trim()!! else null
                    if (firstName.isNullOrBlank() && lastName.isNullOrBlank() && middleName.isNullOrBlank())
                        continue

                    number = number.replace(" ", "")
                    if (!isValid(number)) {
                        val tempNumber = number
                        number = getPhoneNumberWithCountryCode(number)
                        Log.d("ContactsHelper", "Func: getPhoneContactList - Invalid Phone Number = $tempNumber, Fixed Number =$number")
                    }
                    if (!mobileNoSet.contains(number)) {
                        val previousAccountType = contactIdToAccountTypeMap.get(contactId)
                        //google account is most prioritized, so we skip current one if previous was of it
                        if (previousAccountType == googleAccount)
                            continue
                        if (accountType != googleAccount && previousAccountType != null && prioritizedAccountTypes.contains(
                                        previousAccountType))
                        //we got now a name of an account that isn't prioritized, but we already had a prioritized one, so ignore
                            continue

                        contactList.add(Contact(name = name, firstName = firstName, middleName = middleName, lastName = lastName, phone = number, image = image))
                        mobileNoSet.add(number)
                        Log.d("ContactsHelper", "Func: getPhoneContactList - Name = $name Phone Number = $number")
                    }
                    contactIdToAccountTypeMap.put(contactId, accountType)
                }
            }
        }
        return contactList
    }

    fun updateLastConnected(phone: String) {
        mContactsDao?.updateLastConnected(TimeUtils.nowDate.time, phone)
    }

    fun updateContactInvited(phone: String) {
        mContactsDao?.updateContactInvited(true, phone)
    }

    fun updateSomeData(availableOnPlatform: Boolean, image: String, username: String, userPtrId: Int, phone: String, lat: String?, long: String?) {
        mContactsDao?.updateSomeData(availableOnPlatform, image, username, userPtrId, phone, lat, long)
    }

    fun updatePhoneContactsToDB() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val phoneContacts = getPhoneContactList()
        val dbContacts = getDBPhoneContactList()
        for (phoneContact in phoneContacts) {
            var toUpdate = false
            for (dbContact in dbContacts) {
                if (phoneContact.phone == dbContact.phone) {
                    toUpdate = true
                    break
                }
            }
            if (toUpdate) {
                mContactsDao?.update(MapDbEntities.contactToEntity(phoneContact))
            } else {
                mContactsDao?.insert(MapDbEntities.contactToEntity(phoneContact))
            }
        }
    }

    fun getDBPhoneContactList(): ArrayList<Contact> {
        val contactList: ArrayList<Contact> = ArrayList()
        val dbContactList = mContactsDao?.getContactsList() as ArrayList<ContactsEntity>?
        if (dbContactList?.size!! < 1) {
            return contactList
        }
        dbContactList.forEach { item ->
            contactList.add(MapDbEntities.contactToEntity(item))
        }
        return contactList
    }

    fun getDBRecentlyConnectedContactList(): ArrayList<Contact> {
        val contactList: ArrayList<Contact> = ArrayList()
        val dbContactList = mContactsDao?.getLastConnectedContactsListByLimit(RECENTLY_LOWER_LIMIT) as ArrayList<ContactsEntity>?
        if (dbContactList?.size!! < 1) {
            return contactList
        }
        dbContactList.forEach { item ->
            contactList.add(MapDbEntities.contactToEntity(item))
        }
        return contactList
    }

    fun getAvailableContactList(): ArrayList<Contact> {
        val contactList: ArrayList<Contact> = ArrayList()
        val dbContactList = mContactsDao?.getAvailableContactsListByLimit(10000) as ArrayList<ContactsEntity>?
        if (dbContactList?.size!! < 1) {
            return contactList
        }
        dbContactList.forEach { item ->
            contactList.add(MapDbEntities.contactToEntity(item))
        }
        return contactList
    }

    fun syncContactsWithNetwork(TAG: String, context: Context) {
        Log.d(TAG, "SyncContacts - Started")
        CoroutineScope(Dispatchers.IO).executeAsyncTask(onPreExecute = {
            Log.d(TAG, "SyncContacts - onPreExecute")
        }, doInBackground = { publishProgress: suspend (progress: Int) -> Unit ->
            Log.d(TAG, "SyncContacts - doInBackground - $publishProgress")
            publishProgress(10) // call `publishProgress` to update progress, `onProgressUpdate` will be called

            ContactsHelper.updatePhoneContactsToDB()
            val dbContacts = ContactsHelper.getDBPhoneContactList()

            val phoneNumberList: ArrayList<String> = arrayListOf()
            dbContacts.forEach {
                phoneNumberList.add(it.phone!!)
            }
            publishProgress(50)
            getAppDisposable().add(
                    MainApplication.getInstance().getAPIService().syncContacts(phoneNumberList)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(object : CallbackWrapper<Response<SyncedContactsResponse>>() {
                                override fun onSuccess(t: Response<SyncedContactsResponse>) {
                                    if (t.isSuccessful && t.body() != null) {
                                        val response = t.body()!!
                                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                                            return
                                        }
                                        val availableContacts = response.contacts!!
                                        for (availableContact in availableContacts) {
                                            var toUpdate = false
                                            var tempContact = Contact()
                                            for (dbContact in dbContacts) {
                                                if (availableContact.phone == dbContact.phone) {
                                                    toUpdate = true
                                                    tempContact = dbContact.copy()
                                                    break
                                                }
                                            }
                                            if (toUpdate) {
                                                if (CommonUtil.textIsNotEmpty(availableContact.image)) {
                                                    tempContact.image = availableContact.image
                                                }
                                                if (CommonUtil.textIsNotEmpty(availableContact.username)) {
                                                    tempContact.username = availableContact.username
                                                }
                                                if (CommonUtil.textIsNotEmpty(availableContact.lat)) {
                                                    tempContact.lat = availableContact.lat
                                                }
                                                if (CommonUtil.textIsNotEmpty(availableContact.long)) {
                                                    tempContact.long = availableContact.long
                                                }
                                                if (availableContact.userPtrId != null) {
                                                    tempContact.userPtrId = availableContact.userPtrId
                                                }
                                                if (availableContact.availableOnPlatform != null) {
                                                    tempContact.availableOnPlatform = availableContact.availableOnPlatform
                                                }
                                                ContactsHelper.updateSomeData(availableOnPlatform = tempContact.availableOnPlatform!!,
                                                        image = tempContact.image!!,
                                                        userPtrId = tempContact.userPtrId!!,
                                                        phone = tempContact.phone!!,
                                                        username = tempContact.username!!,
                                                        lat = tempContact.lat,
                                                        long = tempContact.long)
                                            }
                                        }
                                    }
                                }

                                override fun onFailure(code: Int, message: String) {
                                    SharedPreferenceManager.disableContactSyncWithNetwork()
                                }
                            }))
            publishProgress(100)
            "Result" // send data to "onPostExecute"
        }, onPostExecute = { it ->
            // runs in Main Thread
            // ... here "it" is a data returned from "doInBackground"
            SharedPreferenceManager.disableContactSyncWithNetwork()
            Handler(Looper.getMainLooper()).postDelayed({
                RxBus.publish(RxEvent.Action(RxEventType.CONTACT_SYNCED_WITH_NETWORK))
            }, 5000)
            Log.d(TAG, "SyncContacts - onPostExecute - $it")
        }, onProgressUpdate = {
            // runs in Main Thread
            // ... here "it" contains progress
            Log.d(TAG, "SyncContacts - onProgressUpdate - $it")
        })
        Log.d(TAG, "SyncContacts - Ended")
    }

    private fun getAppDisposable(): AppDisposable {
        if (appDisposable == null) {
            appDisposable = AppDisposable()
        }
        return appDisposable as AppDisposable
    }

}
