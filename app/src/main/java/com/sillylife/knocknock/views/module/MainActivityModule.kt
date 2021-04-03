package com.sillylife.knocknock.views.module

import android.content.Context
import android.os.AsyncTask
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.sillylife.knocknock.events.IBaseView
import com.sillylife.knocknock.models.responses.GenericResponse
import com.sillylife.knocknock.models.responses.UserResponse
import com.sillylife.knocknock.services.CallbackWrapper
import com.sillylife.knocknock.services.sharedpreference.SharedPreferenceManager
import com.sillylife.knocknock.utils.CommonUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import java.io.IOException
import java.lang.IllegalStateException

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

    fun getAdvertisingId(context: Context) {
        try {
            AsyncTask.execute {
                try {
                    val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
                    val adId = adInfo?.id ?: ""
                    // Use the advertising id
                    if(CommonUtil.textIsNotEmpty(adId)) {
                        iModuleListener.onAdvertisingIdSuccess(adId)
                    } else {
                        iModuleListener.onAdvertisingIdFailure(0, "Empty Advertising ID")
                    }
                } catch (exception: IOException) {
                    // Error handling if needed
                    iModuleListener.onAdvertisingIdFailure(0, "IOException")
                } catch (exception: IllegalStateException) {
                    // Error handling if needed
                    iModuleListener.onAdvertisingIdFailure(0, "IllegalStateException")
                } catch (exception: NullPointerException) {
                    // Error handling if needed
                    iModuleListener.onAdvertisingIdFailure(0, "NullPointerException")
                } catch (exception: GooglePlayServicesRepairableException) {
                    iModuleListener.onAdvertisingIdFailure(0, "GooglePlayServicesRepairableException")
                } catch (exception: GooglePlayServicesNotAvailableException) {
                    iModuleListener.onAdvertisingIdFailure(0, "GooglePlayServicesNotAvailableException")
                }
            }
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }

    interface IModuleListener : IBaseView {
        fun onGetMeApiSuccess(response: UserResponse)
        fun onApiFailure(statusCode: Int, message: String)
        fun onAdvertisingIdSuccess(id: String)
        fun onAdvertisingIdFailure(statusCode: Int, message: String)
    }

}