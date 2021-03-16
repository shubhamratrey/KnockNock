package com.sillylife.knocknock.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Patterns
import android.view.inputmethod.InputMethodManager
import androidx.annotation.DimenRes
import com.sillylife.knocknock.MainApplication
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

object CommonUtil {

    val context = MainApplication.getInstance()
    var priorityAppList: MutableList<String> = ArrayList()

    /**
     * convert dimens to exact pixels
     */
    fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }

    /**
     * Checks whether text is null or empty or not
     */
    fun textIsEmpty(value: String?): Boolean {
        if (value == null)
            return true
        var empty = false
        val message = value.trim { it <= ' ' }
        if (message.isEmpty()) {
            empty = true
        }
        val isWhitespace = message.matches("^\\s*$".toRegex())
        if (isWhitespace) {
            empty = true
        }
        return empty
    }

    fun textIsNotEmpty(value: String?): Boolean {
        return !textIsEmpty(value)
    }

    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    fun getDimensionPixelSize(@DimenRes dimenRes: Int): Int {
        return context.resources.getDimensionPixelSize(dimenRes)
    }

    fun showKeyboard(context: Context?) {
        val inputMethodManager =
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    fun hideKeyboard(context: Context) {
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val v = (context as Activity).currentFocus ?: return
        inputManager.hideSoftInputFromWindow(v.windowToken, 0)
    }

    fun isAppInstalled(context: Context, uri: String): Boolean {
        val pm = context.packageManager
        val appInstalled: Boolean
        appInstalled = try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        } catch (e: RuntimeException) {
            false
        }
        return appInstalled
    }

    // A placeholder username validation check
    fun isValidEmail(email: String): Boolean {
        return if (email.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        } else if (!email.contains("@")) {
            false
        } else {
            email.isNotBlank()
        }
    }

    // A placeholder phone number validation check
    fun isValidMobile(phone: String): Boolean {
        return if (phone.isNotBlank() && phone.length < 7) {
            false
        } else {
            Patterns.PHONE.matcher(phone).matches() && phone.isNotBlank()
        }
    }

    // A placeholder upi address validation check
    fun isValidUPI(email: String): Boolean {
        return if (!email.contains("@")) {
            false
        } else {
            email.isNotBlank()
        }
    }

}