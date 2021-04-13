package com.sillylife.knocknock.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sillylife.knocknock.R
import com.sillylife.knocknock.constants.Constants
import com.sillylife.knocknock.constants.RxEventType
import com.sillylife.knocknock.events.RxBus
import com.sillylife.knocknock.events.RxEvent
import com.sillylife.knocknock.helpers.ContactsHelper
import com.sillylife.knocknock.managers.FirebaseAuthUserManager
import com.sillylife.knocknock.models.Contact
import com.sillylife.knocknock.models.HomeDataItem
import com.sillylife.knocknock.models.responses.GenericResponse
import com.sillylife.knocknock.models.responses.HomeDataResponse
import com.sillylife.knocknock.services.AppDisposable
import com.sillylife.knocknock.services.sharedpreference.SharedPreferenceManager
import com.sillylife.knocknock.utils.CommonUtil
import com.sillylife.knocknock.utils.ImageManager
import com.sillylife.knocknock.utils.TimeUtils
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
    private var adapter: HomeAdapter? = null
    var recentlyListenedRowExists: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, FragmentViewModelFactory(this@HomeFragment))
                .get(HomeFragmentViewModel::class.java)

        if (SharedPreferenceManager.isContactSyncingWithNetwork()) {
            toggleContactSyncLayout(true)
        } else {
            setHomeAdapter()
            toggleContactSyncLayout(false)
        }


//        activity?.startService(Intent(requireContext(), WhereAbouts::class.java))

        cvContactImage?.setOnClickListener {
            addFragment(SettingsFragment.newInstance(), SettingsFragment.TAG)
        }
        fabButton?.setOnClickListener {
            addFragment(InviteFragment.newInstance(), InviteFragment.TAG)
        }
        setPhoto()

        appDisposable?.add(RxBus.listen(RxEvent.Action::class.java).subscribe({ action ->
            if (isAdded && activity != null) {
                requireActivity().runOnUiThread {
                    when (action.eventType) {
                        RxEventType.PROFILE_UPDATED -> {
                            setPhoto()
                        }
                        RxEventType.CONTACT_SYNCED_WITH_NETWORK -> {
                            if (isAdded && isVisible && rcvAll.adapter == null && adapter == null) {
                                showToast("Hogya setup !!", Toast.LENGTH_SHORT)
                                setHomeAdapter()
                                toggleContactSyncLayout(false)
                            }
                        }
                        RxEventType.CONTACT_CLICKED -> {
                            if (isAdded && adapter != null && action.items != null && action.items.isNotEmpty() && action.items[0] is Contact) {
                                onContactClicked(action.items[0] as Contact)
                            }
                        }
                    }
                }
            }
        }, { t: Throwable? -> t?.printStackTrace() }))
    }

    private fun toggleContactSyncLayout(isVisible: Boolean) {
        contactSync_layout?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun setPhoto() {
        val profile = SharedPreferenceManager.getUser()
        if (CommonUtil.textIsNotEmpty(profile?.originalAvatar)) {
            ImageManager.loadImage(ivContactImage, profile?.originalAvatar)
            ivContactImage?.visibility = View.VISIBLE
        } else {
//            tvContactPlaceholder.setTextSize(TypedValue.COMPLEX_UNIT_PX, requireContext().resources.getDimensionPixelSize(R.dimen._47ssp).toFloat())
            tvContactPlaceholder?.text = profile?.getInitialsName()
        }
    }

    private fun setHomeAdapter() {
        val items: ArrayList<HomeDataItem> = arrayListOf()
        val recentlyConnectedContactList = ContactsHelper.getDBRecentlyConnectedContactList()
        if (recentlyConnectedContactList.isNotEmpty() && recentlyConnectedContactList.size >= 1) {
            recentlyListenedRowExists = true
            items.add(HomeDataItem(type = RECENTLY_CONNECTED_CONTACTS, title = "Recently Connected", contacts = recentlyConnectedContactList, false))
        }

        val availableContacts = ContactsHelper.getAvailableContactList()
        items.add(HomeDataItem(type = AVAILABLE_CONTACTS, title = "Phone Contacts", contacts = availableContacts, false))
        adapter = HomeAdapter(requireContext(), HomeDataResponse(items = items, hasMore = false)) { it: Any, pos: Int, type: String, it2: Any? ->
            if (it is Contact) {
                if (type == AVAILABLE_CONTACTS || type == RECENTLY_CONNECTED_CONTACTS) {
                    onContactClicked(it)
                    if (it.userPtrId != null) {
                        viewModel?.ringBell(it.userPtrId!!)
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

    private fun onContactClicked(it: Contact) {
        ContactsHelper.updateLastConnected(it.phone!!)
        it.lastConnected = TimeUtils.nowDate
        if (!recentlyListenedRowExists) {
            val contactList: ArrayList<Contact> = ArrayList()
            contactList.add(it)
            recentlyListenedRowExists = true
            adapter?.addRecentlyListenedRow(HomeDataItem(type = RECENTLY_CONNECTED_CONTACTS, title = "Recently Connected", contacts = contactList, false))
        } else {
            adapter?.updateRecentlyConnected(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appDisposable.dispose()
        viewModel?.onDestroy()
    }

    override fun onApiFailure(statusCode: Int, message: String) {
        if (isAdded && statusCode == 400) {
            FirebaseAuthUserManager.retrieveIdToken(true)
        }
    }

    override fun onRingBellApiSuccess(response: GenericResponse) {
        if (isVisible && response != null) {
            showToast("Bell Ringed", Toast.LENGTH_SHORT)
        }
    }
}