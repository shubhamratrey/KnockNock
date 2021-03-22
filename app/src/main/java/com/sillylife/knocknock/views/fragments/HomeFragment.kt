package com.sillylife.knocknock.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sillylife.knocknock.R
import com.sillylife.knocknock.constants.Constants
import com.sillylife.knocknock.database.DBHelper
import com.sillylife.knocknock.helpers.ContactsHelper
import com.sillylife.knocknock.models.Contact
import com.sillylife.knocknock.models.HomeDataItem
import com.sillylife.knocknock.models.responses.HomeDataResponse
import com.sillylife.knocknock.views.adapter.HomeAdapter
import com.sillylife.knocknock.views.adapter.HomeAdapter.Companion.HomeType.Companion.AVAILABLE_CONTACTS
import com.sillylife.knocknock.views.adapter.HomeAdapter.Companion.HomeType.Companion.RECENTLY_CONNECTED_CONTACTS
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : BaseFragment() {

    companion object {
        val TAG = HomeFragment::class.java.simpleName
        fun newInstance() = HomeFragment()
    }

    private var dbHelper: DBHelper? = null
    private var adapter: HomeAdapter? = null
    var recentlyListenedRowExists: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
//        val contact = ContactsHelper.getAvailableContactList()
        items.add(HomeDataItem(type = AVAILABLE_CONTACTS, title = "Phone Contacts", contacts = contact, false))
        setHomeAdapter(HomeDataResponse(items = items, hasMore = false))

        cvContactImage?.setOnClickListener {
            addFragment(SettingsFragment.newInstance(), SettingsFragment.TAG)
        }
        fabButton?.setOnClickListener {
            addFragment(InviteFragment.newInstance(), InviteFragment.TAG)
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
                Log.d("IMPRESSION", "Contact - $it | Source - $it2")
            }
        }
        rcvAll?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        ViewCompat.setNestedScrollingEnabled(rcvAll, false)
        rcvAll?.adapter = adapter
    }
}