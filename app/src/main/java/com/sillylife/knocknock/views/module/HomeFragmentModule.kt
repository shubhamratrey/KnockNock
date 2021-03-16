package com.sillylife.knocknock.views.module

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.sillylife.knocknock.constants.NetworkConstants
import com.sillylife.knocknock.models.Contact
import com.sillylife.knocknock.models.responses.HomeDataResponse
import com.sillylife.knocknock.services.CallbackWrapper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class HomeFragmentModule(val listener: APIModuleListener) : BaseModule() {

    fun getHomeData(pageNo: Int) {
        val hashMap = HashMap<String, String>()
        hashMap[NetworkConstants.API_PATH_QUERY_PAGE] = pageNo.toString()
        appDisposable.add(apiService
                .getHomeData(hashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : CallbackWrapper<Response<HomeDataResponse>>() {
                    override fun onSuccess(t: Response<HomeDataResponse>) {
                        if (t.isSuccessful) {
                            listener.onHomeApiSuccess(t.body()!!)
                        } else {
                            listener.onApiFailure(t.code(), "empty body")
                        }
                    }

                    override fun onFailure(code: Int, message: String) {
                        listener.onApiFailure(code, message)
                    }
                }))
    }

    fun getRealTimeUpdates(executiveId: Int) {
        messagesListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild("status") && dataSnapshot.hasChild("request_id")) {
                    listener.onRealTimeOrderUpdates(dataSnapshot.child("status").value.toString(), dataSnapshot.child("request_id").value.toString().toInt())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                listener.onRealTimeOrderUpdateFailure(error.message)
            }
        }
        firebaseDatabase.child("delivery_order_request/executive_id/${executiveId}").addValueEventListener(messagesListener!!)
    }

    fun getPhoneContacts() {
        listener.onContactPhoneSyncSuccess(ArrayList())
    }


    interface APIModuleListener {
        fun onHomeApiSuccess(response: HomeDataResponse?)
        fun onContactPhoneSyncSuccess(contacts: ArrayList<Contact>)
        fun onApiFailure(statusCode: Int, message: String)
        fun onRealTimeOrderUpdates(status: String, requestId: Int)
        fun onRealTimeOrderUpdateFailure(message: String)
    }
}