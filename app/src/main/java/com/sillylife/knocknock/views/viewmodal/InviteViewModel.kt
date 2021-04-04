package com.sillylife.knocknock.views.viewmodal

import com.sillylife.knocknock.models.responses.GenericResponse
import com.sillylife.knocknock.views.fragments.BaseFragment
import com.sillylife.knocknock.views.module.BaseModule
import com.sillylife.knocknock.views.module.InviteModule

class InviteViewModel(fragment: BaseFragment) : BaseViewModel(), InviteModule.APIModuleListener {

    val module = InviteModule(this)
    val listener = fragment as InviteModule.APIModuleListener

    override fun onApiFailure(statusCode: Int, message: String) {
        listener.onApiFailure(statusCode, message)
    }

    override fun onRingBellApiSuccess(response: GenericResponse) {
        listener.onRingBellApiSuccess(response)
    }

    override fun setViewModel(): BaseModule {
        return module
    }

    fun ringBell(profileId: Int) {
        module.ringBell(profileId)
    }
}