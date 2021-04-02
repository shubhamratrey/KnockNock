package com.sillylife.knocknock.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sillylife.knocknock.R
import com.sillylife.knocknock.constants.BundleConstants
import com.sillylife.knocknock.constants.IntentConstants
import com.sillylife.knocknock.constants.NotificationKeys
import com.sillylife.knocknock.managers.FirebaseAuthUserManager
import com.sillylife.knocknock.services.sharedpreference.SharedPreferenceManager
import com.sillylife.knocknock.utils.CommonUtil
import com.sillylife.knocknock.utils.ImageManager
import com.sillylife.knocknock.views.activity.MainActivity
import java.util.concurrent.ExecutionException
import java.util.concurrent.atomic.AtomicInteger

class FirebaseMessaging : FirebaseMessagingService() {

    private val TAG = FirebaseMessaging::class.java.simpleName
    private val mNotificationId = AtomicInteger(0)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.
        Log.d(TAG, remoteMessage.data.toString() + "  " + remoteMessage.notification.toString())
        val isNotificationEnabled = !SharedPreferenceManager.isNotificationsPaused()
        if (remoteMessage.data != null && isNotificationEnabled) {
            showNotification(remoteMessage.data, applicationContext)
        }
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        FirebaseAuthUserManager.registerFCMToken()
    }

    @SuppressLint("ObsoleteSdkInt")
    fun showNotification(data: Map<String, String>, context: Context) {
        val mTitle = data[NotificationKeys.TITLE]
        val mDescription = data[NotificationKeys.DESCRIPTION]
        val mSubtext = data[NotificationKeys.SUB_TEXT]
        val imageUrl = data[NotificationKeys.IMAGE]
        val uriString = data[NotificationKeys.URI]
        val mLayoutType = data[NotificationKeys.LAYOUT_TYPE]

        val mNotificationId: String = data.getOrElse(NotificationKeys.NOTIFICATION_ID) { "not available" }

        val uri = if (CommonUtil.textIsEmpty(uriString)) null else Uri.parse(uriString)
        var image: Bitmap? = null
        try {
            if (!CommonUtil.textIsEmpty(imageUrl)) {
                val bitmap = ImageManager.getBitmapSync(imageUrl!!, 1024, 500)
                image = bitmap
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }

        var channelId = data[NotificationKeys.N_CHANNEL_ID]
        var channelName = data[NotificationKeys.N_CHANNEL_NAME]
        val channelDescription = data[NotificationKeys.N_CHANNEL_DESCRIPTION]
        var channelPriority = NotificationManager.IMPORTANCE_MAX
        if (data.containsKey(NotificationKeys.N_CHANNEL_PRIORITY)) {
            channelPriority = Integer.parseInt(data[NotificationKeys.N_CHANNEL_PRIORITY]!!)
        }

        if (CommonUtil.textIsEmpty(channelId) || CommonUtil.textIsEmpty(channelName)) {
            channelId = NotificationKeys.NOTIFICATION_CHANNEL_SHOW_PODCAST
            channelName = context.getString(R.string.app_name)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels(channelId!!, channelName!!, channelDescription, channelPriority, context)
        }
        val notificationId = this.mNotificationId.incrementAndGet()
        val notificationBuilder = NotificationCompat.Builder(context, channelId!!)
                .setAutoCancel(true)
                .setContentIntent(getContentIntent(uri, mNotificationId, notificationId, context))
                .setDeleteIntent(getDeleteIntent(uri, mNotificationId, notificationId, context))
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            when (channelPriority) {
                NotificationManager.IMPORTANCE_MAX -> notificationBuilder.priority =
                        NotificationCompat.PRIORITY_MAX
                NotificationManager.IMPORTANCE_HIGH -> notificationBuilder.priority =
                        NotificationCompat.PRIORITY_HIGH
                NotificationManager.IMPORTANCE_DEFAULT -> notificationBuilder.priority =
                        NotificationCompat.PRIORITY_DEFAULT
                NotificationManager.IMPORTANCE_LOW -> notificationBuilder.priority =
                        NotificationCompat.PRIORITY_LOW
            }
        }

        //Notification sound
        val customSound = SharedPreferenceManager.getKnockTone()
        if (CommonUtil.textIsNotEmpty(customSound)) {
            try {
//                val sound = Uri.parse("android.resource://" + applicationContext.packageName + "/" + R.raw.cuckoo_sms)
                val r = RingtoneManager.getRingtone(applicationContext, Uri.parse(customSound))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    r.volume = 1.0f
                }
                r.play()
                Log.d(TAG, r.getTitle(context) + r.audioAttributes)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (mDescription != null) {
            notificationBuilder.setContentText(mDescription)
            notificationBuilder.setContentInfo(mDescription)
        }
        if (mTitle != null) {
            notificationBuilder.setContentTitle(mTitle)
        }
        if (mSubtext != null) {
            notificationBuilder.setSubText(mSubtext)
        }
        if (image != null) {
            notificationBuilder.setLargeIcon(image)
        }
        notificationBuilder.setSound(null)

        if (mLayoutType != null && mLayoutType == NotificationKeys.LAYOUT_TYPES.TEXT_DESC_ACTION_BTN) {
            notificationBuilder.addAction(0, "Click here to view", getContentIntent(uri, mNotificationId, notificationId, context))
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupChannels(channel_id: String, channelName: String, channelDescription: String?, priority: Int, context: Context) {
        val adminChannel = NotificationChannel(channel_id, channelName, priority)
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
//        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//        val audioAttributes = AudioAttributes.Builder()
//                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
//                .build()
//        adminChannel.setSound(null, audioAttributes)

        if (channelDescription != null) {
            adminChannel.description = channelDescription
        }
        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(adminChannel)
    }

    private fun getDeleteIntent(uri: Uri?, notificationId: String, notificationIdInt: Int, context: Context): PendingIntent {
        val bundle = Bundle()
        bundle.putString(BundleConstants.NOTIFICATION_URI, uri.toString())
        bundle.putString(BundleConstants.NOTIFICATION_ID, notificationId)

        val intent = Intent(context, MainActivity::class.java)
        intent.action = IntentConstants.NOTIFICATION_DISMISS
        intent.putExtra(IntentConstants.NOTIFICATION_DISMISS, bundle)
        intent.putExtra(IntentConstants.NOTIFICATION_DISMISS_ID, notificationIdInt)

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    private fun setActionButtonIntent(context: Context, action: String, order_id: String, uri: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(IntentConstants.ACTION, action)
        intent.putExtra(IntentConstants.ORDER_ID, order_id)
        intent.putExtra(IntentConstants.NOTIFICATION_URI, uri)
        return PendingIntent.getActivity(context, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT)
    }

    private fun getContentIntent(uri: Uri?, notificationId: String, notificationIdInt: Int, context: Context): PendingIntent {
        val bundle = Bundle()
        bundle.putString(BundleConstants.NOTIFICATION_ID, notificationId)
        bundle.putString(BundleConstants.NOTIFICATION_URI, uri.toString())
        val intent = Intent(context, MainActivity::class.java)
        intent.data = uri
        intent.putExtra(IntentConstants.NOTIFICATION_TAPPED, bundle)
        intent.putExtra(IntentConstants.NOTIFICATION_DISMISS_ID, notificationIdInt)
        intent.action = IntentConstants.NOTIFICATION_URI
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(context, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT)
    }
}
