package com.sillylife.knocknock.views.module

import com.sillylife.knocknock.models.responses.GenericResponse
import com.sillylife.knocknock.services.CallbackWrapper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class InviteModule(val listener: APIModuleListener) : BaseModule() {


    fun ringBell(profileId: Int) {
        appDisposable.add(apiService
                .ringBell(profileId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<Response<GenericResponse>>() {
                    override fun onSuccess(t: Response<GenericResponse>) {
                        if (t.isSuccessful) {
                            listener.onRingBellApiSuccess(t.body()!!)
                        } else {
                            listener.onApiFailure(t.code(), t.message())
                        }
                    }

                    override fun onFailure(code: Int, message: String) {
                        listener.onApiFailure(code, message)
                    }
                }))
    }

    interface APIModuleListener {
        fun onApiFailure(statusCode: Int, message: String)
        fun onRingBellApiSuccess(response: GenericResponse)
    }
}