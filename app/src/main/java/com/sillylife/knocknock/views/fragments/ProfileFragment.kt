package com.sillylife.knocknock.views.fragments

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.karumi.dexter.PermissionToken
import com.sillylife.knocknock.R
import com.sillylife.knocknock.constants.Constants
import com.sillylife.knocknock.constants.RxEventType
import com.sillylife.knocknock.events.RxBus
import com.sillylife.knocknock.events.RxEvent
import com.sillylife.knocknock.models.responses.UserResponse
import com.sillylife.knocknock.services.AppDisposable
import com.sillylife.knocknock.services.sharedpreference.SharedPreferenceManager
import com.sillylife.knocknock.utils.CommonUtil
import com.sillylife.knocknock.utils.DexterUtil
import com.sillylife.knocknock.utils.FileUtils
import com.sillylife.knocknock.utils.ImageManager
import com.sillylife.knocknock.views.adapter.SettingsAdapter
import com.sillylife.knocknock.views.module.ProfileModule
import com.sillylife.knocknock.views.viewmodal.ProfileViewModel
import com.sillylife.knocknock.views.viewmodelfactory.FragmentViewModelFactory
import kotlinx.android.synthetic.main.fragment_invite.toolbar
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.layout_contact.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.URLEncoder


class ProfileFragment : BaseFragment(), ProfileModule.IModuleListener {

    companion object {
        val TAG = ProfileFragment::class.java.simpleName
        fun newInstance() = ProfileFragment()

        fun newInstance(type: String) =
                ProfileFragment().apply {
                    arguments = Bundle().apply {
                        putString(TYPE, type)
                    }
                }
    }

    private val TYPE = "param1"
    private var mType: String? = null
    private var appDisposable: AppDisposable = AppDisposable()
    private var mAvatarFile: File? = null
    private var viewModel: ProfileViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mType = it.getString(TYPE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, FragmentViewModelFactory(this@ProfileFragment))
                .get(ProfileViewModel::class.java)

