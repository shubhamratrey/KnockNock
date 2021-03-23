package com.sillylife.knocknock.views.viewmodal

import com.sillylife.knocknock.models.responses.UserResponse
import com.sillylife.knocknock.views.fragments.BaseFragment
import com.sillylife.knocknock.views.module.BaseModule
import com.sillylife.knocknock.views.module.ProfileModule
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ProfileViewModel(activity: BaseFragment) : BaseViewModel(), ProfileModule.IModuleListener {

    override fun onGetMeApiSuccess(response: UserResponse?) {
        viewListener.onGetMeApiSuccess(response)
    }

    override fun onUpdateProfileApiSuccess(response: UserResponse) {
        viewListener.onUpdateProfileApiSuccess(response)
    }

    override fun onApiFailure(statusCode: Int, message: String) {
        viewListener.onApiFailure(statusCode, message)
    }

    val module = ProfileModule(this)
    val viewListener = activity as ProfileModule.IModuleListener
    override fun setViewModel(): BaseModule {
        return module
    }

    fun getMe() {
        module.getMe()
    }

    fun updateProfile(firstName: RequestBody?, lastName: RequestBody?, username: RequestBody?, avatar: MultipartBody.Part?) {
        module.updateProfile(firstName, lastName, username, avatar)
    }
}
