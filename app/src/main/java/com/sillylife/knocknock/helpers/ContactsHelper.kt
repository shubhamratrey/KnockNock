package com.sillylife.knocknock.helpers

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import android.util.LongSparseArray
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.sillylife.knocknock.MainApplication
import com.sillylife.knocknock.constants.Constants.RECENTLY_LOWER_LIMIT
import com.sillylife.knocknock.database.MapDbEntities
import com.sillylife.knocknock.database.entities.ContactsEntity
import com.sillylife.knocknock.models.Contact
import com.sillylife.knocknock.utils.CommonUtil
import com.sillylife.knocknock.utils.PhoneNumberUtils.getPhoneNumberWithCountryCode
import com.sillylife.knocknock.utils.PhoneNumberUtils.isValid
import com.sillylife.knocknock.utils.TimeUtils

object ContactsHelper {

    private val context = MainApplication.getInstance()
    val mContactsDao = context.getKnockNockDatabase()?.contactsDao()

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
        dbContactList?.forEach { item ->
            contactList.add(MapDbEntities.contactToEntity(item))
        }
        return contactList
    }

    fun getDBRecentlyConnectedContactList(): ArrayList<Contact> {
        val contactList: ArrayList<Contact> = ArrayList()
        val dbContactList = mContactsDao?.getLastConnectedContactsListByLimit(RECENTLY_LOWER_LIMIT) as ArrayList<ContactsEntity>?
        dbContactList?.forEach { item ->
            contactList.add(MapDbEntities.contactToEntity(item))
        }
        return contactList
    }

    fun getAvailableContactList(): ArrayList<Contact> {
        val contactList: ArrayList<Contact> = ArrayList()
        val dbContactList = mContactsDao?.getAvailableContactsListByLimit(10000) as ArrayList<ContactsEntity>?
        dbContactList?.forEach { item ->
            contactList.add(MapDbEntities.contactToEntity(item))
        }
        return contactList
    }

}
