package com.sillylife.knocknock.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sillylife.knocknock.MainApplication
import com.sillylife.knocknock.constants.Constants
import com.sillylife.knocknock.constants.Constants.NotificationActionType
import com.sillylife.knocknock.constants.Constants.USER_PTR_ID
import com.sillylife.knocknock.models.responses.GenericResponse
import com.sillylife.knocknock.utils.CommonUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response


class KnockCallbackReceiver : BroadcastReceiver() {

    private var appDisposable: AppDisposable? = null

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.extras != null) {
            val action: String? = intent.getStringExtra(Constants.ACTION_TYPE)
            if (CommonUtil.textIsNotEmpty(action)) {
                when (action) {
                    NotificationActionType.WIDGET_PHOTO_CLICKED -> {
                        ringBell(intent.getIntExtra(USER_PTR_ID, -1), context)
                    }
                    NotificationActionType.KNOCK_BACK -> {
                        ringBell(intent.getIntExtra(USER_PTR_ID, -1), context)
                    }
                }
            } else {
                // Close the notification after the click action is performed.
                context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
                context.stopService(Intent(context, KnockNotificationService::class.java))
            }
        }
    }

    private fun ringBell(profileId: Int, context: Context) {
        if (profileId == -1) {
            return
        }
        getAppDisposable().add(MainApplication.getInstance().getAPIService()
                .ringBell(profileId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<Response<GenericResponse>>() {
                    override fun onSuccess(t: Response<GenericResponse>) {
                        if (t.isSuccessful && t.body() != null) {
                            // Close the notification after the click action is performed.
                            context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
                            context.stopService(Intent(context, KnockNotificationService::class.java))
                        }
                    }

                    override fun onFailure(code: Int, message: String) {
                        // Close the notification after the click action is performed.
                        context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
                        context.stopService(Intent(context, KnockNotificationService::class.java))
                    }
                }))
    }

    private fun getAppDisposable(): AppDisposable {
        if (appDisposable == null) {
            appDisposable = AppDisposable()
        }
        return appDisposable as AppDisposable
    }
}