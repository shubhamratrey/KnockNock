package com.sillylife.knocknock.utils

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * Created on 26/09/18.
 */
object FragmentHelper {

    const val HOME = "home"
    const val TASK = "task"
    const val RETAILER_DETAILS = "retailer_details"
    const val ADD_PRODUCT = "add_product"
    const val PRODUCT = "product"
    const val UPDATE_PRODUCT = "update_product"
    const val COLORS = "colors"
    const val GENDER = "gender"
    const val PRODUCT_TYPE = "product_type"
    const val PRODUCT_LIST = "product_list"
    const val CATEGORY = "category"
    const val HOME_TO_WEBVIEW = "home_to_webview"

    fun replace(@IdRes containerId: Int, fragmentManager: FragmentManager, fragment: Fragment, tag: String) {
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(containerId, fragment, tag)
        fragmentTransaction.commitAllowingStateLoss()
    }

    fun add(@IdRes containerId: Int, fragmentManager: FragmentManager, fragment: Fragment, tag: String) {
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(containerId, fragment, tag)
        fragmentTransaction.addToBackStack(tag)

        val displayedFragment = fragmentManager.findFragmentById(containerId)

        if (displayedFragment != null) {
            fragmentTransaction.hide(displayedFragment)
        }

        fragmentTransaction.commitAllowingStateLoss()
    }

    fun add(@IdRes containerId: Int, fragmentManager: FragmentManager, fragment: Fragment, tag: String,
            enterAnim: Int, exitAnim: Int) {
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(containerId, fragment, tag)
        fragmentTransaction.setCustomAnimations(enterAnim, exitAnim, enterAnim, exitAnim)
        fragmentTransaction.addToBackStack(tag)

        val displayedFragment = fragmentManager.findFragmentById(containerId)

        if (displayedFragment != null) {
            fragmentTransaction.hide(displayedFragment)
        }

        fragmentTransaction.commitAllowingStateLoss()
    }

}