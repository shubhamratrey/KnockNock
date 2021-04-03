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
import com.sillylife.knocknock.models.Contact
import com.sillylife.knocknock.models.HomeDataItem
import com.sillylife.knocknock.models.responses.GenericResponse
import com.sillylife.knocknock.models.responses.HomeDataResponse
import com.sillylife.knocknock.services.AppDisposable
import com.sillylife.knocknock.services.sharedpreference.SharedPreferenceManager
import com.sillylife.knocknock.utils.CommonUtil
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
    private var adapter: HomeAdapter? = null
    var recentlyListenedRowExists: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, FragmentViewModelFactory(this@HomeFragment))
                .get(HomeFragmentViewModel::class.java)

        val items: ArrayList<HomeDataItem> = arrayListOf()

        val recentlyConnectedContactList = ContactsHelper.getDBRecentlyConnectedContactList()
        if (recentlyConnectedContactList.isNotEmpty() && recentlyConnectedContactList.size >= 1) {
            recentlyListenedRowExists = true
            items.add(HomeDataItem(type = RECENTLY_CONNECTED_CONTACTS, title = "Recently Connected", contacts = recentlyConnectedContactList, false))
        }

        val availableContacts = ContactsHelper.getAvailableContactList()
        items.add(HomeDataItem(type = AVAILABLE_CONTACTS, title = "Phone Contacts", contacts = availableContacts, false))
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

    override fun onDestroy() {
        super.onDestroy()
        appDisposable.dispose()
        viewModel?.onDestroy()
    }

    override fun onApiFailure(statusCode: Int, message: String) {

    }

    override fun onRingBellApiSuccess(response: GenericResponse) {
        if (isVisible && response != null) {
            showToast("Bell Ringed", Toast.LENGTH_SHORT)
        }
    }
}