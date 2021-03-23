package com.sillylife.knocknock.views.module

import com.sillylife.knocknock.constants.NetworkConstants
import com.sillylife.knocknock.events.IBaseView
import com.sillylife.knocknock.models.responses.UserResponse
import com.sillylife.knocknock.services.CallbackWrapper
import com.sillylife.knocknock.services.sharedpreference.SharedPreferenceManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class ProfileModule(val iModuleListener: IModuleListener) : BaseModule() {


    fun getMe() {
        val hashMap = HashMap<String, String>()
        hashMap[NetworkConstants.API_PATH_QUERY_SHOP_REQ] = true.toString()
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

    fun updateProfile(firstName: RequestBody?, lastName: RequestBody?, username: RequestBody?, avatar: MultipartBody.Part?) {
        appDisposable.add(apiService
                .updateProfile(firstName, lastName, username, avatar)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<Response<UserResponse>>() {
                    override fun onSuccess(t: Response<UserResponse>) {
                        if (t.body() != null) {
                            SharedPreferenceManager.setUser(t.body()?.user!!)
                            iModuleListener.onUpdateProfileApiSuccess(t.body()!!)
                        } else {
                            iModuleListener.onApiFailure(t.code(), t.message())
                        }
                    }

                    override fun onFailure(code: Int, message: String) {
                        iModuleListener.onApiFailure(code, message)
                    }
                })
        )
    }

    interface IModuleListener : IBaseView {
        fun onGetMeApiSuccess(response: UserResponse?)
        fun onUpdateProfileApiSuccess(response: UserResponse)
        fun onApiFailure(statusCode: Int, message: String)
    }
}