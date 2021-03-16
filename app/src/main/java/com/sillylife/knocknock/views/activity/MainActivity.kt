package com.sillylife.knocknock.views.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.sillylife.knocknock.R
import com.sillylife.knocknock.models.responses.GenericResponse
import com.sillylife.knocknock.models.responses.UserResponse
import com.sillylife.knocknock.services.FirebaseAuthUserManager
import com.sillylife.knocknock.services.sharedpreference.SharedPreferenceManager
import com.sillylife.knocknock.views.fragments.HomeFragment
import com.sillylife.knocknock.views.module.MainActivityModule
import com.sillylife.knocknock.views.viewmodal.MainActivityViewModel
import com.sillylife.knocknock.views.viewmodelfactory.ActivityViewModelFactory


class MainActivity : BaseActivity(), MainActivityModule.IModuleListener {
    override fun onGetMeApiSuccess(response: UserResponse) {
        if (!isFinishing && response != null) {
            Toast.makeText(this, "Logged In Successfully", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRingBellApiSuccess(response: GenericResponse) {
        if (!isFinishing && response != null) {
            Toast.makeText(this, "Bell Ringed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onApiFailure(statusCode: Int, message: String) {

    }

    val RC_SIGN_IN = 12132
    private val TAG = MainActivity::class.java.simpleName
    private var viewModel: MainActivityViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this, ActivityViewModelFactory(this)).get(MainActivityViewModel::class.java)
        if (!FirebaseAuthUserManager.isUserLoggedIn()) {
            val providers = arrayListOf(
                    AuthUI.IdpConfig.PhoneBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build()
            )
            startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                            providers
                    ).setIsSmartLockEnabled(false).build(), RC_SIGN_IN
            )
        } else {
            if (SharedPreferenceManager.getUser() != null) {
                replaceFragment(HomeFragment.newInstance(), HomeFragment.TAG)
                Toast.makeText(this, "Already Logged In", Toast.LENGTH_SHORT).show()
            } else {
                viewModel?.getMe()
            }
        }
//        ringBell?.setOnClickListener {
//            viewModel?.ringBell(1)
//        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                Log.d("onActivityResult", "using FirebaseAuthUserManager ${FirebaseAuthUserManager.getFirebaseAuthToken()}")
                FirebaseAuth.getInstance().currentUser?.getIdToken(true)
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("onActivityResult", task.result!!.token!!)
                                FirebaseAuthUserManager.registerFCMToken()
                                viewModel?.getMe()
                            }
                        }
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel?.onDestroy()
    }
}
