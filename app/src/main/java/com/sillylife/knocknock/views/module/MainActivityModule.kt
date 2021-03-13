package com.sillylife.knocknock.views.module

import com.sillylife.knocknock.events.IBaseView
import com.sillylife.knocknock.models.responses.GenericResponse
import com.sillylife.knocknock.models.responses.UserResponse
import com.sillylife.knocknock.services.CallbackWrapper
import com.sillylife.knocknock.services.sharedpreference.SharedPreferenceManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class MainActivityModule(val iModuleListener: IModuleListener) : BaseModule() {


    fun getMe() {
        val hashMap = HashMap<String, String>()
        appDisposable.add(apiService
                .getMe(hashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<Response<UserResponse>>() {
                    override fun onSuccess(t: Response<UserResponse>) {
                        if (t.isSuccessful) {
                            SharedPreferenceManager.setUser(t.body()?.user!!)
                            iModuleListener.onGetMeApiSuccess(t.body()!!)
                        } else {
                            iModuleListener.onApiFailure(t.code(), "empty body")
                        }
                    }

                    override fun onFailure(code: Int, message: String) {
                        iModuleListener.onApiFailure(code, message)
                    }
                }))
    }

    fun ringBell(profileId: Int) {
        appDisposable.add(apiService
                .ringBell(profileId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<Response<GenericResponse>>() {
                    override fun onSuccess(t: Response<GenericResponse>) {
                        if (t.isSuccessful) {
                            iModuleListener.onRingBellApiSuccess(t.body()!!)
                        } else {
                            iModuleListener.onApiFailure(t.code(), "empty body")
                        }
                    }

                    override fun onFailure(code: Int, message: String) {
                        iModuleListener.onApiFailure(code, message)
                    }
                }))
    }

    interface IModuleListener : IBaseView {
        fun onGetMeApiSuccess(response: UserResponse)
        fun onRingBellApiSuccess(response: GenericResponse)
        fun onApiFailure(statusCode: Int, message: String)
    }

}