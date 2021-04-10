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
import com.sillylife.knocknock.constants.Constants.HOME_PAGINATE
import com.sillylife.knocknock.constants.Constants.IMPRESSION
import com.sillylife.knocknock.models.Contact
import com.sillylife.knocknock.models.HomeDataItem
import com.sillylife.knocknock.models.responses.HomeDataResponse
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_home_grid_layout.*

class HomeAdapter(val context: Context, val response: HomeDataResponse, val listener: (Any, Int, String, Any?) -> Unit) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    val commonItemLists = ArrayList<Any>()
    var pageNo = 0
    var TAG = HomeAdapter::class.java.simpleName


    companion object {
        const val RECENTLY_CONNECTED_CONTACTS_VIEW = 0
        const val AVAILABLE_CONTACTS_VIEW = 1
        const val FOOTER_VIEW = 3
        const val PROGRESS_VIEW = 4

        interface HomeType {
            companion object {
                const val RECENTLY_CONNECTED_CONTACTS = "recently_connected_contacts"
                const val AVAILABLE_CONTACTS = "available_contacts"
            }
        }
    }

    init {
        if (response.items != null && response.items!!.isNotEmpty()) {
            commonItemLists.addAll(response.items!!)
            if (response.hasMore != null && response.hasMore!!) {
                commonItemLists.add(PROGRESS_VIEW)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (commonItemLists[position] is Int) {
            when {
                commonItemLists[position] == PROGRESS_VIEW -> PROGRESS_VIEW
                else -> FOOTER_VIEW
            }
        } else
            when {
                commonItemLists[position] is HomeDataItem -> {
                    val homeDataItem = commonItemLists[position] as HomeDataItem
                    when {
                        homeDataItem.type?.equals(HomeType.RECENTLY_CONNECTED_CONTACTS) == true -> {
                            RECENTLY_CONNECTED_CONTACTS_VIEW
                        }
                        homeDataItem.type?.equals(HomeType.AVAILABLE_CONTACTS) == true -> {
                            AVAILABLE_CONTACTS_VIEW
                        }
                        else -> {
                            FOOTER_VIEW
                        }
                    }
                }
                else -> PROGRESS_VIEW
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = when (viewType) {
            RECENTLY_CONNECTED_CONTACTS_VIEW, AVAILABLE_CONTACTS_VIEW -> LayoutInflater.from(context).inflate(R.layout.item_home_grid_layout, parent, false)
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
                if (any is Contact) {
                    if (holder.rcvAll.adapter is HomeContactsAdapter) {
                        val adapter = holder.rcvAll.adapter as HomeContactsAdapter
                        if (holder.itemViewType == RECENTLY_CONNECTED_CONTACTS_VIEW) {
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
            RECENTLY_CONNECTED_CONTACTS_VIEW, AVAILABLE_CONTACTS_VIEW -> {
                setGridLayoutView(holder)
            }
        }

        if (holder.adapterPosition == itemCount - 1) {
            if (response.hasMore != null && response.hasMore!!) {
                listener(pageNo, -1, HOME_PAGINATE, null)
            }
        }
    }

    private fun setGridLayoutView(holder: ViewHolder) {
        val homeDataItem = commonItemLists[holder.adapterPosition] as HomeDataItem
        holder.titleTv.text = homeDataItem.title
        if (homeDataItem.contacts != null) {
            val adapter = HomeContactsAdapter(context = context, homeDataItem, object : HomeContactsAdapter.ContactsAdapterListener {
                override fun onContactClicked(contact: Contact, position: Int, view: View?) {
                    listener(contact, holder.adapterPosition, homeDataItem.type!!, homeDataItem.type)
                }

                override fun onImpression(contact: Contact, itemRank: Int) {
                    listener(contact, itemRank, IMPRESSION, homeDataItem.type)
                }

                override fun onLoadMoreData(pageNo: Int) {

                }
            })

            adapter.setHasStableIds(true)
            if (homeDataItem.type == HomeType.AVAILABLE_CONTACTS) {
                if (holder.rcvAll.itemDecorationCount == 0) {
                    holder.rcvAll?.addItemDecoration(GridSpacingItemDecoration(4, context.resources.getDimensionPixelSize(R.dimen.dp_20), false))
                }
                holder.rcvAll?.layoutManager = WrapContentGridLayoutManager(context, 4)
            } else {
                if (holder.rcvAll.itemDecorationCount == 0) {
                    holder.rcvAll?.addItemDecoration(GridSpacingItemDecoration(3, context.resources.getDimensionPixelSize(R.dimen.dp_20), false))
                }
                holder.rcvAll?.layoutManager = WrapContentGridLayoutManager(context, 3)
            }
            holder.rcvAll?.adapter = adapter
        }
    }

    fun updateRecentlyConnected(contact: Contact) {
        for (i in commonItemLists.indices) {
            if (commonItemLists[i] is HomeDataItem && (commonItemLists[i] as HomeDataItem).type?.equals(HomeType.RECENTLY_CONNECTED_CONTACTS) == true) {
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
                if (a is HomeDataItem && a.type.equals(HomeType.RECENTLY_CONNECTED_CONTACTS)) {
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
//
//    fun addMoreCUData(response: HomeDataResponse) {
//        var homeDataItem: HomeDataItem? = null
//        var itemIndex = 0
//        if (response.contacts != null && response.contacts!!.isNotEmpty()) {
//            if (commonItemLists.size > 0) {
//                for (i in commonItemLists.indices) {
//                    val a = commonItemLists[i]
//                    if (a is HomeDataItem && a.contacts != null) {
//                        homeDataItem = a
//                        itemIndex = i
//                        break
//                    }
//                }
//            }
//            homeDataItem?.contacts?.addAll(response.contacts!!)
//            homeDataItem?.hasNext = response.hasMore ?: false
//            if (homeDataItem != null) {
//                when {
//                    homeDataItem.type.equals(HomeType.CONTACT_LIST) -> {
//                        notifyItemChanged(itemIndex, UPDATE_ALL_CONTACT_DATA)
//                    }
//                }
//            }
//        }
//    }

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer

    class GridSpacingItemDecoration(private val spanCount: Int, private val spacing: Int, private val includeEdge: Boolean) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view) // item position
            val column = position % spanCount // item column
            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)
                if (position < spanCount) { // top edge
                    outRect.top = spacing
                }
                outRect.bottom = spacing // item bottom
            } else {
                outRect.left = column * spacing / spanCount // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing // item top
                }
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
