package com.sillylife.knocknock.views.activity

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.sillylife.knocknock.R
import com.sillylife.knocknock.constants.Constants
import com.sillylife.knocknock.constants.NotificationKeys
import com.sillylife.knocknock.services.KnockCallbackReceiver
import com.sillylife.knocknock.utils.ImageManager
import kotlinx.android.synthetic.main.activity_onscreendialog.*


class OnScreenDialogActivity : AppCompatActivity() {

    private val TAG = OnScreenDialogActivity::class.java.simpleName

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            this.setTurnScreenOn(true)
            val keyguardManager = this.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            this.window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT or
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
            )
        }
        initiateVibration()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate START")
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
//            setFinishOnTouchOutside(true)
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_onscreendialog)
            initializeContent()
        } catch (e: Exception) {
            Log.d("Exception", e.toString())
            e.printStackTrace()
        }
        Log.d(TAG, "onCreate END")
    }

    private fun initializeContent() {
        Log.d(TAG, "initializeContent START")
        val data = intent?.extras!!
        val mTitle = if (data[NotificationKeys.TITLE] != null) data[NotificationKeys.TITLE].toString() else ""
        val mUserPtrId = if (data[NotificationKeys.USER_PTR_ID] != null) data[NotificationKeys.USER_PTR_ID].toString().toInt() else null
        val imageUrl = if (data[NotificationKeys.IMAGE] != null) data[NotificationKeys.IMAGE].toString() else ""

        textTv?.text = mTitle
        ImageManager.loadImageCircular(photoIv!!, imageUrl)
        dismissBtn?.setOnClickListener {
            sendBroadcast(Intent(this, KnockCallbackReceiver::class.java)
                    .putExtra(Constants.ACTION_TYPE, Constants.CallbackActionType.KNOCK_BACK)
                    .putExtra(Constants.USER_PTR_ID, mUserPtrId))
            dismissPopup()
        }
        textTv?.setOnClickListener {
            dismissPopup()
        }
        photoIv?.setOnClickListener {
            Log.d(TAG, "contactPhoto")
//            dismissPopup()
            startActivity(Intent(this, MainActivity::class.java))
        }


        Log.d(TAG, "initializeContent END")
    }

    private fun dismissPopup() {
        this.finish()
    }

    private fun initiateVibration() {
        val v = this.getSystemService(VIBRATOR_SERVICE) as Vibrator
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
            v.vibrate(500)
        }
    }
}