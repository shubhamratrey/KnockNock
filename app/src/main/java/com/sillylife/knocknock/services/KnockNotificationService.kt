package com.sillylife.knocknock.services

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.IBinder
import android.widget.RemoteViews
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import com.bumptech.glide.request.target.NotificationTarget
import com.sillylife.knocknock.R
import com.sillylife.knocknock.constants.NotificationKeys
import com.sillylife.knocknock.utils.ImageManager
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


class KnockNotificationService : Service() {

    private val mNotificationId = AtomicInteger(0)

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val data = intent?.extras!!
        val mTitle = if (data[NotificationKeys.TITLE] != null) data[NotificationKeys.TITLE].toString() else ""
        val mDescription = if (data[NotificationKeys.DESCRIPTION] != null) data[NotificationKeys.DESCRIPTION].toString() else ""
        val mSubtext = if (data[NotificationKeys.SUB_TEXT] != null) data[NotificationKeys.SUB_TEXT].toString() else ""
        val imageUrl = if (data[NotificationKeys.IMAGE] != null) data[NotificationKeys.IMAGE].toString() else ""
        val uriString = if (data[NotificationKeys.URI] != null) data[NotificationKeys.URI].toString() else ""
        val channelId = if (data[NotificationKeys.N_CHANNEL_ID] != null) data[NotificationKeys.N_CHANNEL_ID].toString() else "KnockChannelId"
        val channelName = if (data[NotificationKeys.N_CHANNEL_NAME] != null) data[NotificationKeys.N_CHANNEL_NAME].toString() else "KnockChannelName"
        val mLayoutType = data[NotificationKeys.LAYOUT_TYPE]

        val mNotificationId: String = data.get(NotificationKeys.NOTIFICATION_ID).toString()
        val notificationId = 120
        try {
            val receiveCallAction = Intent(applicationContext, KnockCallbackReceiver::class.java)
            receiveCallAction.putExtra("ConstantApp.CALL_RESPONSE_ACTION_KEY", "ConstantApp.CALL_RECEIVE_ACTION")
            receiveCallAction.putExtra("ACTION_TYPE", "RECEIVE_CALL")
            receiveCallAction.putExtra("NOTIFICATION_ID", notificationId)
            receiveCallAction.action = "RECEIVE_CALL"
            val cancelCallAction = Intent(applicationContext, KnockCallbackReceiver::class.java)
            cancelCallAction.putExtra("ConstantApp.CALL_RESPONSE_ACTION_KEY", "ConstantApp.CALL_CANCEL_ACTION")
            cancelCallAction.putExtra("ACTION_TYPE", "CANCEL_CALL")
            cancelCallAction.putExtra("NOTIFICATION_ID", notificationId)
            cancelCallAction.action = "CANCEL_CALL"
            val callDialogAction = Intent(applicationContext, KnockCallbackReceiver::class.java)
            callDialogAction.putExtra("ACTION_TYPE", "DIALOG_CALL")
            callDialogAction.putExtra("NOTIFICATION_ID", notificationId)
            callDialogAction.action = "DIALOG_CALL"
            val receiveCallPendingIntent = PendingIntent.getBroadcast(applicationContext, 1200, receiveCallAction, PendingIntent.FLAG_UPDATE_CURRENT)
            val cancelCallPendingIntent = PendingIntent.getBroadcast(applicationContext, 1201, cancelCallAction, PendingIntent.FLAG_UPDATE_CURRENT)
            val callDialogPendingIntent = PendingIntent.getBroadcast(applicationContext, 1202, callDialogAction, PendingIntent.FLAG_UPDATE_CURRENT)
            if (Build.VERSION.SDK_INT >= VERSION_CODES.O) {
                try {
                    val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
                    channel.description = "Call Notifications"
                    channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    Objects.requireNonNull(applicationContext.getSystemService(NotificationManager::class.java)).createNotificationChannel(channel)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val mRemoteViews = RemoteViews(packageName, R.layout.custom_notification_small)
            mRemoteViews.setTextViewText(R.id.notif_title, mTitle)
            mRemoteViews.setTextViewText(R.id.notif_content, mDescription)

//            val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
//            val hungupPendingIntent = PendingIntent.getBroadcast(this, 0, hungupIntent, PendingIntent.FLAG_UPDATE_CURRENT)
//            val answerPendingIntent = PendingIntent.getActivity(this, 0, answerIntent, PendingIntent.FLAG_UPDATE_CURRENT)
//
//            customView.setOnClickPendingIntent(R.id.btnAnswer, answerPendingIntent)
//            customView.setOnClickPendingIntent(R.id.btnDecline, hungupPendingIntent)


            var notificationBuilder: NotificationCompat.Builder? = null
            // Uri ringUri= Settings.System.DEFAULT_RINGTONE_URI;
            notificationBuilder = NotificationCompat.Builder(this, channelId)
                    .setFullScreenIntent(callDialogPendingIntent, true)
//                    .setContentTitle(mTitle)
//                        .setContent(mRemoteViews)
                    .setCustomHeadsUpContentView(mRemoteViews)
//                        .setCustomContentView(mRemoteViews)
//                        .setCustomBigContentView(mRemoteViews)
                    .setContentText("Incoming Audio Call")
                    .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
//                        .addAction(android.R.drawable.ic_menu_call, "Reject call", cancelCallPendingIntent)
//                        .addAction(android.R.drawable.ic_menu_call, "Answer call", receiveCallPendingIntent)
                    .setAutoCancel(true) //.setSound(ringUri)
//                        .setOngoing(true)
            var incomingCallNotification: Notification? = null
            if (notificationBuilder != null) {
                incomingCallNotification = notificationBuilder.build()
            }

            ImageManager.loadImageCircular(NotificationTarget(applicationContext, R.id.notif_icon, mRemoteViews, incomingCallNotification, notificationId), imageUrl)

            startForeground(notificationId, incomingCallNotification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy() // release your media player here
    }
}


//from users.models import *
//from helpers.notification_helper import NotificationHelper
//user_app_info = UserAppInfo.objects.select_related('profile').filter(profile_id=6,is_active=True).latest('created_on')
//NotificationHelper.send_push_notification(data={
//    'image': '',
//    'title': "yoyoyo",
//    'description': 'Click here to more info.',
//    'uri': '',
//    'is_knock': "True",
//    'n_channel_priority': "1"
//}, fcm_ids=[user_app_info.fcm_token])