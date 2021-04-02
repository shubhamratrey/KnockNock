package com.sillylife.knocknock.views.viewmodal

import com.sillylife.knocknock.models.Contact
import com.sillylife.knocknock.models.responses.GenericResponse
import com.sillylife.knocknock.models.responses.HomeDataResponse
import com.sillylife.knocknock.models.responses.SyncedContactsResponse
import com.sillylife.knocknock.views.fragments.BaseFragment
import com.sillylife.knocknock.views.module.BaseModule
import com.sillylife.knocknock.views.module.HomeFragmentModule

class HomeFragmentViewModel(fragment: BaseFragment) : BaseViewModel(), HomeFragmentModule.APIModuleListener {

    val module = HomeFragmentModule(this)
    val listener = fragment as HomeFragmentModule.APIModuleListener
    override fun onContactPhoneSyncSuccess(response: SyncedContactsResponse) {
        listener.onContactPhoneSyncSuccess(response)
    }

    override fun onApiFailure(statusCode: Int, message: String) {
        listener.onApiFailure(statusCode, message)
    }

//    override fun onRealTimeOrderUpdates(status: String, requestId: Int) {
//        listener.onRealTimeOrderUpdates(status, requestId)
//    }
//
//    override fun onRealTimeOrderUpdateFailure(message: String) {
//        listener.onRealTimeOrderUpdateFailure(message)
//    }
//
//    override fun onHomeApiSuccess(response: HomeDataResponse?) {
//        listener.onHomeApiSuccess(response)
//    }
//
//    override fun onContactPhoneSyncSuccess(contacts: ArrayList<Contact>) {
//        listener.onContactPhoneSyncSuccess(contacts)
//    }


    override fun onRingBellApiSuccess(response: GenericResponse) {
        listener.onRingBellApiSuccess(response)
    }

    override fun setViewModel(): BaseModule {
        return module
    }

//    fun getHomeData(pageNo: Int) {
//        module.getHomeData(pageNo)
//    }
//
//
//    fun getRealTimeUpdates(executiveId: Int) {
//        module.getRealTimeUpdates(executiveId)
//    }

    fun getPhoneContacts(phoneNumbers: ArrayList<String>) {
        module.getPhoneContacts(phoneNumbers)
    }

    fun ringBell(profileId: Int) {
        module.ringBell(profileId)
    }
}