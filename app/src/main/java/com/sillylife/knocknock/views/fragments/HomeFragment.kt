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
import com.sillylife.knocknock.database.MapDbEntities
import com.sillylife.knocknock.helpers.ContactsHelper
import com.sillylife.knocknock.models.Contact
import com.sillylife.knocknock.models.HomeDataItem
import com.sillylife.knocknock.models.responses.HomeDataResponse
import com.sillylife.knocknock.services.sharedpreference.SharedPreferenceManager
import com.sillylife.knocknock.views.adapter.HomeAdapter
import kotlinx.android.synthetic.main.fragment_home.*
import kotlin.collections.ArrayList

class HomeFragment : BaseFragment(){

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
            items.add(HomeDataItem(type = Constants.HomeType.RECENTLY_CONNECTED, title = "Recently Connected", contacts = recentlyConnectedContactList, false))
        }
        val contact = ContactsHelper.getDBPhoneContactList()
        items.add(HomeDataItem(type = Constants.HomeType.CONTACT_LIST, title = "Phone Contacts", contacts = contact, false))
        setHomeAdapter(HomeDataResponse(items = items, hasMore = false))

        fab.setOnClickListener {
            SharedPreferenceManager.storeRecentlyConnectedContacts(ArrayList<Contact>())
            ContactsHelper.updatePhoneContactsToDB()
        }
    }

    private fun setHomeAdapter(homeDataResponse: HomeDataResponse) {
        adapter = HomeAdapter(requireContext(), homeDataResponse) { it: Any, pos: Int, type: String, it2:Any?->
            if (it is Contact) {
                if (type == Constants.HomeType.CONTACT_LIST) {
                    ContactsHelper.updateLastConnected(it.phone!!)
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