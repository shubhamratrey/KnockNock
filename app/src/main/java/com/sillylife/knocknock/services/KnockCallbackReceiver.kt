package com.sillylife.knocknock.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sillylife.knocknock.MainApplication
import com.sillylife.knocknock.models.responses.GenericResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response


class KnockCallbackReceiver : BroadcastReceiver() {
    var mContext: Context? = null
    private var appDisposable: AppDisposable? = null
    override fun onReceive(context: Context, intent: Intent) {
        mContext = context
        if (intent.extras != null) {
            var action: String? = ""
            action = intent.getStringExtra("ACTION_TYPE")
            if (action != null && action.equals("WIDGET_PHOTO_CLICKED", ignoreCase = true)) {
                ringBell(intent.getIntExtra("PROFILE_ID", -1))
            } else if (action != null && !action.equals("", ignoreCase = true)) {
                performClickAction(context, action)
            }

            // Close the notification after the click action is performed.
            val iclose = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            context.sendBroadcast(iclose)
            context.stopService(Intent(context, KnockNotificationService::class.java))
        }
    }

    private fun performClickAction(context: Context, action: String) {
//        if (action.equals("RECEIVE_CALL", ignoreCase = true)) {
//            if (checkAppPermissions()) {
//                val intentCallReceive = Intent(mContext, VideoCallActivity::class.java)
//                intentCallReceive.putExtra("Call", "incoming")
//                intentCallReceive.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                mContext!!.startActivity(intentCallReceive)
//            } else {
//                val intent = Intent(AppController.getInstance().getContext(), VideoCallRingingActivity::class.java)
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                intent.putExtra("CallFrom", "call from push")
//                mContext!!.startActivity(intent)
//            }
//        } else if (action.equals("DIALOG_CALL", ignoreCase = true)) {
//            // show ringing activity when phone is locked
//            val intent = Intent(AppController.getInstance().getContext(), VideoCallRingingActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
//            mContext!!.startActivity(intent)
//        } else {
//            context.stopService(Intent(context, KnockingReceiverService::class.java))
//            val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
//            context.sendBroadcast(it)
//        }
    }


    private fun checkAppPermissions(): Boolean {
        return true
//        return hasReadPermissions() && hasWritePermissions() && hasCameraPermissions() && hasAudioPermissions()
    }

    private fun ringBell(profileId: Int) {
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

                        }
                    }

                    override fun onFailure(code: Int, message: String) {
                    }
                }))
    }

//    private fun hasAudioPermissions(): Boolean {
//        return ContextCompat.checkSelfPermission(AppController.getInstance().getContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
//    }
//
//    private fun hasReadPermissions(): Boolean {
//        return ContextCompat.checkSelfPermission(AppController.getInstance().getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
//    }
//
//    private fun hasWritePermissions(): Boolean {
//        return ContextCompat.checkSelfPermission(AppController.getInstance().getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
//    }
//
//    private fun hasCameraPermissions(): Boolean {
//        return ContextCompat.checkSelfPermission(AppController.getInstance().getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
//    }

    private fun getAppDisposable(): AppDisposable {
        if (appDisposable == null) {
            appDisposable = AppDisposable()
        }
        return appDisposable as AppDisposable
    }
}