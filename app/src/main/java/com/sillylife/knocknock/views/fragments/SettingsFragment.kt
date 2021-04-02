package com.sillylife.knocknock.views.fragments

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.karumi.dexter.PermissionToken
import com.sillylife.knocknock.R
import com.sillylife.knocknock.constants.BundleConstants
import com.sillylife.knocknock.constants.Constants
import com.sillylife.knocknock.constants.PackageNameConstants
import com.sillylife.knocknock.constants.RxEventType
import com.sillylife.knocknock.database.KnockNockDatabase
import com.sillylife.knocknock.events.RxBus
import com.sillylife.knocknock.events.RxEvent
import com.sillylife.knocknock.managers.AuthManager
import com.sillylife.knocknock.services.AppDisposable
import com.sillylife.knocknock.services.sharedpreference.SharedPreferenceManager
import com.sillylife.knocknock.services.sharedpreference.SharedPreferences
import com.sillylife.knocknock.utils.CommonUtil
import com.sillylife.knocknock.utils.DexterUtil
import com.sillylife.knocknock.utils.FileUtils
import com.sillylife.knocknock.views.activity.WebViewActivity
import com.sillylife.knocknock.views.adapter.SettingsAdapter
import kotlinx.android.synthetic.main.fragment_home.rcvAll
import kotlinx.android.synthetic.main.fragment_invite.*

class SettingsFragment : BaseFragment() {

    companion object {
        val TAG = SettingsFragment::class.java.simpleName
        fun newInstance() = SettingsFragment()
    }

    private var appDisposable: AppDisposable = AppDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar?.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        appDisposable?.add(RxBus.listen(RxEvent.Action::class.java).subscribe { action ->
            when (action.eventType) {
                RxEventType.PROFILE_UPDATED -> {
                    val adapter = rcvAll?.adapter as SettingsAdapter
                    if (isAdded && adapter != null) {
                        adapter.notifyItemChange(SettingsAdapter.PROFILE_ITEM)
                    }
                }
            }
        })
        setAdapter()
        addActivityResultListener()
    }

    private fun addActivityResultListener() {
        appDisposable.add(RxBus.listen(RxEvent.ActivityResult::class.java).subscribe { action ->
            if (isAdded) {
                val requestCode = action.requestCode
                val resultCode = action.resultCode
                val data = action.data
                if (resultCode == Activity.RESULT_OK) {
                    when (requestCode) {
                        Constants.AUDIO_LIBRARY ->
                            if (data != null) {
                                SharedPreferenceManager.setKnockTone(data.dataString!!)
                                val adapter = rcvAll?.adapter as SettingsAdapter
                                if (isAdded && adapter != null) {
                                    adapter.notifyItemChange(SettingsAdapter.NOTIFICATION_ITEM)
                                }
                            }
                    }
                }
            }
        })
    }

    private fun setAdapter() {
        val adapter = SettingsAdapter(context = requireContext(), object : SettingsAdapter.SettingListener {
            override fun onWebUrlClicked(url: String, position: Int, view: View?) {
                startActivity(Intent(requireContext(), WebViewActivity::class.java).putExtra(BundleConstants.WEB_URL, url))
            }

            override fun onLogout(position: Int, view: View?) {
                if (isVisible && isAdded) {
                    AuthManager.logoutUser(requireActivity(), object : AuthManager.IAuthCredentialLogoutCallback {
                        override fun onUserSignedOutSuccessfully() {
                            val db: KnockNockDatabase? = KnockNockDatabase.getInstance(requireActivity())
                            db?.clearAllTables()
                            SharedPreferences.clearPrefs()
                            (requireActivity() as Activity).recreate()
                        }
                    })
                }
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

            override fun onProfileItem(type: Int, position: Int, view: View?) {
                if (isAdded && isVisible) {
                    addFragment(ProfileFragment.newInstance(type.toString()), ProfileFragment.TAG)
                }
            }

            override fun onKnockToneClicked(position: Int, view: View?) {
                if (isAdded && isVisible) {
                    chooseAudioFromFiles()
                }
            }

        })
        rcvAll?.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rcvAll?.adapter = adapter
    }


    private fun chooseAudioFromFiles() {
        DexterUtil.with(getBaseActivity(), Manifest.permission.READ_EXTERNAL_STORAGE).setListener(object :
                DexterUtil.DexterUtilListener {
            override fun permissionGranted() {
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryIntent.type = "audio/*"
                galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);

                try {
                    activity?.startActivityForResult(galleryIntent, Constants.AUDIO_LIBRARY)
                } catch (e: ActivityNotFoundException) {
                    val getIntent = Intent(Intent.ACTION_GET_CONTENT)
                    getIntent.type = "audio/*"
                    galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);

                    val chooserIntent = Intent.createChooser(getIntent, "Select Image")
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(galleryIntent))

                    activity?.startActivityForResult(chooserIntent, Constants.AUDIO_LIBRARY)
                }
                showToast("permissionGranted", Toast.LENGTH_SHORT)
            }

            override fun permissionDenied(token: PermissionToken?) {
                showToast("permissionDenied", Toast.LENGTH_SHORT)
            }
        }).check()
    }

    override fun onDestroy() {
        super.onDestroy()
        appDisposable.dispose()
    }
}