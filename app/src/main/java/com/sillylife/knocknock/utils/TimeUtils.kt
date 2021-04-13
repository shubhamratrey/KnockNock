package com.sillylife.knocknock.utils

import android.content.Context
import android.text.format.DateUtils
import com.sillylife.knocknock.MainApplication
import com.sillylife.knocknock.R
import com.sillylife.knocknock.services.sharedpreference.SharedPreferenceManager
import java.text.DateFormat
import java.text.DateFormatSymbols
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created  on 21/08/17.
 */
//        2018-11-19T16:39:43.974104+00:00 (Input date format)
//        2017-09-28T01:00:00+00:00 - (Parsed date format)
object TimeUtils {
    const val SECOND_IN_MILLIS: Long = 1000
    const val MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60
    const val HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60
    const val DAY_IN_MILLIS = HOUR_IN_MILLIS * 24
    const val WEEK_IN_MILLIS = DAY_IN_MILLIS * 7
    private val FORMAT1: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
    private val context = MainApplication.getInstance()

    /**
     * @param time 时间字符串
     * @return Date类型
     */
    fun string2DateFormat(time: String, context: Context?): Date? {
        var language: String = SharedPreferenceManager.getAppLanguage()!! // Helper method to get saved language from SharedPreferences
        if (language.equals("default", ignoreCase = true)) {
            language = "en"
        }
        val locale = Locale(language)
        Locale.setDefault(locale)
        val FORMAT1: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
        FORMAT1.timeZone = TimeZone.getTimeZone("UTC")
        return string2Date(parseDateInput(time), FORMAT1)
    }

    fun string2DateFormat(time: String): Date? {
        var language: String = SharedPreferenceManager.getAppLanguage()!! // Helper method to get saved language from SharedPreferences
        if (language.equals("default", ignoreCase = true)) {
            language = "en"
        }
        val locale = Locale(language)
        Locale.setDefault(locale)
        val FORMAT1: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
        return string2Date(parseDateInput(time), FORMAT1)
    }

