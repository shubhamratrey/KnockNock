package com.sillylife.knocknock.views.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Selection
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.core.text.bold
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.sillylife.knocknock.R
import com.sillylife.knocknock.constants.BundleConstants
import com.sillylife.knocknock.models.responses.UserResponse
import com.sillylife.knocknock.services.AppDisposable
import com.sillylife.knocknock.utils.CommonUtil
import com.sillylife.knocknock.utils.PhoneNumberUtils
import com.sillylife.knocknock.utils.TextViewLinkHandler
import com.sillylife.knocknock.views.activity.MainActivity
import com.sillylife.knocknock.views.activity.WebViewActivity
import com.sillylife.knocknock.views.module.LoginFragmentModule
import com.sillylife.knocknock.views.viewmodal.LoginFragmentViewModel
import com.sillylife.knocknock.views.viewmodelfactory.FragmentViewModelFactory
import kotlinx.android.synthetic.main.fragment_invite.toolbar
import kotlinx.android.synthetic.main.fragment_login.*


class LoginFragment : BaseFragment(), LoginFragmentModule.IModuleListener {

    companion object {
        val TAG = LoginFragment::class.java.simpleName
        fun newInstance() = LoginFragment()
    }

    private val mTeamMessageLayout: String = "team_message_layout"
    private val mPhoneLayout: String = "phone_layout"
    private val mOtpLayout: String = "otp_layout"
    private var mType: String = mTeamMessageLayout
    private var appDisposable: AppDisposable = AppDisposable()
    private val mCountryCode = "+91-"
    private var mPhoneNumber: String? = null
    private var viewModel: LoginFragmentViewModel? = null
    private var mVerificationId: String? = null


