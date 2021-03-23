package com.sillylife.knocknock.views.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.karumi.dexter.PermissionToken
import com.sillylife.knocknock.R
import com.sillylife.knocknock.constants.Constants
import com.sillylife.knocknock.constants.RxEventType
import com.sillylife.knocknock.database.DBHelper
import com.sillylife.knocknock.events.RxBus
import com.sillylife.knocknock.events.RxEvent
import com.sillylife.knocknock.helpers.ContactsHelper
import com.sillylife.knocknock.models.Contact
import com.sillylife.knocknock.models.HomeDataItem
import com.sillylife.knocknock.models.responses.HomeDataResponse
import com.sillylife.knocknock.models.responses.SyncedContactsResponse
import com.sillylife.knocknock.services.AppDisposable
import com.sillylife.knocknock.services.sharedpreference.SharedPreferenceManager
import com.sillylife.knocknock.utils.CommonUtil
import com.sillylife.knocknock.utils.DexterUtil
import com.sillylife.knocknock.utils.ImageManager
import com.sillylife.knocknock.views.adapter.HomeAdapter
import com.sillylife.knocknock.views.adapter.HomeAdapter.Companion.HomeType.Companion.AVAILABLE_CONTACTS
import com.sillylife.knocknock.views.adapter.HomeAdapter.Companion.HomeType.Companion.RECENTLY_CONNECTED_CONTACTS
import com.sillylife.knocknock.views.module.HomeFragmentModule
import com.sillylife.knocknock.views.viewmodal.HomeFragmentViewModel
import com.sillylife.knocknock.views.viewmodelfactory.FragmentViewModelFactory
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.cvContactImage
import kotlinx.android.synthetic.main.layout_contact.*


class HomeFragment : BaseFragment(), HomeFragmentModule.APIModuleListener {

    companion object {
        val TAG = HomeFragment::class.java.simpleName
        fun newInstance() = HomeFragment()
    }

    private var appDisposable: AppDisposable = AppDisposable()
    private var viewModel: HomeFragmentViewModel? = null
    private var dbHelper: DBHelper? = null
    private var adapter: HomeAdapter? = null
    var recentlyListenedRowExists: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, FragmentViewModelFactory(this@HomeFragment))
                .get(HomeFragmentViewModel::class.java)
        DexterUtil.with(requireActivity(), Manifest.permission.READ_CONTACTS).setListener(object :
                DexterUtil.DexterUtilListener {
            override fun permissionGranted() {

            }

            override fun permissionDenied(token: PermissionToken?) {

            }
        }).check()
        dbHelper = ViewModelProvider(activity!!).get(DBHelper::class.java)
        ContactsHelper.updatePhoneContactsToDB()

        val items: ArrayList<HomeDataItem> = arrayListOf()
//
//        dbHelper?.getLiveDBRecentlyConnectedContactList()?.observe(viewLifecycleOwner, {
//            val contactList: ArrayList<Contact> = ArrayList()
//            for (i in it) {
//                contactList.add(MapDbEntities.contactToEntity(i))
//            }
//            if (contactList.isNotEmpty() && contactList.size >= 1) {
//                recentlyListenedRowExists = true
//                items.add(HomeDataItem(type = Constants.HomeType.RECENTLY_CONNECTED, title = "Recently Connected", contacts = contactList, false))
//            }
//        })

        val recentlyConnectedContactList = ContactsHelper.getDBRecentlyConnectedContactList()
        if (recentlyConnectedContactList.isNotEmpty() && recentlyConnectedContactList.size >= 1) {
            recentlyListenedRowExists = true
            items.add(HomeDataItem(type = RECENTLY_CONNECTED_CONTACTS, title = "Recently Connected", contacts = recentlyConnectedContactList, false))
        }
        val contact = ContactsHelper.getDBPhoneContactList()

        val temp: ArrayList<String> = arrayListOf()
        contact.forEach {
            temp.add(it.phone!!)
        }

        viewModel?.getPhoneContacts(temp)


//        val contact = ContactsHelper.getAvailableContactList()
        items.add(HomeDataItem(type = AVAILABLE_CONTACTS, title = "Phone Contacts", contacts = contact, false))
        setHomeAdapter(HomeDataResponse(items = items, hasMore = false))

        cvContactImage?.setOnClickListener {
            addFragment(SettingsFragment.newInstance(), SettingsFragment.TAG)
        }
        fabButton?.setOnClickListener {
            addFragment(InviteFragment.newInstance(), InviteFragment.TAG)
        }
        setPhoto()
        appDisposable?.add(RxBus.listen(RxEvent.Action::class.java).subscribe { action ->
            when (action.eventType) {
                RxEventType.PROFILE_UPDATED -> {
                    setPhoto()
                }
            }
        })
    }

    private fun setPhoto() {
        val profile = SharedPreferenceManager.getUser()
        if (CommonUtil.textIsNotEmpty(profile?.originalAvatar)) {
            ImageManager.loadImage(ivContactImage, profile?.originalAvatar)
            ivContactImage.visibility = View.VISIBLE
        } else {
//            tvContactPlaceholder.setTextSize(TypedValue.COMPLEX_UNIT_PX, requireContext().resources.getDimensionPixelSize(R.dimen._47ssp).toFloat())
            tvContactPlaceholder.text = profile?.getInitialsName()
        }
    }

    private fun setHomeAdapter(homeDataResponse: HomeDataResponse) {
        adapter = HomeAdapter(requireContext(), homeDataResponse) { it: Any, pos: Int, type: String, it2: Any? ->
            if (it is Contact) {
                if (type == AVAILABLE_CONTACTS || type == RECENTLY_CONNECTED_CONTACTS) {
                    ContactsHelper.updateLastConnected(it.phone!!)
                    if (!recentlyListenedRowExists) {
                        val contactList: ArrayList<Contact> = ArrayList()
                        contactList.add(it)
                        recentlyListenedRowExists = true
                        adapter?.addRecentlyListenedRow(HomeDataItem(type = RECENTLY_CONNECTED_CONTACTS, title = "Recently Connected", contacts = contactList, false))
                    } else {
                        adapter?.updateRecentlyConnected(it)
                    }
                }
            }
            if (type == Constants.IMPRESSION) {
//                Log.d("IMPRESSION", "Contact - $it | Source - $it2")
            }
        }
        rcvAll?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        ViewCompat.setNestedScrollingEnabled(rcvAll, false)
        rcvAll?.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        appDisposable.dispose()
        viewModel?.onDestroy()
    }

    override fun onContactPhoneSyncSuccess(response: SyncedContactsResponse) {
        if (isAdded && response != null) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                return
            }

            val dbContacts = ContactsHelper.getDBPhoneContactList()
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

    override fun onApiFailure(statusCode: Int, message: String) {

    }
}