package com.sillylife.knocknock.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.sillylife.knocknock.R
import com.sillylife.knocknock.constants.Constants
import com.sillylife.knocknock.helpers.ContactsHelper
import com.sillylife.knocknock.models.Contact
import com.sillylife.knocknock.models.HomeDataItem
import com.sillylife.knocknock.models.responses.HomeDataResponse
import com.sillylife.knocknock.services.sharedpreference.SharedPreferenceManager
import com.sillylife.knocknock.views.adapter.HomeAdapter
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : BaseFragment() {

    companion object {
        val TAG = HomeFragment::class.java.simpleName
        fun newInstance() = HomeFragment()
    }

    private var adapter: HomeAdapter? = null
    var recentlyListenedRowExists: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val contact = ContactsHelper.getPhoneContactList()
        val recentlyConnectedContactList = ContactsHelper.getRecentlyConnectedContactList()
        val items: ArrayList<HomeDataItem> = arrayListOf()
        if (recentlyConnectedContactList.isNotEmpty() && recentlyConnectedContactList.size >= 1) {
            recentlyListenedRowExists = true
            items.add(HomeDataItem(type = Constants.HomeType.RECENTLY_CONNECTED, title = "Recently Connected", contacts = recentlyConnectedContactList, false))
        }
        items.add(HomeDataItem(type = Constants.HomeType.CONTACT_LIST, title = "Phone Contacts", contacts = contact, false))
        setHomeAdapter(HomeDataResponse(items = items, hasMore = false))

        fab.setOnClickListener {
            SharedPreferenceManager.storeRecentlyConnectedContacts(ArrayList<Contact>())
        }
    }

    private fun setHomeAdapter(homeDataResponse: HomeDataResponse) {
        adapter = HomeAdapter(requireContext(), homeDataResponse) { it: Any, pos: Int, type: String, it2:Any?->
            if (it is Contact) {
                if (type == Constants.HomeType.CONTACT_LIST) {
                    ContactsHelper.addRecentlyConnectedContact(it)
                    if (!recentlyListenedRowExists){
                        val contactList: ArrayList<Contact> = ArrayList()
                        contactList.add(it)
                        recentlyListenedRowExists = true
                        adapter?.addRecentlyListenedRow(HomeDataItem(type = Constants.HomeType.RECENTLY_CONNECTED, title = "Recently Connected", contacts = contactList, false))
                    } else {
                        adapter?.updateRecentlyConnected(it)
                    }
                }
            }
            if (type == Constants.IMPRESSION){
                Log.d("IMPRESSION", "Contact - $it | Source - $it2")
            }
        }
        rcvAll?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        ViewCompat.setNestedScrollingEnabled(rcvAll, false)
        rcvAll?.adapter = adapter
    }
}