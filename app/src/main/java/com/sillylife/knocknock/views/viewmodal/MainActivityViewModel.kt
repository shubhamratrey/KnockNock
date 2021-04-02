package com.sillylife.knocknock.views.viewmodal

import com.sillylife.knocknock.models.responses.UserResponse
import com.sillylife.knocknock.views.activity.BaseActivity
import com.sillylife.knocknock.views.module.BaseModule
import com.sillylife.knocknock.views.module.MainActivityModule

class MainActivityViewModel(activity: BaseActivity) : BaseViewModel(), MainActivityModule.IModuleListener {

    override fun onGetMeApiSuccess(response: UserResponse) {
        viewListener.onGetMeApiSuccess(response)
    }


    override fun onApiFailure(statusCode: Int, message: String) {
        viewListener.onApiFailure(statusCode, message)
    }

    val module = MainActivityModule(this)
    val viewListener = activity as MainActivityModule.IModuleListener

    override fun setViewModel(): BaseModule {
        return module
    }

    fun getMe() {
        module.getMe()
    }

}