        toolbar?.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        setViews()
        addActivityResultListener()
    }

    private fun setViews() {
        username_layout.visibility = View.GONE
        name_layout.visibility = View.GONE
        photo_layout.visibility = View.GONE
        ivContactImage.visibility = View.INVISIBLE
        enableButton(true)
        val profile = SharedPreferenceManager.getUser()
        when {
            mType?.equals(SettingsAdapter.PROFILE_ITEM_AVATAR.toString()) == true -> {
                tvPhotoHeader.text = requireContext().getString(R.string.change_photo)
                photo_layout.visibility = View.VISIBLE
                ImageManager.loadImage(ivContactImage, profile?.originalAvatar)
                ivContactImage.visibility = View.VISIBLE
            }
            mType?.equals(SettingsAdapter.PROFILE_ITEM_NAME.toString()) == true -> {
                tvFullnameHeader.text = requireContext().getString(R.string.correct_full_name)

                etFirstname.setText(profile?.firstName!!)
                etLastname.setText(profile?.lastName!!)
                name_layout.visibility = View.VISIBLE
            }
            mType?.equals(SettingsAdapter.PROFILE_ITEM_USERNAME.toString()) == true -> {
                tvUsernameHeader.text = requireContext().getString(R.string.change_username)
                username_layout.visibility = View.VISIBLE
                if (CommonUtil.textIsNotEmpty(profile?.username)) {
                    etUsername.setText(profile?.getUserName())
                } else {
                    etUsername.setText("@")
                }
            }
            else -> {
                toolbar.navigationIcon = null
                toggleView()
            }
        }

        userImageIv1?.setOnClickListener {
            choosePhotoFromGallery()
        }

        Selection.setSelection(etUsername.text, etUsername.text?.length!!)
        etUsername?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (!s.toString().startsWith("@")) {
                    etUsername?.setText("@")
                    Selection.setSelection(etUsername.text, etUsername.text?.length!!)
                }
            }
        })

        button?.setOnClickListener {
            if (validate()) {
                updateProfile(firstName = etFirstname?.text.toString(),
                        lastName = etLastname?.text.toString(),
                        username = etUsername?.text.toString().replace("@", ""),
                        profileImage = mAvatarFile)
                enableButton(false)
            }
        }
    }

    private fun toggleView() {
        username_layout.visibility = View.GONE
        name_layout.visibility = View.GONE
        photo_layout.visibility = View.GONE
        enableButton(true)
        val profile = SharedPreferenceManager.getUser()
        when {
            CommonUtil.textIsEmpty(profile?.firstName) && CommonUtil.textIsEmpty(profile?.firstName) -> {
                tvFullnameHeader.text = requireContext().getString(R.string.enter_full_name)
                name_layout.visibility = View.VISIBLE
            }
            CommonUtil.textIsEmpty(profile?.originalAvatar) -> {
                tvPhotoHeader.text = requireContext().getString(R.string.add_photo)
                photo_layout.visibility = View.VISIBLE
            }
            CommonUtil.textIsEmpty(profile?.username) -> {
                tvUsernameHeader.text = requireContext().getString(R.string.create_username)
                etUsername.setText("@")
                username_layout.visibility = View.VISIBLE
            }
            else -> {
                replaceFragment(HomeFragment.newInstance(), HomeFragment.TAG)
            }
        }
    }

    private fun validate(): Boolean {
        var isValid = true
        when {
            mType?.equals(SettingsAdapter.PROFILE_ITEM_AVATAR.toString()) == true -> {
                if (mAvatarFile == null) {
                    showToast("Please add profile photo", Toast.LENGTH_SHORT)
                    isValid = false
                }
            }
            mType?.equals(SettingsAdapter.PROFILE_ITEM_NAME.toString()) == true -> {
                if (CommonUtil.textIsEmpty(etFirstname?.text.toString())) {
                    showToast("First name can not be empty", Toast.LENGTH_SHORT)
                    isValid = false
                }
                if (CommonUtil.textIsEmpty(etLastname?.text.toString())) {
                    showToast("Last name can not be empty", Toast.LENGTH_SHORT)
                    isValid = false
                }
            }
            mType?.equals(SettingsAdapter.PROFILE_ITEM_USERNAME.toString()) == true -> {
                if (CommonUtil.textIsEmpty(etUsername?.text.toString())) {
                    showToast("Username can not be empty", Toast.LENGTH_SHORT)
                    isValid = false
                }
            }
            else -> {
                val profile = SharedPreferenceManager.getUser()
                when {
                    CommonUtil.textIsEmpty(profile?.firstName) && CommonUtil.textIsEmpty(profile?.firstName) -> {
                        if (CommonUtil.textIsEmpty(etFirstname?.text.toString())) {
                            showToast("Please add first name", Toast.LENGTH_SHORT)
                            isValid = false
                        }
                        if (CommonUtil.textIsEmpty(etLastname?.text.toString())) {
                            showToast("Please add last name", Toast.LENGTH_SHORT)
                            isValid = false
                        }
                    }
                    CommonUtil.textIsEmpty(profile?.originalAvatar) -> {
                        if (mAvatarFile == null) {
                            showToast("Please add profile photo", Toast.LENGTH_SHORT)
                            isValid = false
                        }
                    }
                    CommonUtil.textIsEmpty(profile?.username) -> {
                        if (CommonUtil.textIsEmpty(etUsername?.text.toString())) {
                            showToast("Please add username", Toast.LENGTH_SHORT)
                            isValid = false
                        }
                    }
                }
            }
        }
        return isValid
    }

    private fun choosePhotoFromGallery() {
        DexterUtil.with(getBaseActivity(), Manifest.permission.READ_EXTERNAL_STORAGE).setListener(object :
                DexterUtil.DexterUtilListener {
            override fun permissionGranted() {
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryIntent.type = "image/*"
                galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);

                try {
                    activity?.startActivityForResult(galleryIntent, Constants.GALLERY)
                } catch (e: ActivityNotFoundException) {
                    val getIntent = Intent(Intent.ACTION_GET_CONTENT)
                    getIntent.type = "image/*"
                    galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);

                    val chooserIntent = Intent.createChooser(getIntent, "Select Image")
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(galleryIntent))

                    activity?.startActivityForResult(chooserIntent, Constants.GALLERY)
                }
                showToast("permissionGranted", Toast.LENGTH_SHORT)
            }

            override fun permissionDenied(token: PermissionToken?) {
                showToast("permissionDenied", Toast.LENGTH_SHORT)
            }
        }).check()
    }

    private fun updateProfile(firstName: String?, lastName: String?, username: String?, profileImage: File?) {
        var imageBody: MultipartBody.Part? = null
        profileImage?.let {
            imageBody = MultipartBody.Part.createFormData("avatar", URLEncoder.encode(profileImage.name, "utf-8"), profileImage.asRequestBody("image/*".toMediaTypeOrNull()))
        }
        var firstNameBody: RequestBody? = null
        if (firstName != null) {
            firstNameBody = firstName.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        var lastNameBody: RequestBody? = null
        if (lastName != null) {
            lastNameBody = lastName.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        var usernameBody: RequestBody? = null
        if (username != null) {
            usernameBody = username.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        viewModel?.updateProfile(firstNameBody, lastNameBody, usernameBody, imageBody)
    }

    private fun addActivityResultListener() {
        appDisposable.add(RxBus.listen(RxEvent.ActivityResult::class.java).subscribe { action ->
            if (isAdded) {
                val requestCode = action.requestCode
                val resultCode = action.resultCode
                val data = action.data
                if (resultCode == Activity.RESULT_OK) {
                    when (requestCode) {
                        Constants.GALLERY ->
                            if (data != null) {
                                val file = FileUtils.getFile(requireContext(), data.data)!!
                                mAvatarFile = file
                                ImageManager.loadImage(ivContactImage, file)
                                ivContactImage.visibility = View.VISIBLE
                            }
                    }
                }
            }
        })
    }

    private fun enableButton(isEnabled: Boolean) {
        proceedProgress.visibility = if (isEnabled) View.GONE else View.VISIBLE
        button.isEnabled = isEnabled
        val profile = SharedPreferenceManager.getUser()
        when {
            mType != null -> {
                button.text = if (isEnabled) requireContext().getString(R.string.update) else ""
            }
            CommonUtil.textIsEmpty(profile?.firstName) && CommonUtil.textIsEmpty(profile?.firstName) -> {
                button.text = if (isEnabled) requireContext().getString(R.string.next) else ""
            }
            CommonUtil.textIsEmpty(profile?.originalAvatar) -> {
                button.text = if (isEnabled) requireContext().getString(R.string.next) else ""
            }
            CommonUtil.textIsEmpty(profile?.username) -> {
                button.text = if (isEnabled) requireContext().getString(R.string.done) else ""
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appDisposable.dispose()
        viewModel?.onDestroy()
    }

    override fun onGetMeApiSuccess(response: UserResponse?) {

    }

    override fun onUpdateProfileApiSuccess(response: UserResponse) {
        if (isAdded && response != null) {
            if (mType != null) {
                showToast("Updated", Toast.LENGTH_SHORT)
                RxBus.publish(RxEvent.Action(RxEventType.PROFILE_UPDATED))
                requireActivity().supportFragmentManager.popBackStack()
                enableButton(true)
            } else {
                toggleView()
            }
        }
    }

    fun onBackPressed(): Boolean {
        val profile = SharedPreferenceManager.getUser()
        return when {
            mType != null -> {
                true
            }
            CommonUtil.textIsEmpty(profile?.firstName) && CommonUtil.textIsEmpty(profile?.firstName) && CommonUtil.textIsEmpty(profile?.originalAvatar) && CommonUtil.textIsEmpty(profile?.username) -> {
                false
            }
            else -> {
                true
            }
        }
    }

    override fun onApiFailure(statusCode: Int, message: String) {
        if (isAdded) {
            enableButton(true)
            showToast(message, Toast.LENGTH_SHORT)
        }
    }

}