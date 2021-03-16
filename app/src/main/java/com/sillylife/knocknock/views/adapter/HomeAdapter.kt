package com.sillylife.knocknock.views.adapter

import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sillylife.knocknock.R
import com.sillylife.knocknock.constants.Constants
import com.sillylife.knocknock.constants.Constants.HOME_PAGINATE
import com.sillylife.knocknock.constants.Constants.HOME_SCROLL
import com.sillylife.knocknock.constants.Constants.HomeType
import com.sillylife.knocknock.constants.Constants.IMPRESSION
import com.sillylife.knocknock.models.Contact
import com.sillylife.knocknock.models.HomeDataItem
import com.sillylife.knocknock.models.responses.HomeDataResponse
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_contact_grid.contactName
import kotlinx.android.synthetic.main.item_contact_linear.*
import kotlinx.android.synthetic.main.item_home_recyclerview.*

class HomeAdapter(val context: Context, val response: HomeDataResponse, val listener: (Any, Int, String, Any?) -> Unit) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    val commonItemLists = ArrayList<Any>()
    var pageNo = 0
    var scrollBackPosition: Int = 6
    var TAG = HomeAdapter::class.java.simpleName


    companion object {
        const val PROGRESS_VIEW = 0
        const val RECYCLERVIEW_WITH_HEADER = 1
        const val HEADER = 2
        const val CONTACT_ITEM = 3
        const val SCROLLBACK_SHOW_ID = -111
        const val SCROLLBACK_HIDE_ID = -222

        const val UPDATE_RECENTLY_CONNECTED = "update_recently_connected"
        const val UPDATE_ALL_CONTACT_DATA = "update_all_contact_data"
    }

    init {
        if (response.items != null && response.items!!.isNotEmpty()) {
            pageNo++
            for (item in response.items!!) {
                if (item.type == HomeType.CONTACT_LIST) {
                    commonItemLists.add(item.title!!)
                    commonItemLists.addAll(item.contacts!!)
                } else {
                    commonItemLists.add(item)
                }
            }
            if (response.hasMore != null && response.hasMore!!) {
                commonItemLists.add(PROGRESS_VIEW)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            commonItemLists[position] is HomeDataItem -> {
                val homeDataItem = commonItemLists[position] as HomeDataItem
                when (homeDataItem.type) {
                    HomeType.RECENTLY_CONNECTED -> RECYCLERVIEW_WITH_HEADER
                    else -> PROGRESS_VIEW
                }
            }
            commonItemLists[position] is Contact -> {
                CONTACT_ITEM
            }
            commonItemLists[position] is String -> {
                HEADER
            }
            else -> {
                PROGRESS_VIEW
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = when (viewType) {
            RECYCLERVIEW_WITH_HEADER -> LayoutInflater.from(context).inflate(R.layout.item_home_recyclerview, parent, false)
            CONTACT_ITEM -> LayoutInflater.from(context).inflate(R.layout.item_contact_linear, parent, false)
            HEADER -> LayoutInflater.from(context).inflate(R.layout.item_home_recyclerview, parent, false)
            else -> LayoutInflater.from(context).inflate(R.layout.item_progress, parent, false)
        }
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return commonItemLists.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (!payloads.isNullOrEmpty()) {
            for (any in payloads) {
                if (any is String) {
                    when (any) {
//                        UPDATE_ALL_CONTACT_DATA -> { // to enable infinite scrolling for CUs
//                            val adapter = holder.rcvAll.adapter as ContactsAdapter
//                            val homeDataItem = commonItemLists[holder.adapterPosition]
//                            if (homeDataItem is HomeDataItem) {
//                                adapter.addMoreContactsData(homeDataItem.contacts!!, homeDataItem.hasNext!!)
//                            }
//                        }
                    }
                } else if (any is Contact) {
                    if (holder.rcvAll.adapter is ContactsAdapter) {
                        val adapter = holder.rcvAll.adapter as ContactsAdapter
                        if (adapter.layoutManager == Constants.LayoutManager.GRID_LAYOUT_MANAGER) {
                            adapter.updateRecentlyConnected(any)
                        }
                    }
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder.itemViewType) {
            RECYCLERVIEW_WITH_HEADER -> {
                val homeDataItem = commonItemLists[holder.adapterPosition] as HomeDataItem
                when (homeDataItem.type) {
                    HomeType.RECENTLY_CONNECTED -> setRecentlyConnectedData(holder, holder.adapterPosition, homeDataItem)
                }
            }
            CONTACT_ITEM -> {
                setContactItemData(holder, holder.adapterPosition)
            }
            HEADER -> {
                if (commonItemLists[holder.adapterPosition] is String && commonItemLists[holder.adapterPosition] != null) {
                    holder.titleTv.text = commonItemLists[holder.adapterPosition] as String
                    holder.rcvAll?.visibility = View.GONE
                }
            }

        }

        if (holder.adapterPosition == itemCount - 1) {
            if (response.hasMore != null && response.hasMore!!) {
                listener(pageNo, -1, HOME_PAGINATE, null)
            }

            if (position > scrollBackPosition) {
                // show scroll back visible
                listener(SCROLLBACK_SHOW_ID, -1, HOME_SCROLL, null)
            } else {
                // hide scroll back visible
                listener(SCROLLBACK_HIDE_ID, -1, HOME_SCROLL, null)
            }
        }
    }

    private fun setRecentlyConnectedData(holder: ViewHolder, position: Int, homeDataItem: HomeDataItem) {
        holder.titleTv.text = homeDataItem.title
        if (homeDataItem.contacts != null) {
            val adapter = ContactsAdapter(context = context, items = homeDataItem.contacts, hasNext = false,
                    layoutManager = Constants.LayoutManager.GRID_LAYOUT_MANAGER,
                    object : ContactsAdapter.ContactsAdapterListener {
                        override fun onContactClicked(contact: Contact, position: Int, view: View?) {

                        }

                        override fun onImpression(contact: Contact, itemRank: Int) {
                            listener(contact, itemRank, IMPRESSION, "RECENTLY")
                        }

                        override fun onLoadMoreData(pageNo: Int) {

                        }
                    })

            if (holder.rcvAll.adapter == null) {
                holder.rcvAll?.addItemDecoration(GridItemDecoration(context.resources.getDimensionPixelSize(R.dimen.dp_20), context.resources.getDimensionPixelSize(R.dimen.dp_20), context.resources.getDimensionPixelSize(R.dimen.dp_20), context.resources.getDimensionPixelSize(R.dimen.dp_20)))
            }
            adapter.setHasStableIds(true)
//            holder.rcvAll.layoutManager = GridLayoutManager(context, 4, RecyclerView.VERTICAL, false)
            holder.rcvAll?.layoutManager = WrapContentGridLayoutManager(context, 4)
            holder.rcvAll?.adapter = adapter
            holder.rcvAll?.setHasFixedSize(true)
        }
    }

    private fun setContactItemData(holder: ViewHolder, position: Int) {
        val contact = commonItemLists[holder.adapterPosition] as Contact
        holder.contactName?.text = contact.name
        holder.contactPhoneNumber?.text = contact.phone

        holder.containerView.setOnClickListener {
            listener(contact, holder.adapterPosition, HomeType.CONTACT_LIST, null)
        }

        listener(contact, holder.adapterPosition, IMPRESSION, "PHONE_CONTACTS")
    }

    fun updateRecentlyConnected(contact: Contact) {
        for (i in commonItemLists.indices) {
            if (commonItemLists[i] is HomeDataItem && (commonItemLists[i] as HomeDataItem).type?.equals(HomeType.RECENTLY_CONNECTED) == true) {
                notifyItemChanged(i, contact)
                break
            }
        }
    }

    fun addMoreData(response: HomeDataResponse?) {
        val oldSize = itemCount
        commonItemLists.remove(PROGRESS_VIEW)
        if (response != null && response.items!!.isNotEmpty()) {
            pageNo++
            this.response.items!!.addAll(response.items!!)
            this.response.hasMore = response.hasMore
            commonItemLists.addAll(response.items!!)
        }
        if (response?.hasMore!!) {
            commonItemLists.add(PROGRESS_VIEW)
        }
        notifyItemRangeChanged(oldSize, itemCount)
    }

    fun addRecentlyListenedRow(item: HomeDataItem) {
        var homeDataItem: HomeDataItem? = null
        var itemIndex = 0
        if (commonItemLists != null && commonItemLists.size > 0) {
            for (i in commonItemLists.indices) {
                val a = commonItemLists[i]
                if (a is HomeDataItem && a.type.equals(HomeType.RECENTLY_CONNECTED)) {
                    homeDataItem = a
                    itemIndex = i
                    break
                }
            }
        }

        if (homeDataItem == null || homeDataItem.contacts.isNullOrEmpty()) {
            commonItemLists.add(0, item)
            notifyItemInserted(0)
        } else {
            item.contacts?.let { homeDataItem.contacts?.addAll(it) }
            notifyItemChanged(itemIndex)
        }
    }

    fun addMoreCUData(response: HomeDataResponse) {
        var homeDataItem: HomeDataItem? = null
        var itemIndex = 0
        if (response.contacts != null && response.contacts!!.isNotEmpty()) {
            if (commonItemLists.size > 0) {
                for (i in commonItemLists.indices) {
                    val a = commonItemLists[i]
                    if (a is HomeDataItem && a.contacts != null) {
                        homeDataItem = a
                        itemIndex = i
                        break
                    }
                }
            }
            homeDataItem?.contacts?.addAll(response.contacts!!)
            homeDataItem?.hasNext = response.hasMore ?: false
            if (homeDataItem != null) {
                when {
                    homeDataItem.type.equals(HomeType.CONTACT_LIST) -> {
                        notifyItemChanged(itemIndex, UPDATE_ALL_CONTACT_DATA)
                    }
                }
            }
        }
    }

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer


    class GridItemDecoration(val leftMargin: Int, val topMargin: Int, val rightMargin: Int, val bottomMargin: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            if (parent.getChildAdapterPosition(view) == 0 || parent.getChildAdapterPosition(view) == 1) {
                outRect.top = topMargin
                outRect.bottom = bottomMargin
            }

            if (parent.getChildAdapterPosition(view) % 2 == 0) {
                outRect.right = rightMargin
            } else {
                outRect.left = leftMargin
            }
        }
    }

    inner class WrapContentGridLayoutManager(mContext: Context, spanCount: Int) : GridLayoutManager(mContext, spanCount) {
        override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
            try {
                super.onLayoutChildren(recycler, state)
            } catch (e: IndexOutOfBoundsException) {
                Log.e("Error", "IndexOutOfBoundsException in RecyclerView happens")
            }
        }
    }
}
