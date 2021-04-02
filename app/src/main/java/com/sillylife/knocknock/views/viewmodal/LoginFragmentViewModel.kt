package com.sillylife.knocknock.views.viewmodal

import android.app.Activity
import com.google.firebase.auth.PhoneAuthCredential
import com.sillylife.knocknock.models.responses.UserResponse
import com.sillylife.knocknock.views.fragments.BaseFragment
import com.sillylife.knocknock.views.module.BaseModule
import com.sillylife.knocknock.views.module.LoginFragmentModule

class LoginFragmentViewModel(fragment: BaseFragment) : BaseViewModel(), LoginFragmentModule.IModuleListener {

    override fun onPhoneAuthCompleted() {
        viewListener.onPhoneAuthCompleted()
    }

    override fun onAuthError(error: String) {
        viewListener.onAuthError(error)
    }

    override fun onCodeSent(verificationId: String) {
        viewListener.onCodeSent(verificationId)
    }

    override fun onAccountExists() {
        viewListener.onAccountExists()
    }

    override fun onGetMeApiSuccess(response: UserResponse) {
        viewListener.onGetMeApiSuccess(response)
    }

    override fun onGetMeApiFailure(statusCode: Int, message: String) {
        viewListener.onGetMeApiFailure(statusCode, message)
    }

    val module = LoginFragmentModule(this)
    val viewListener = fragment as LoginFragmentModule.IModuleListener

    override fun setViewModel(): BaseModule {
        return module
    }

    fun getMe() {
        module.getMe()
    }

    /*fun loginAnonymously() {
        module.loginAnonymously()
    }*/

    fun signInWithPhone(phoneNumber: String, countryCode: String, activity: Activity) {
        module.signInWithPhone(phoneNumber, countryCode, activity)
    }

    fun submitCode(credential: PhoneAuthCredential, mobile: String) {
        module.submitCode(credential, mobile)
    }

    fun resendCode(phoneNumber: String, activity: Activity) {
        module.resendCode(phoneNumber, activity)
    }

}
