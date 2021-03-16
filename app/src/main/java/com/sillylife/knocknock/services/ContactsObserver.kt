package com.sillylife.knocknock.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat

class ContactsObserver : ContentObserver {
    private var context: Context? = null

    constructor(handler: Handler?) : super(handler) {}
    constructor(handler: Handler?, context: Context?) : super(handler) {
        this.context = context
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        if (!selfChange) {
            try {
                if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    val cr = context!!.contentResolver
                    val cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
                    if (cursor != null && cursor.count > 0) {
                        //moving cursor to last position
                        //to get last element added
                        cursor.moveToLast()
                        var contactName: String? = null
                        val photo: String? = null
                        var contactNumber: String? = null
                        val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                        if (cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)).toInt() > 0) {
                            val pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(id), null)
                            if (pCur != null) {
                                while (pCur.moveToNext()) {
                                    contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                    if (contactNumber != null && contactNumber.length > 0) {
                                        contactNumber = contactNumber.replace(" ", "")
                                    }
                                    contactName = pCur.getString(pCur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                                    val msg = "Name : $contactName Contact No. : $contactNumber"
                                    Log.d("ContactsObserver", msg)
                                    //Displaying result
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                                pCur.close()
                            }
                        }
                        cursor.close()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}