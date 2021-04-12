package com.sillylife.knocknock.views.fragments

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.bumptech.glide.request.target.AppWidgetTarget
import com.sillylife.knocknock.R
import com.sillylife.knocknock.constants.Constants
import com.sillylife.knocknock.constants.Constants.CallbackActionType.Companion.WIDGET_PHOTO_CLICKED
import com.sillylife.knocknock.helpers.ContactsHelper
import com.sillylife.knocknock.services.KnockCallbackReceiver
import com.sillylife.knocknock.utils.ImageManager


/**
 * Implementation of App Widget functionality.
 */
class RecentlyKnockedWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
//            val widgetText = context.getString(R.string.appwidget_text)
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.recently_knocked)
//            views.setTextViewText(R.id.appwidget_text, widgetText)

            ContactsHelper.getDBRecentlyConnectedContactList().forEachIndexed { index, contact ->
                when (index) {
                    0 -> {
                        ImageManager.loadImageCircular(AppWidgetTarget(context, R.id.image1, views, appWidgetId), contact.image!!)
                        views.setOnClickPendingIntent(R.id.image1,
                                PendingIntent.getBroadcast(context, 100 + index,
                                        Intent(context, KnockCallbackReceiver::class.java)
                                                .putExtra(Constants.ACTION_TYPE, WIDGET_PHOTO_CLICKED)
                                                .putExtra(Constants.USER_PTR_ID, contact.userPtrId),
                                        PendingIntent.FLAG_UPDATE_CURRENT))
                    }
                    1 -> {
                        ImageManager.loadImageCircular(AppWidgetTarget(context, R.id.image2, views, appWidgetId), contact.image!!)
                        views.setOnClickPendingIntent(R.id.image2,
                                PendingIntent.getBroadcast(context, 100 + index,
                                        Intent(context, KnockCallbackReceiver::class.java)
                                                .putExtra(Constants.ACTION_TYPE, WIDGET_PHOTO_CLICKED)
                                                .putExtra(Constants.USER_PTR_ID, contact.userPtrId),
                                        PendingIntent.FLAG_UPDATE_CURRENT))
                    }
                    2 -> {
                        ImageManager.loadImageCircular(AppWidgetTarget(context, R.id.image3, views, appWidgetId), contact.image!!)
                        views.setOnClickPendingIntent(R.id.image3,
                                PendingIntent.getBroadcast(context, 100 + index,
                                        Intent(context, KnockCallbackReceiver::class.java)
                                                .putExtra(Constants.ACTION_TYPE, WIDGET_PHOTO_CLICKED)
                                                .putExtra(Constants.USER_PTR_ID, contact.userPtrId),
                                        PendingIntent.FLAG_UPDATE_CURRENT))
                    }
                    3 -> {
                        ImageManager.loadImageCircular(AppWidgetTarget(context, R.id.image4, views, appWidgetId), contact.image!!)
                        views.setOnClickPendingIntent(R.id.image4,
                                PendingIntent.getBroadcast(context, 100 + index,
                                        Intent(context, KnockCallbackReceiver::class.java)
                                                .putExtra(Constants.ACTION_TYPE, WIDGET_PHOTO_CLICKED)
                                                .putExtra(Constants.USER_PTR_ID, contact.userPtrId),
                                        PendingIntent.FLAG_UPDATE_CURRENT))
                    }
                }
            }

            AppWidgetManager.getInstance(context).updateAppWidget(ComponentName(context, RecentlyKnockedWidget::class.java), views)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}
