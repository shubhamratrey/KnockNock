package com.sillylife.knocknock.views.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sillylife.knocknock.R
import com.sillylife.knocknock.constants.Constants
import com.sillylife.knocknock.constants.Constants.RECENTLY_LOWER_LIMIT
import com.sillylife.knocknock.models.Contact
import com.sillylife.knocknock.utils.ImageManager
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_contact_grid.*
import kotlinx.android.synthetic.main.item_contact_grid.contactName
import kotlinx.android.synthetic.main.item_contact_linear.*

class ContactsAdapter(val context: Context,
                      var items: ArrayList<Contact>,
                      var hasNext: Boolean = false,
                      val layoutManager: String,
                      val listener: ContactsAdapterListener) : RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {
    var page_no = 2
    var oldCount = 0
    var commonItemList = ArrayList<Any>()

    init {
        commonItemList.addAll(items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (layoutManager == Constants.LayoutManager.GRID_LAYOUT_MANAGER) {
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_contact_grid, parent, false))
        } else {
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_contact_linear, parent, false))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = commonItemList[holder.adapterPosition] as Contact
        when (layoutManager) {
            Constants.LayoutManager.GRID_LAYOUT_MANAGER -> {
                holder.contactImageIv?.setImageResource(R.drawable.alpha_white_solid_circle)
                ImageManager.loadImageCircular(holder.contactImageIv, contact.image)
                holder.contactLastConnected?.text = contact.lastConnectedDateString()
            }
            Constants.LayoutManager.LINEAR_LAYOUT_MANAGER -> {
                //  to enable infinite scrolling
//                val pos = if (itemCount - 10 < 0) itemCount - 1 else itemCount - 10
//                if (holder.adapterPosition == pos && hasNext) {
//                    oldCount = itemCount
//                    listener.onLoadMoreData(page_no)
//                }
                holder.contactPhoneNumber?.text = contact.phone
            }
        }
        holder.contactName?.text = contact.name

        holder.containerView.setOnClickListener {
            listener.onContactClicked(contact, position, holder.containerView)
        }
        //Analytics stuff
        listener.onImpression(contact, position)
    }

    override fun getItemId(position: Int): Long {
//        if (position >= 0 && position < items.size) {
//            if (items[position] is Contact) {
//                return (items[position]).phone!!.toLong()
//            }
//            return super.getItemId(position)
//        }
        return super.getItemId(position)
    }

    override fun getItemCount(): Int {
        return if (layoutManager == Constants.LayoutManager.GRID_LAYOUT_MANAGER && commonItemList.size > RECENTLY_LOWER_LIMIT) {
            RECENTLY_LOWER_LIMIT
        } else {
            commonItemList.size
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.contactImageIv.setImageResource(R.drawable.ic_place_holder_colors)
    }

    fun updateRecentlyConnected(contact: Contact) {
        var lastIndex = 0
        for (i in commonItemList.indices) {
            val a = commonItemList[i]
            if (a is Contact && a.phone.equals(contact.phone)) {
                lastIndex = i
                break
            }
        }
        if (lastIndex != 0) {
            notifyItemMoved(lastIndex, 0)
        } else {
            items.add(0, contact)
            commonItemList.add(0, contact)
            notifyItemInserted(0)
        }
    }

    fun addMoreContactsData(items: ArrayList<Contact>, hasNext: Boolean) {
        oldCount = itemCount
        this.hasNext = hasNext
        page_no++
        commonItemList.clear()
        commonItemList.addAll(items)
        notifyItemRangeInserted(oldCount, itemCount - 1)
    }

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer

    interface ContactsAdapterListener {
        fun onContactClicked(contact: Contact, position: Int, view: View?)
        fun onImpression(contact: Contact, itemRank: Int)
        fun onLoadMoreData(pageNo: Int)
    }
}