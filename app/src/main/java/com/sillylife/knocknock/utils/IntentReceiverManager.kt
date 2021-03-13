package com.sillylife.knocknock.utils

import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.sillylife.knocknock.services.ConnectivityReceiver

class IntentReceiverManager(var fragmentActivity: FragmentActivity) {

    companion object {
        const val PATH_PRODUCT_DETAIL = "product"
        const val PATH_PRODUCT_LIST = "products"
        const val PATH_ORDER = "order"
        const val PATH_ACTIVE = "active"
        const val PATH_REQUEST = "request"
        const val PATH_PAST = "past"

        const val PATH_WEBVIEW = "webview"

    }

    private val TAG = IntentReceiverManager::class.java.simpleName

    interface IntentReceiverListener {
        fun openProductFragment(id: Int?, slug: String?)
        fun openRetailerProductsFragment()
        fun openOrdersFragment(type: String)
        fun openOrderFragment(id: Int)
        fun openWebViewViaIntent(url: String)
        fun onFailed()
    }

    fun process(data: Uri, listener: IntentReceiverListener?) {
        if (!ConnectivityReceiver.isConnected(fragmentActivity)) {
            listener?.onFailed()
        }
        val pathSegments = data.pathSegments
//        val referrer = data.getQueryParameter("refer")
        if (pathSegments != null && pathSegments.size > 0) {
            when {
                pathSegments.contains(PATH_PRODUCT_DETAIL) && pathSegments.size > 1 -> {
                    if (pathSegments[1].toString().toIntOrNull()?.let { true } == true) {
                        listener?.openProductFragment(id = Integer.valueOf(pathSegments[1]), slug = null)
                    } else {
                        listener?.openProductFragment(slug = pathSegments[1], id = null)
                    }
                }
                pathSegments.contains(PATH_PRODUCT_LIST) -> {
                    listener?.openRetailerProductsFragment()
                }
                pathSegments.contains(PATH_ORDER) -> {
                    if (pathSegments[1] is String && pathSegments[1] in arrayOf(PATH_ACTIVE, PATH_REQUEST, PATH_PAST)) {
                        listener?.openOrdersFragment(pathSegments[1])
                    } else {
                        listener?.openOrderFragment(Integer.valueOf(pathSegments[1]))
                    }
                }
                pathSegments.contains(PATH_WEBVIEW) -> {
                    if (pathSegments.size > 0) {
                        val url = StringBuilder()
                        if ((pathSegments[1] == "http:" || pathSegments[1] == "https:")) {
                            url.append(pathSegments[1]).append("//")
                            if (pathSegments.size > 2) {
                                for (i in 2 until pathSegments.size) {
                                    url.append(pathSegments[i])
                                    if (i < pathSegments.size - 1) {
                                        url.append("/")
                                    }
                                }
                            }
                            listener?.openWebViewViaIntent(url.toString())
                        } else {
                            url.append("http://")
                            if (pathSegments.size >= 1) {
                                for (i in 1 until pathSegments.size) {
                                    url.append(pathSegments[i])
                                    if (i < pathSegments.size - 1) {
                                        url.append("/")
                                    }
                                }
                            }
                            listener?.openWebViewViaIntent(url.toString())
                        }
                    } else {
                        listener?.onFailed()
                    }
                }
            }
        } else {
            listener?.onFailed()
        }
    }
}