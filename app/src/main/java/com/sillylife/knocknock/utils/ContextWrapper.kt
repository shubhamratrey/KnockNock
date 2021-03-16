package com.sillylife.knocknock.utils

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.LocaleList
import java.util.*

object ContextWrapper {

    fun wrap(context: Context, newLocale: Locale): ContextWrapper {
        var mContext = context

        val res = mContext.resources
        val configuration = res.configuration

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                configuration.setLocale(newLocale)
                val localeList = LocaleList(newLocale)
                LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)
                mContext = mContext.createConfigurationContext(configuration)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 -> {
                configuration.setLocale(newLocale)
                mContext = mContext.createConfigurationContext(configuration)
            }
            else -> {
                configuration.locale = newLocale
                res.updateConfiguration(configuration, res.displayMetrics)
            }
        }
        return ContextWrapper(mContext)
    }
}