    /**
     * @param time   时间字符串
     * @param format 时间格式
     * @return Date类型
     */
    fun string2Date(time: String?, format: DateFormat): Date? {
        try {
            return format.parse(time)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * @return Date类型时间
     */
    val nowDate: Date
        get() = Date()

    fun getDifferenceBetweenTwoDates(dt2: Date, dt1: Date?): String? {
        var resultString: String? = null
        val diff = dt2.time - dt1!!.time
        val diffSeconds = diff / 1000 % 60
        val diffMinutes = diff / (60 * 1000) % 60
        val diffHours = diff / (60 * 60 * 1000)
        val diffInDays = ((dt2.time - dt1.time) / (1000 * 60 * 60 * 24)).toInt()
        resultString = when {
            diffInDays > 7 -> {
                val sdf = SimpleDateFormat("MMM d, yyyy")
                sdf.format(dt1)
            }
            diffInDays >= 1 -> {
                context.resources
                        .getQuantityString(R.plurals.day_count_string,
                                diffInDays,
                                diffInDays)
            }
            diffHours >= 1 -> {
                context.resources
                        .getQuantityString(R.plurals.hour_count_string,
                                diffHours.toInt(),
                                diffHours.toInt())
            }
            diffMinutes >= 1 -> {
                context.resources
                        .getQuantityString(R.plurals.min_count_string,
                                diffMinutes.toInt(),
                                diffMinutes.toInt())
            }
            diffSeconds >= 1 -> {
                context.resources
                        .getQuantityString(R.plurals.second_count_string,
                                diffSeconds.toInt(),
                                diffSeconds.toInt())
            }
            else -> {
                context.getString(R.string.just_now)
            }
        }
        return resultString
    }

    fun getDisplayDateOnlyDays(input: String): String? {
        var dateToDisplay: String? = ""
        try {
            val date = string2DateFormat2(input)
            val currentDate = nowDate
            dateToDisplay = if (isYesterday(date)) {
                context.getString(R.string.yesterday)
            } else {
                getDifferenceBetweenTwoDatesOnlyDays(currentDate, date)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return dateToDisplay
    }

    fun getDifferenceBetweenTwoDatesOnlyDays(dt2: Date, dt1: Date?): String? {
        var resultString: String? = null
        val diff = dt2.time - dt1!!.time
        val diffInDays = ((dt2.time - dt1.time) / (1000 * 60 * 60 * 24)).toInt()
        resultString = if (diffInDays > 7) {
            val sdf = SimpleDateFormat("MMM d, yyyy")
            sdf.format(dt1)
        } else if (diffInDays >= 1) {
            context.resources
                    .getQuantityString(R.plurals.day_count_string,
                            diffInDays,
                            diffInDays)
        } else {
            context.getString(R.string.today)
        }
        return resultString
    }

    fun getDifferenceTimeStringFromDate(dateTime: String): String? {
        val date = string2DateFormat(dateTime, context)
        val currentDate = nowDate
        return getDifferenceBetweenTwoDates(currentDate, date)
    }

    fun getDisplayDate(input: String): String? {
        var dateToDisplay: String? = ""
        try {
            val date = string2DateFormat(input, context)
            val currentDate = nowDate
            dateToDisplay = if (isYesterday(date)) {
                context.getString(R.string.yesterday)
            } else {
                getDifferenceBetweenTwoDates(currentDate, date)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return dateToDisplay
    }

    fun isCustomizedDate(input: String): Boolean {
        try {
            val dt1 = string2DateFormat(input, context)
            val dt2 = nowDate
            return if (isYesterday(dt2)) {
                true
            } else {
                val diffInDays = ((dt2.time - dt1!!.time) / (1000 * 60 * 60 * 24)).toInt()
                diffInDays <= 7
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun isYesterday(d: Date?): Boolean {
        return DateUtils.isToday(d!!.time + DAY_IN_MILLIS)
    }

    fun isToday(d: Date): Boolean {
        return DateUtils.isToday(d.time)
    }

    private fun parseDateInput(input: String): String? {
//        String input1 = "2018-11-21T06:48:32.472090+00:00";
//        String input2 = "2018-11-21T06:48:48+00:00";
        var result: String? = input
        if (result != null && result.contains(".")) {
            val s1 = result.substring(result.lastIndexOf("+"), result.length)
            result = result.substring(0, result.lastIndexOf("."))
            result += s1
        }
        return result
    }

    fun getTimeAgo(timestamp: String): String? {
        var time = timestamp.toLong()
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000
        }
        val now = System.currentTimeMillis()
        if (time > now || time <= 0) {
            return null
        }
        val diff = now - time
        val diffSeconds = diff / 1000 % 60
        val diffMinutes = diff / (60 * 1000) % 60
        val diffHours = diff / (60 * 60 * 1000)
        val diffInDays = ((now - time) / (1000 * 60 * 60 * 24)).toInt()
        return when {
            diffInDays > 7 -> {
                val myDate = Date(time)
                val calendar: Calendar = GregorianCalendar()
                calendar.time = myDate
                val year = calendar[Calendar.YEAR]
                val month = calendar[Calendar.MONTH] + 1
                val day = calendar[Calendar.DAY_OF_MONTH]
                val monthString = DateFormatSymbols().shortMonths[month - 1]
                "$monthString $day, $year"
            }
            diffInDays >= 1 -> {
                context.resources
                        .getQuantityString(R.plurals.day_count_string,
                                diffInDays,
                                diffInDays)
            }
            diffHours >= 1 -> {
                context.resources
                        .getQuantityString(R.plurals.hour_count_string,
                                diffHours.toInt(),
                                diffHours.toInt())
            }
            diffMinutes >= 1 -> {
                context.resources
                        .getQuantityString(R.plurals.min_count_string,
                                diffMinutes.toInt(),
                                diffMinutes.toInt())
            }
            diffSeconds >= 1 -> {
                context.resources
                        .getQuantityString(R.plurals.second_count_string,
                                diffSeconds.toInt(),
                                diffSeconds.toInt())
            }
            else -> {
                context.getString(R.string.just_now)
            }
        }
    }

    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     */
    fun milliSecondsToTimer(milliseconds: Long): String {
        var finalTimerString = ""
        val secondsString: String

        // Convert total duration into time
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
        // Add hours if there
        if (hours > 0) {
            finalTimerString = "$hours:"
        }
        if (minutes < 10) {
            finalTimerString += "0"
        }

        // Prepending 0 to seconds if it is one digit
        secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "" + seconds
        }
        finalTimerString += "$minutes:$secondsString"

        // return timer string
        return finalTimerString
    }

    /**
     * Returns time from "yyyy-MM-dd" pattern
     */
    fun string2DateFormat2(time: String): Date? {
        var language: String = SharedPreferenceManager.getAppLanguage()!! // Helper method to get saved language from SharedPreferences
        if (language.equals("default", ignoreCase = true)) {
            language = "en"
        }
        val locale = Locale(language)
        Locale.setDefault(locale)
        val FORMAT1: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
        FORMAT1.timeZone = TimeZone.getTimeZone("UTC")
        return string2Date(parseDateInput(time), FORMAT1)
    }

    fun getDisplayDate2(input: String): String? {
        var dateToDisplay: String? = ""
        try {
            val date = string2DateFormat2(input)
            val currentDate = nowDate
            dateToDisplay = if (isYesterday(date)) {
                context.getString(R.string.yesterday)
            } else {
                getDifferenceBetweenTwoDates(currentDate, date)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return dateToDisplay
    }

    fun string2DateFormat3(time: String): Date? {
        var language: String = SharedPreferenceManager.getAppLanguage()!! // Helper method to get saved language from SharedPreferences
        if (language.equals("default", ignoreCase = true)) {
            language = "en"
        }
        val locale = Locale(language)
        Locale.setDefault(locale)
        val FORMAT1: DateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        FORMAT1.timeZone = TimeZone.getTimeZone("UTC")
        return string2Date(parseDateInput(time), FORMAT1)
    }

    fun getDisplayDate3(input: String): String? {
        var dateToDisplay: String? = ""
        try {
            val date = string2DateFormat3(input)
            val currentDate = nowDate
            dateToDisplay = if (isYesterday(date)) {
                context.getString(R.string.yesterday)
            } else {
                getDifferenceBetweenTwoDates(currentDate, date)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return dateToDisplay
    }

    fun milliSecondsToSeconds(milliseconds: Long): Long {
        return milliseconds / 1000
    }

    fun getSecondsFromStartTimeAndCurrentTime(startTime: Long): Int {
        val l = System.currentTimeMillis() - startTime
        return (l / 1000).toInt()
    }

    fun getEpisodePublishDisplayDate(input: String?): String? {
        val serverFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm aaa")
        val dateFormat = SimpleDateFormat("EEE, MMM d")
        try {
            val date = serverFormat.parse(input)
            val d = dateFormat.format(date.time)
            val t = timeFormat.format(date.time)
            return "$d, $t"
        } catch (e: ParseException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        return null
    }

    fun getDateFromString(input: String?): String? {
        val serverFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateFormat = SimpleDateFormat("MMM d, yyyy -- hh:mm a", Locale.ENGLISH)
        try {
            val date = serverFormat.parse(input)
            return dateFormat.format(date.time)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null
    }

    fun getTotalDuration(context: Context, seconds: Int): String {
        return if (seconds < 60) {
            seconds.toString() + " " + context.resources.getString(R.string.sec)
        } else {
            TimeUnit.SECONDS.toMinutes(seconds.toLong()).toString() + " " + context.resources.getString(R.string.min)
        }
    }

    fun getFormattedTimeFromMilliseconds(context: Context, totalDuration: Int): String {
        var totalDuration = totalDuration
        val secLabel = " " + context.resources.getString(R.string.sec)
        val minLabel = " " + context.resources.getString(R.string.min)
        val hourLabel = " " + context.resources.getString(R.string.hour)

        //fixme made 1 bcz 0 is crashing
        if (totalDuration == 0) {
            totalDuration = 1
        }
        val seconds = totalDuration % 60
        var hour = totalDuration / 60
        val mins = hour % 60
        hour /= 60
        val secStr = if (seconds > 0) if (seconds > 9) seconds.toString() + secLabel else "0$seconds$secLabel" else ""
        val minStr = if (mins > 0) if (mins > 9) mins.toString() + minLabel else "0$mins$minLabel" else ""
        val hourStr = if (hour > 0) if (hour > 9) hour.toString() + hourLabel else "0$hour$hourLabel" else ""
        return (if (hourStr.isEmpty()) "" else "$hourStr ") + (if (minStr.isEmpty()) "" else "$minStr ") + secStr
    }

    fun getFormattedTimeFromMillisecondsWithoutSec(context: Context, totalDuration: Int): String {
        val secLabel = " " + context.resources.getString(R.string.sec)
        val minLabel = " " + context.resources.getString(R.string.min)
        val hourLabel = " " + context.resources.getString(R.string.hour)

        //fixme made 1 bcz 0 is crashing
        if (totalDuration == 0) {
            return ""
        }
        val seconds = totalDuration % 60
        var hour = totalDuration / 60
        val minute = hour % 60
        hour /= 60
        val secStr = if (seconds > 0) if (seconds > 9) seconds.toString() + secLabel else "0$seconds$secLabel" else ""
        val minStr = if (minute > 0) if (minute > 9) minute.toString() + minLabel else "0$minute$minLabel" else ""
        val hourStr = if (hour > 0) if (hour > 9) hour.toString() + hourLabel else "0$hour$hourLabel" else ""
        return if (minute > 0) {
            (if (hourStr.isEmpty()) "" else "$hourStr ") + if (minStr.isEmpty()) "" else minStr
        } else {
            (if (hourStr.isEmpty()) "" else "$hourStr ") + (if (minStr.isEmpty()) "" else "$minStr ") + secStr
        }
    }

    fun getServerFormatFromMillis(timeInMillis: Long): String {
        val date = Date(timeInMillis)
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
        return simpleDateFormat.format(date)
    }
}