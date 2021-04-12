package com.sillylife.knocknock.views.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.karumi.dexter.PermissionToken
import com.sillylife.knocknock.R
import com.sillylife.knocknock.constants.Constants
import com.sillylife.knocknock.helpers.ContactsHelper
import com.sillylife.knocknock.managers.FirebaseAuthUserManager
import com.sillylife.knocknock.models.responses.UserResponse
import com.sillylife.knocknock.services.KnockCallbackReceiver
import com.sillylife.knocknock.services.NewContactAddedService
import com.sillylife.knocknock.services.sharedpreference.SharedPreferenceManager
import com.sillylife.knocknock.utils.CommonUtil
import com.sillylife.knocknock.utils.DexterUtil
import com.sillylife.knocknock.views.fragments.HomeFragment
import com.sillylife.knocknock.views.fragments.LoginFragment
import com.sillylife.knocknock.views.fragments.ProfileFragment
import com.sillylife.knocknock.views.module.MainActivityModule
import com.sillylife.knocknock.views.viewmodal.MainActivityViewModel
import com.sillylife.knocknock.views.viewmodelfactory.ActivityViewModelFactory


class MainActivity : BaseActivity(), MainActivityModule.IModuleListener {
    override fun onGetMeApiSuccess(response: UserResponse) {
        if (!isFinishing && response != null) {
            val profile = response.user
            if (CommonUtil.textIsEmpty(profile?.firstName) || CommonUtil.textIsEmpty(profile?.lastName) || CommonUtil.textIsEmpty(profile?.originalAvatar) || CommonUtil.textIsEmpty(profile?.username)) {
                openProfileFragment()
            } else {
                Toast.makeText(this, "Welcome back ${profile?.getFullName()}", Toast.LENGTH_SHORT).show()
                openHomeFragment()
            }
        }
    }

    override fun onApiFailure(statusCode: Int, message: String) {
        if (statusCode == 404 && message == "User Not Found.") {
            replaceFragment(ProfileFragment.newInstance(), ProfileFragment.TAG)
        }
    }

    override fun onAdvertisingIdSuccess(id: String) {
        SharedPreferenceManager.setAdvertisingId(id)
    }

    override fun onAdvertisingIdFailure(statusCode: Int, message: String) {
        Log.d(TAG, "onAdvertisingIdFailure statusCode:$statusCode message:$message")
    }

    val RC_SIGN_IN = 12132
    private val TAG = MainActivity::class.java.simpleName
    private var viewModel: MainActivityViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this, ActivityViewModelFactory(this)).get(MainActivityViewModel::class.java)
        if (!FirebaseAuthUserManager.isUserLoggedIn()) {
            replaceFragment(LoginFragment.newInstance(), LoginFragment.TAG)
        } else {
            val profile = SharedPreferenceManager.getUser()
            if (profile != null) {
                if (CommonUtil.textIsEmpty(profile?.firstName) || CommonUtil.textIsEmpty(profile?.lastName) || CommonUtil.textIsEmpty(profile?.originalAvatar) || CommonUtil.textIsEmpty(profile?.username)) {
                    openProfileFragment()
                } else {
                    Toast.makeText(this, "Welcome back ${profile?.getFullName()}", Toast.LENGTH_SHORT).show()
                    openHomeFragment()
                    viewModel?.getAdvertisingId(this)
                }
            } else {
                viewModel?.getMe()
            }
        }
    }

    fun openHomeFragment() {
        //Checking permission
        if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            replaceFragment(HomeFragment.newInstance(), HomeFragment.TAG)
            syncContacts()
//            Starting service for registering ContactObserver
            try {
                startService(Intent(this@MainActivity, NewContactAddedService::class.java))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            DexterUtil.with(this, Manifest.permission.READ_CONTACTS).setListener(object :
                    DexterUtil.DexterUtilListener {
                override fun permissionGranted() {
                    replaceFragment(HomeFragment.newInstance(), HomeFragment.TAG)
                    syncContacts()
                }

                override fun permissionDenied(token: PermissionToken?) {

                }
            }).check()
        }
    }

    private fun openProfileFragment() {
        replaceFragment(ProfileFragment.newInstance(), ProfileFragment.TAG)
    }

    fun syncContacts() {
//        sendBroadcast(Intent(this, KnockCallbackReceiver::class.java).putExtra(Constants.ACTION_TYPE, Constants.CallbackActionType.SYNC_CONTACTS))
        ContactsHelper.syncContactsWithNetwork(TAG, this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel?.onDestroy()
    }

    override fun onBackPressed() {
        val profileFragment = supportFragmentManager.findFragmentByTag(ProfileFragment.TAG)
        val loginFragment = supportFragmentManager.findFragmentByTag(LoginFragment.TAG)
        if (profileFragment != null) {
            if ((profileFragment as ProfileFragment).onBackPressed()) {
                super.onBackPressed()
            }
        } else if (loginFragment != null) {
            if ((loginFragment as LoginFragment).onBackPressed()) {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }
}
