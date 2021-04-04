package com.sillylife.knocknock.views.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.karumi.dexter.PermissionToken
import com.sillylife.knocknock.MainApplication
import com.sillylife.knocknock.R
import com.sillylife.knocknock.constants.Constants
import com.sillylife.knocknock.constants.RxEventType
import com.sillylife.knocknock.events.RxBus
import com.sillylife.knocknock.events.RxEvent
import com.sillylife.knocknock.helpers.ContactsHelper
import com.sillylife.knocknock.managers.FirebaseAuthUserManager
import com.sillylife.knocknock.models.Contact
import com.sillylife.knocknock.models.responses.SyncedContactsResponse
import com.sillylife.knocknock.models.responses.UserResponse
import com.sillylife.knocknock.services.CallbackWrapper
import com.sillylife.knocknock.services.ContactWatchService
import com.sillylife.knocknock.services.sharedpreference.SharedPreferenceManager
import com.sillylife.knocknock.utils.AsyncTaskAlternative.executeAsyncTask
import com.sillylife.knocknock.utils.CommonUtil
import com.sillylife.knocknock.utils.DexterUtil
import com.sillylife.knocknock.views.fragments.HomeFragment
import com.sillylife.knocknock.views.fragments.LoginFragment
import com.sillylife.knocknock.views.fragments.ProfileFragment
import com.sillylife.knocknock.views.module.MainActivityModule
import com.sillylife.knocknock.views.viewmodal.MainActivityViewModel
import com.sillylife.knocknock.views.viewmodelfactory.ActivityViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import retrofit2.Response


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

    private fun openHomeFragment() {
        //Checking permission
        if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            replaceFragment(HomeFragment.newInstance(), HomeFragment.TAG)
            syncContacts()
//            Starting service for registering ContactObserver
            try {
                val intent = Intent(this@MainActivity, ContactWatchService::class.java)
                startService(intent)
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
        Log.d(TAG, "SyncContacts - Started")
        CoroutineScope(Dispatchers.IO).executeAsyncTask(onPreExecute = {
            Log.d(TAG, "SyncContacts - onPreExecute")
        }, doInBackground = { publishProgress: suspend (progress: Int) -> Unit ->
            Log.d(TAG, "SyncContacts - doInBackground - $publishProgress")
            publishProgress(10) // call `publishProgress` to update progress, `onProgressUpdate` will be called

            ContactsHelper.updatePhoneContactsToDB()
            val dbContacts = ContactsHelper.getDBPhoneContactList()

            val phoneNumberList: ArrayList<String> = arrayListOf()
            dbContacts.forEach {
                phoneNumberList.add(it.phone!!)
            }
            publishProgress(50)
            viewModel?.getAppDisposable()?.add(
                    MainApplication.getInstance().getAPIService().syncContacts(phoneNumberList)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(object : CallbackWrapper<Response<SyncedContactsResponse>>() {
                                override fun onSuccess(t: Response<SyncedContactsResponse>) {
                                    if (t.isSuccessful && t.body() != null) {
                                        val response = t.body()!!
                                        if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                                            return
                                        }
                                        val availableContacts = response.contacts!!
                                        for (availableContact in availableContacts) {
                                            var toUpdate = false
                                            var tempContact = Contact()
                                            for (dbContact in dbContacts) {
                                                if (availableContact.phone == dbContact.phone) {
                                                    toUpdate = true
                                                    tempContact = dbContact.copy()
                                                    break
                                                }
                                            }
                                            if (toUpdate) {
                                                if (CommonUtil.textIsNotEmpty(availableContact.image)) {
                                                    tempContact.image = availableContact.image
                                                }
                                                if (CommonUtil.textIsNotEmpty(availableContact.username)) {
                                                    tempContact.username = availableContact.username
                                                }
                                                if (availableContact.userPtrId != null) {
                                                    tempContact.userPtrId = availableContact.userPtrId
                                                }
                                                if (availableContact.availableOnPlatform != null) {
                                                    tempContact.availableOnPlatform = availableContact.availableOnPlatform
                                                }
                                                ContactsHelper.updateSomeData(availableOnPlatform = tempContact.availableOnPlatform!!, image = tempContact.image!!, userPtrId = tempContact.userPtrId!!, phone = tempContact.phone!!, username = tempContact.username!!)
                                            }
                                        }
                                    }
                                }

                                override fun onFailure(code: Int, message: String) {
                                    SharedPreferenceManager.disableContactSyncWithNetwork()
                                }
                            }))
            publishProgress(100)
            "Result" // send data to "onPostExecute"
        }, onPostExecute = { it ->
            // runs in Main Thread
            // ... here "it" is a data returned from "doInBackground"
            SharedPreferenceManager.disableContactSyncWithNetwork()
            Handler(Looper.getMainLooper()).postDelayed({
                RxBus.publish(RxEvent.Action(RxEventType.CONTACT_SYNCED_WITH_NETWORK))
            }, 5000)
            Log.d(TAG, "SyncContacts - onPostExecute - $it")
        }, onProgressUpdate = {
            // runs in Main Thread
            // ... here "it" contains progress
            Log.d(TAG, "SyncContacts - onProgressUpdate - $it")
        })
        Log.d(TAG, "SyncContacts - Ended")
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
