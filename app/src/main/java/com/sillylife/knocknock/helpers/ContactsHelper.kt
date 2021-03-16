package com.sillylife.knocknock.helpers

import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import com.sillylife.knocknock.MainApplication
import com.sillylife.knocknock.models.Contact
import com.sillylife.knocknock.services.sharedpreference.SharedPreferenceManager
import com.sillylife.knocknock.utils.PhoneNumberUtils.getPhoneNumberWithCountryCode
import com.sillylife.knocknock.utils.PhoneNumberUtils.isValid

object ContactsHelper {

    private val context = MainApplication.getInstance()

    fun getPhoneContactList(): ArrayList<Contact> {
        val contactList: ArrayList<Contact> = ArrayList()
        val cursor: Cursor? = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER),
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
        if (cursor != null) {
            val mobileNoSet = HashSet<String>()
            cursor.use {
                val nameIndex: Int = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val numberIndex: Int = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                var name: String
                var number: String
                while (it.moveToNext()) {
                    name = it.getString(nameIndex)
                    number = it.getString(numberIndex)
                    number = number.replace(" ", "")
                    if (!isValid(number)) {
                        val tempNumber = number
                        number = getPhoneNumberWithCountryCode(number)
                        Log.d("ContactsHelper", "Func: getPhoneContactList - Invalid Phone Number = $tempNumber, Fixed Number =$number")
                    }
                    if (!mobileNoSet.contains(number)) {
                        contactList.add(Contact(name = name, phone = number))
                        mobileNoSet.add(number)
                        Log.d("ContactsHelper", "Func: getPhoneContactList - Name = $name Phone Number = $number")
                    }
                }
            }
        }
        return contactList
    }

    fun getRecentlyConnectedContactList(): ArrayList<Contact> {
        val lastConnectedContacts = SharedPreferenceManager.getRecentlyConnectedContacts()
        lastConnectedContacts.sortByDescending { it.lastConnected }
        return lastConnectedContacts
    }

    fun addRecentlyConnectedContact(contact: Contact): Boolean {
        return SharedPreferenceManager.addRecentlyConnectedContact(contact = contact)
    }

    fun removeLastConnectedContact(contact: Contact): Boolean {
        return SharedPreferenceManager.removeRecentlyConnectedContact(contact = contact)
    }

}