    private var mGoogleSignInClient: GoogleSignInClient? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, FragmentViewModelFactory(this@LoginFragment)).get(LoginFragmentViewModel::class.java)
        //        val str = SpannableStringBuilder().bold { color(ContextCompat.getColor(context, R.color.light_orange_primary)) { append("$count  ") } }.append(context.getString(R.string.listens_count_without_value))

        toolbar?.setNavigationOnClickListener {
            (requireActivity() as MainActivity).onBackPressed()
        }
        setViews()
    }

    private fun setViews() {
        teamMessage_layout.visibility = View.GONE
        phonenumber_layout.visibility = View.GONE
        otp_layout.visibility = View.GONE
        button_layout.visibility = View.GONE
        when (mType) {
            mTeamMessageLayout -> {
                toolbar?.navigationIcon = null
                teamMessage_layout?.visibility = View.VISIBLE
                alreadySignInTv?.text = SpannableStringBuilder().append("Already have account? ").bold { append("Sign in") }
                teamMessageTv?.movementMethod = ScrollingMovementMethod()

                alreadySignInTv?.setOnClickListener {
                    mType = mPhoneLayout
                    setViews()
                }
                getUsernameBtn?.setOnClickListener {
                    mType = mPhoneLayout
                    setViews()
                }

            }
            mPhoneLayout -> {
                toolbar?.navigationIcon = null
                phonenumber_layout?.visibility = View.VISIBLE
                button_layout?.visibility = View.VISIBLE
                button?.text = getString(R.string.get_otp)
                enableButton(true)

                if (CommonUtil.textIsNotEmpty(mPhoneNumber)) {
                    val tempPhone = mCountryCode + mPhoneNumber
                    etPhoneNumber?.setText(tempPhone)
                } else {
                    etPhoneNumber?.setText(mCountryCode)
                }
                Selection.setSelection(etPhoneNumber.text, etPhoneNumber.text?.length!!)
                etPhoneNumber?.addTextChangedListener(object : TextWatcher {
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    }

                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                    }

                    override fun afterTextChanged(s: Editable) {
                        if (!s.toString().startsWith(mCountryCode)) {
                            etPhoneNumber?.setText(mCountryCode)
                            Selection.setSelection(etPhoneNumber.text, etPhoneNumber.text?.length!!)
                        }
                        mPhoneNumber = s.toString().replace(mCountryCode, "")
                    }
                })
                tcPrivacyTv?.movementMethod = LinkMovementMethod.getInstance()
                tcPrivacyTv?.text = HtmlCompat.fromHtml(resources.getString(R.string.t_c_and_privacy_policy), HtmlCompat.FROM_HTML_MODE_LEGACY)
                try {
                    tcPrivacyTv?.movementMethod = object : TextViewLinkHandler() {
                        override fun onLinkClick(url: String?) {
                            if (url != null) {
                                if (url.contains("privacy-policy")) {
                                    startActivity(Intent(requireContext(), WebViewActivity::class.java).putExtra(BundleConstants.WEB_URL, url))
                                } else if (url.contains("terms-condition")) {
                                    startActivity(Intent(requireContext(), WebViewActivity::class.java).putExtra(BundleConstants.WEB_URL, url))
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            mOtpLayout -> {
                toolbar?.setNavigationIcon(R.drawable.ic_left_arrow)
                otp_layout?.visibility = View.VISIBLE
                button_layout?.visibility = View.VISIBLE
                button?.text = getString(R.string.submit_otp)
                enableButton(true)

                otpView?.setText("")
                otpView?.isFocusableInTouchMode = true
                otpView?.setAnimationEnable(true)
                otpView?.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {}

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                        otpView?.setLineColor(ContextCompat.getColor(requireContext(), R.color.black))
                        val len = s?.length ?: 0
                        Log.d("onTextChanged", "---")
                        if (len == 6) {
                            Log.d("onTextChanged", "++++")
                            otpView?.setTextColor(Color.parseColor("#DD0D4B"))
                        } else {
                            otpView?.setTextColor(Color.parseColor("#001A28"))
                        }
                    }
                })


                resendTv?.setOnClickListener {
                    val phoneNumber = "+$mCountryCode$mPhoneNumber"
                    if (activity != null) {
                        viewModel?.resendCode(phoneNumber, requireActivity())
                    }
                }
            }
        }

//        alreadySignInTv.text = SpannableStringBuilder().append("Already have account? ").bold { color(ContextCompat.getColor(requireContext(), R.color.black)){ append("Sign in") } }


//
        button?.setOnClickListener {
            if (validate()) {
                enableButton(false)
                when (mType) {
                    mPhoneLayout -> {
                        mType = mOtpLayout
                        if (activity != null && CommonUtil.textIsNotEmpty(mPhoneNumber)) {
                            viewModel?.signInWithPhone(phoneNumber = mPhoneNumber!!, "91", requireActivity())
                        }
                    }
                    mOtpLayout -> {
                        val code = otpView.text.toString()
                        val credential = PhoneAuthProvider.getCredential(if (mVerificationId != null) mVerificationId!! else "", code)
                        CommonUtil.hideKeyboard(context!!)
                        viewModel?.submitCode(credential, PhoneNumberUtils.getPseudoValidPhoneNumber(mPhoneNumber!!, "91")!!)
                    }
                }
            }
        }
    }


    private fun validate(): Boolean {
        var isValid = true
        when (mType) {
            mPhoneLayout -> {
                if (CommonUtil.textIsEmpty(etPhoneNumber?.text.toString()) || etPhoneNumber.text.toString().length < 12) {
                    showToast("Phone number invalid", Toast.LENGTH_SHORT)
                    isValid = false
                }
            }
            mOtpLayout -> {
                if (mVerificationId != null && CommonUtil.textIsEmpty(otpView?.text.toString()) || otpView.text.toString().length < 6) {
                    showToast("Invalid OTP", Toast.LENGTH_SHORT)
                    isValid = false
                }
            }
        }
        return isValid
    }

    private fun enableButton(isEnabled: Boolean) {
        proceedProgress.visibility = if (isEnabled) View.GONE else View.VISIBLE
        button.isEnabled = isEnabled
        when (mType) {
            mPhoneLayout -> {
                button.text = if (isEnabled) requireContext().getString(R.string.get_otp) else ""
            }
            mOtpLayout -> {
                button.text = if (isEnabled) requireContext().getString(R.string.submit_otp) else ""
            }
        }
    }

    private fun requestHint() {
        val options: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        if (activity != null) {
            mGoogleSignInClient = GoogleSignIn.getClient(activity!!, options)
        }
        if (mGoogleSignInClient != null) {
            try {
                val hintRequest = HintRequest.Builder().setPhoneNumberIdentifierSupported(true).build()
                val intent = if (mGoogleSignInClient?.asGoogleApiClient() != null) {
                    Auth.CredentialsApi.getHintPickerIntent(mGoogleSignInClient?.asGoogleApiClient(), hintRequest)
                } else {
                    null
                }
                if (intent != null) {
//                    startIntentSenderForResult(intent.intentSender, LoginTransition.PHONE_NUMBER_REQUESTCODE, null, 0, 0, 0, null)
                }
            } catch (e: Exception) {

            }
        }
    }

    fun onBackPressed(): Boolean {
        return when (mType) {
            mPhoneLayout -> {
                mType = mTeamMessageLayout
                setViews()
                false
            }
            mOtpLayout -> {
                mType = mPhoneLayout
                setViews()
                false
            }
            else -> {
                true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appDisposable.dispose()
        viewModel?.onDestroy()
    }

    override fun onGetMeApiSuccess(response: UserResponse) {
        if (isAdded) {
            replaceFragment(HomeFragment.newInstance(), HomeFragment.TAG)
        }
    }

    override fun onGetMeApiFailure(statusCode: Int, message: String) {
        if (statusCode == 404 && message == "User Not Found.") {
            replaceFragment(ProfileFragment.newInstance(), ProfileFragment.TAG)
        }
        Log.d(TAG, "statusCode = $statusCode, message - $message")
    }

    override fun onPhoneAuthCompleted() {
        if (activity != null && isAdded) {
            viewModel?.getMe()
        }
    }

    override fun onAuthError(error: String) {
        if (isAdded) {
            Log.d(TAG, error)
            enableButton(true)
        }
    }

    override fun onCodeSent(verificationId: String) {
        if (activity != null && isAdded) {
            this.mVerificationId = verificationId
            setViews()
        }
    }

    override fun onAccountExists() {

    }
}
