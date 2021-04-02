package com.sillylife.knocknock.managers

import android.R.attr.phoneNumber
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.sillylife.knocknock.MainApplication
import com.sillylife.knocknock.services.sharedpreference.SharedPreferenceManager
import java.util.concurrent.TimeUnit


object AuthManager {

    val activity = MainApplication.getInstance()
    var anonymousUserId = FirebaseAuthUserManager.getFirebaseUserId()

    interface IAuthCredentialCallback {
        fun onAuthCompleted(isNewUser: Boolean)
        fun onAuthError(error: String, smsAutoDetect: Boolean)
        fun onCodeSent(verificationId: String)
        fun onAccountExists()
    }

    interface IAuthCredentialLogoutCallback {
        fun onUserSignedOutSuccessfully()
    }

    interface IAuthCredentialAnonymouslyLoginCallback {
        fun onSignInAnonymously()
        fun onSignInAnonymouslyFailed()
    }

    private val TAG = AuthManager::class.java.simpleName
    private var mResendToken: PhoneAuthProvider.ForceResendingToken? = null
    private const val AUTO_RETRIEVAL_TIMEOUT_SECONDS: Long = 60
    private var mVerificationId: String? = null

    fun signInWithPhone(mobile: String, mListener: IAuthCredentialCallback, activity: Activity) {
        anonymousUserId = FirebaseAuthUserManager.getFirebaseUserId()


        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber(mobile) // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(activity) // Activity (for callback binding)
                .setCallbacks(phoneCallbacks(mListener, mobile)) // OnVerificationStateChangedCallbacks
                .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun signInAnonymously(mListener: IAuthCredentialAnonymouslyLoginCallback) {
        FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener(OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful) {
                        mListener.onSignInAnonymously()
                    } else {
                        mListener.onSignInAnonymouslyFailed()
                    }
                })
    }

    fun logoutUser(activity: Activity, mListener: IAuthCredentialLogoutCallback) {
        FirebaseAuth.getInstance().signOut()
        val mGoogleSignInClient = GoogleSignIn.getClient(
                activity, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        )
        mGoogleSignInClient.signOut()?.addOnCompleteListener(activity) {
            if (it.isSuccessful) {
                mListener.onUserSignedOutSuccessfully()
            }
        }
    }

    private fun phoneCallbacks(mListener: IAuthCredentialCallback, mobile: String): PhoneAuthProvider.OnVerificationStateChangedCallbacks {
        return object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneCredential(credential, mListener, mobile, true)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                if (e is FirebaseAuthInvalidCredentialsException) {
                    mListener.onAuthError("Invalid credentials", false)

                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    mListener.onAuthError((e.message ?: ""), false)
                }
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                mVerificationId = verificationId
                mResendToken = token
                Log.d(TAG, "onCodeSent: $verificationId\n$token")
                mListener.onCodeSent(verificationId!!)

            }
        }
    }

    fun signInWithPhoneCredential(credential: PhoneAuthCredential, mListener: IAuthCredentialCallback, mobile: String, isAutoDetect: Boolean) {
        val isAnonymous = FirebaseAuthUserManager.isAnonymousLoggedIn()
        val bearerToken = SharedPreferenceManager.getFirebaseAuthToken()
        anonymousUserId = if(isAnonymous) {
            FirebaseAuthUserManager.getFirebaseUserId()
        } else {
            null
        }
        signInWithCredentialTask(credential, mListener, mobile, isAnonymous, bearerToken ?: "", isAutoDetect)
    }

    fun signInWithCredentialTask(credential: PhoneAuthCredential, mListener: IAuthCredentialCallback, mobile: String, isAnonymous: Boolean, bearerToken: String?, smsAutoDetect: Boolean) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { _task ->
                    if (_task.isSuccessful) {
                        mListener.onAuthCompleted(_task?.result?.additionalUserInfo?.isNewUser
                                ?: false)
                    } else {
//                        CommonUtil.callRegisterFCMAPI = true
                        Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                        mListener.onAuthError(_task.exception?.message
                                ?: "Something went wrong", smsAutoDetect)
                    }
                }
    }
}