package com.sillylife.knocknock.views.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.sillylife.knocknock.R
import com.sillylife.knocknock.constants.BundleConstants
import com.sillylife.knocknock.constants.Constants
import com.sillylife.knocknock.constants.PackageNameConstants
import com.sillylife.knocknock.utils.CommonUtil
import com.sillylife.knocknock.views.activity.WebViewActivity
import com.sillylife.knocknock.views.adapter.SettingsAdapter
import kotlinx.android.synthetic.main.fragment_home.rcvAll
import kotlinx.android.synthetic.main.fragment_invite.*

class SettingsFragment : BaseFragment() {

    companion object {
        val TAG = SettingsFragment::class.java.simpleName
        fun newInstance() = SettingsFragment()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar?.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        val adapter = SettingsAdapter(context = requireContext(), object : SettingsAdapter.SettingListener {


            override fun onWebUrlClicked(url: String, position: Int, view: View?) {
                startActivity(Intent(requireContext(), WebViewActivity::class.java).putExtra(BundleConstants.WEB_URL, url))
            }

            override fun onLogout(position: Int, view: View?) {

            }

            override fun onLogin(position: Int, view: View?) {

            }

            override fun onSocial(type: Int, position: Int, view: View?) {
                when {
                    type == SettingsAdapter.SOCIAL_ITEM_TWITTER && CommonUtil.isAppInstalled(requireContext(), PackageNameConstants.PACKAGE_TWITTER) -> {
                        try {
                            try {
                                context?.packageManager?.getPackageInfo(PackageNameConstants.PACKAGE_TWITTER, 0)
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?user_id=1139533775522549761")))
                            } catch (e: Exception) {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.SocialLinks.TWITTER)))
                            }
                        } catch (e: Exception) {
                            showToast("Error while opening Twitter.", Toast.LENGTH_SHORT)
                        }
                    }
                    type == SettingsAdapter.SOCIAL_ITEM_FB && CommonUtil.isAppInstalled(requireContext(), PackageNameConstants.PACKAGE_FACEBOOK) -> {
                        try {
                            try {
                                context?.packageManager?.getPackageInfo(PackageNameConstants.PACKAGE_FACEBOOK, 0)
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/2078242259060363")))
                            } catch (e: Exception) {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.SocialLinks.FACEBOOK)))
                            }
                        } catch (e: Exception) {
                            showToast("Error while opening Facebook.", Toast.LENGTH_SHORT)
                        }
                    }
                    type == SettingsAdapter.SOCIAL_ITEM_INSTAGRAM && CommonUtil.isAppInstalled(requireContext(), PackageNameConstants.PACKAGE_INSTAGRAM) -> {
                        val uri = Uri.parse(Constants.SocialLinks.INSTAGRAM)
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.setPackage(PackageNameConstants.PACKAGE_INSTAGRAM)
                        try {
                            startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.SocialLinks.INSTAGRAM)))
                        }
                    }
                    else -> {
                        var uriString = ""
                        when (type) {
                            SettingsAdapter.SOCIAL_ITEM_TWITTER -> {
                                uriString = Constants.SocialLinks.TWITTER
                            }
                            SettingsAdapter.SOCIAL_ITEM_FB -> {
                                uriString = Constants.SocialLinks.FACEBOOK
                            }
                            SettingsAdapter.SOCIAL_ITEM_INSTAGRAM -> {
                                uriString = Constants.SocialLinks.INSTAGRAM
                            }
                            else -> {
                                showToast("Something went wrong")
                            }
                        }
                        if (CommonUtil.textIsNotEmpty(uriString)) {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
                            startActivity(browserIntent)
                        }
                    }
                }
            }

        })
        rcvAll?.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rcvAll?.adapter = adapter
    }
}