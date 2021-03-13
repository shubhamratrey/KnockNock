package com.sillylife.knocknock.views.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.sillylife.knocknock.views.activity.BaseActivity

open class BaseFragment : Fragment() {

    private var mContext: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = context
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun showToast(message: String, length: Int?=0) {
        getBaseActivity().showToast(message, length)
    }

    fun getBaseActivity(): BaseActivity {
        return (mContext as FragmentActivity) as BaseActivity
    }

    fun addFragment(fragment: Fragment, tag: String? = null) {
        getBaseActivity().addFragment(fragment, tag)
    }

    fun replaceFragment(fragment: Fragment, tag: String) {
        getBaseActivity().replaceFragment(fragment, tag)
    }
}
