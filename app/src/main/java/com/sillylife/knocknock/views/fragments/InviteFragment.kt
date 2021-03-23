package com.sillylife.knocknock.views.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sillylife.knocknock.R
import com.sillylife.knocknock.database.DBHelper
import com.sillylife.knocknock.helpers.ContactsHelper
import com.sillylife.knocknock.models.Contact
import com.sillylife.knocknock.utils.CommonUtil
import com.sillylife.knocknock.views.adapter.InviteAdapter
import kotlinx.android.synthetic.main.fragment_home.rcvAll
import kotlinx.android.synthetic.main.fragment_invite.*

class InviteFragment : BaseFragment() {

    companion object {
        val TAG = InviteFragment::class.java.simpleName
        fun newInstance() = InviteFragment()
    }

    private var adapter: InviteAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_invite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        dbHelper = ViewModelProvider(activity!!).get(DBHelper::class.java)
//        ContactsHelper.updatePhoneContactsToDB()
        setAdapter()
        setupSearchView()
        toolbar?.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setAdapter() {
        val contacts = ContactsHelper.getDBPhoneContactList()
        adapter = InviteAdapter(context = requireContext(), items = contacts,
                object : InviteAdapter.Listeners {
                    override fun onContactClicked(contact: Contact, position: Int, view: View?) {
                        showToast(contact.name + contact.phone + " onContactClicked")
                    }

                    override fun onInviteClicked(contact: Contact, position: Int, view: View?) {
                        showToast(contact.name + contact.phone + " onInviteClicked")
                        ContactsHelper.updateContactInvited(contact.phone!!)
                        contact.hasInvited = true
                        adapter?.updateContact(contact)
                    }

                    override fun onSendRemindedClicked(contact: Contact, position: Int, view: View?) {
                        showToast(contact.name + contact.phone + " onSendRemindedClicked")
                    }

                    override fun onImpression(contact: Contact, itemRank: Int) {
                        Log.d("IMPRESSION", "Contact - ${contact.name} | Source - InviteFragment")
                    }
                })

        rcvAll?.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rcvAll?.adapter = adapter


        val HIDE_THRESHOLD = 100f
        val SHOW_THRESHOLD = 50f
        var scrollDist = 0
        rcvAll?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                //  Check scrolled distance against the minimum
                if (searchView.hasFocus() && (scrollDist > HIDE_THRESHOLD) || scrollDist < -SHOW_THRESHOLD) {
                    //  Hide fab & reset scrollDist
//                    searchView.clearFocus()
                    CommonUtil.hideKeyboard(requireContext())
                    scrollDist = 0
                }
                //  Whether we scroll up or down, calculate scroll distance
                if (searchView.hasFocus() && (dy > 0 || dy < 0)) {
                    scrollDist += dy
                }

            }
        })
    }

    private fun setupSearchView() {
        searchView.clearFocus()
        searchView.isFocusable = false
        CommonUtil.hideKeyboard(requireContext())
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            searchView.isFocusedByDefault = false
            CommonUtil.hideKeyboard(requireContext())
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                if (CommonUtil.textIsNotEmpty(newText)) {
                    if (adapter?.filter != null) {
                        adapter?.filter?.filter(newText)
                    }
                }
                return false
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (CommonUtil.textIsNotEmpty(query)) {
                    if (adapter?.filter != null) {
                        adapter?.filter?.filter(query)
                    }
                }
                return false
            }
        })
//        searchView.onActionViewExpanded()
        val textview: TextView = searchView.findViewById(androidx.appcompat.R.id.search_src_text)
        val typeface = ResourcesCompat.getFont(activity!!, R.font.notosans_regular)
        textview.typeface = typeface
        textview.textSize = 16F

        val searchClose: ImageView = searchView.findViewById(androidx.appcompat.R.id.search_close_btn)
        searchClose.setColorFilter(Color.parseColor("#BAB5C6"))

        val v: View = searchView.findViewById(androidx.appcompat.R.id.search_plate)
        v.background = null

        val llSubmitArea: LinearLayout = searchView.findViewById(androidx.appcompat.R.id.submit_area)
        llSubmitArea.background = null

        val closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn) as ImageView
        closeButton.setOnClickListener {
            searchView.setQuery("", false)
            if (adapter != null && adapter?.filter != null) {
                adapter?.filter?.filter("")
                CommonUtil.hideKeyboard(requireContext())
            }
        }

        searchView.setOnQueryTextFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                searchView.setQuery("", false)
                adapter!!.filter.filter("")
                CommonUtil.hideKeyboard(requireContext())
            }
        }
    }
}