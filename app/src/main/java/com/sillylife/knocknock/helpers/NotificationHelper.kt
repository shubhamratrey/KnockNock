package com.sillylife.knocknock.helpers

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.request.target.NotificationTarget
import com.sillylife.knocknock.R
import com.sillylife.knocknock.constants.Constants
import com.sillylife.knocknock.constants.NotificationKeys
import com.sillylife.knocknock.services.KnockCallbackReceiver
import com.sillylife.knocknock.utils.ImageManager
import com.sillylife.knocknock.views.activity.MainActivity
import java.util.concurrent.atomic.AtomicInteger

object NotificationHelper {

    const val TAG = "NotificationHelper"

    /* Used to build and start foreground service. */
    private fun startForegroundServices(intent: Intent?, context: Context) {
        Log.d(TAG, "Start foreground service.")

        val data = intent?.extras!!
        val mChannelId = if (data[NotificationKeys.N_CHANNEL_ID] != null) data[NotificationKeys.N_CHANNEL_ID].toString() else "KnockChannelId"
        val mChannelName = if (data[NotificationKeys.N_CHANNEL_NAME] != null) data[NotificationKeys.N_CHANNEL_NAME].toString() else "KnockChannelName"
        val mTitle = if (data[NotificationKeys.TITLE] != null) data[NotificationKeys.TITLE].toString() else ""
        val mDescription = if (data[NotificationKeys.DESCRIPTION] != null) data[NotificationKeys.DESCRIPTION].toString() else ""
        val mUsername = if (data[NotificationKeys.USERNAME] != null) data[NotificationKeys.USERNAME].toString() else "user"
        val mUserPtrId = if (data[NotificationKeys.USER_PTR_ID] != null) data[NotificationKeys.USER_PTR_ID].toString().toInt() else null
        val mSubtext = if (data[NotificationKeys.SUB_TEXT] != null) data[NotificationKeys.SUB_TEXT].toString() else ""
        val imageUrl = if (data[NotificationKeys.IMAGE] != null) data[NotificationKeys.IMAGE].toString() else ""
        val uriString = if (data[NotificationKeys.URI] != null) data[NotificationKeys.URI].toString() else ""
        val mLayoutType = if (data[NotificationKeys.LAYOUT_TYPE] != null) data[NotificationKeys.LAYOUT_TYPE].toString() else null
        val mNotificationId = if (data[NotificationKeys.LAYOUT_TYPE] != null) data.get(NotificationKeys.NOTIFICATION_ID).toString() else 244
        val notificationId = AtomicInteger(0).incrementAndGet()

        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addNextIntentWithParentStack(Intent(context, MainActivity::class.java))
        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)


        val mRemoteHeadUpViews = RemoteViews(context.packageName, R.layout.notification_headsup)
        mRemoteHeadUpViews.setTextViewText(R.id.headsup_title, mTitle)
        mRemoteHeadUpViews.setTextViewText(R.id.headsup_desc, mDescription)
        mRemoteHeadUpViews.setTextViewText(R.id.headsup_userrname, "New Knock • @" + mUsername)
//        mRemoteHeadUpViews.setTextViewText(R.id.headsup_userrname, SpannableStringBuilder().append("New Knock • @").append(mUsername))


        val headsupPendingIntent = PendingIntent.getBroadcast(context, 1200,
                Intent(context, KnockCallbackReceiver::class.java)
                        .putExtra(Constants.ACTION_TYPE, Constants.CallbackActionType.KNOCK_BACK)
                        .putExtra(Constants.USER_PTR_ID, mUserPtrId)
                        .setAction("DIALOG_CALL"),
                PendingIntent.FLAG_UPDATE_CURRENT)

        val fullscreenPendingIntent = PendingIntent.getActivity(context, 1202,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT)
        mRemoteHeadUpViews.setOnClickPendingIntent(R.id.headsup_knockback_tv, headsupPendingIntent)


        // Create notification builder.
        val notificationBuilder: NotificationCompat.Builder? = NotificationCompat.Builder(context, mChannelId)

        // Make head-up notification.
        notificationBuilder?.setFullScreenIntent(PendingIntent.getActivity(context, 0, Intent(), 0), true)
        notificationBuilder?.setContentIntent(fullscreenPendingIntent)
//        notificationBuilder.setContent(mRemoteViews)
        notificationBuilder?.setCustomHeadsUpContentView(mRemoteHeadUpViews)
//        notificationBuilder.setCustomContentView(mRemoteViews)
//        notificationBuilder.setCustomBigContentView(mRemoteViews)
        notificationBuilder?.setContentText("Incoming Audio Call")
//        notificationBuilder?.setContentTitle(mTitle)
        notificationBuilder?.priority = NotificationCompat.PRIORITY_MAX
        notificationBuilder?.setDefaults(Notification.DEFAULT_ALL)
        notificationBuilder?.setCategory(NotificationCompat.CATEGORY_CALL)
        notificationBuilder?.setAutoCancel(true)
        notificationBuilder?.setSmallIcon(R.drawable.ic_launcher_background)
//        notificationBuilder?.setSound(ringUri)
        notificationBuilder?.setOngoing(true)
        var notification: Notification? = null
        if (notificationBuilder != null) {
            notification = notificationBuilder.build()
        }

        ImageManager.loadImageCircular(NotificationTarget(context, R.id.headsup_icon, mRemoteHeadUpViews, notification, notificationId), imageUrl)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notification != null) {
            val channel = NotificationChannel(mChannelId, mChannelName, NotificationManager.IMPORTANCE_HIGH)
            channel.lightColor = Color.BLUE
            channel.description = "Call Notifications"
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            channel.enableLights(true)
            channel.enableVibration(true)
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)

            // Build the notification.
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(1, notification)
            // Start foreground service.
            (context as Service).startForeground(1, notification)
        } else {
            // Build the notification and start foreground service.
            (context as Service).startForeground(1, notification)
        }
    }

}
