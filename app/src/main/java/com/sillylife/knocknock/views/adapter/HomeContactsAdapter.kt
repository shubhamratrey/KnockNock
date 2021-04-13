package com.sillylife.knocknock.views.adapter

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sillylife.knocknock.R
import com.sillylife.knocknock.constants.Constants.RECENTLY_LOWER_LIMIT
import com.sillylife.knocknock.models.Contact
import com.sillylife.knocknock.models.HomeDataItem
import com.sillylife.knocknock.utils.CommonUtil
import com.sillylife.knocknock.utils.ImageManager
import com.sillylife.knocknock.views.adapter.HomeAdapter.Companion.HomeType.Companion.RECENTLY_CONNECTED_CONTACTS
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_home_grid_item.*
import kotlinx.android.synthetic.main.layout_contact.*


class HomeContactsAdapter(val context: Context,
                          val homeDataItem: HomeDataItem,
                          val listener: ContactsAdapterListener) : RecyclerView.Adapter<HomeContactsAdapter.ViewHolder>() {
    var pageNo = 0
    var commonItemList = ArrayList<Any>()

    companion object {
        const val CONTACTS_VIEW = 0
        const val PROGRESS_VIEW = 1
    }


    init {
        if (homeDataItem.contacts != null && homeDataItem.contacts.isNotEmpty()) {
            commonItemList.addAll(homeDataItem.contacts)
            if (homeDataItem.hasNext != null && homeDataItem.hasNext!!) {
                commonItemList.add(HomeAdapter.PROGRESS_VIEW)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (commonItemList[position] is Contact) {
            CONTACTS_VIEW
        } else {
            PROGRESS_VIEW
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = when (viewType) {
            CONTACTS_VIEW -> LayoutInflater.from(context).inflate(R.layout.item_home_grid_item, parent, false)
            else -> LayoutInflater.from(context).inflate(R.layout.item_progress, parent, false)
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder.itemViewType) {
            CONTACTS_VIEW -> {
                val contact = commonItemList[holder.adapterPosition] as Contact
                holder.tvContactPlaceholder.text = contact.getInitialsName()
                holder.tvContactPlaceholder.visibility = View.VISIBLE
                if (CommonUtil.textIsNotEmpty(contact.image)) {
                    ImageManager.loadImage(holder.ivContactImage, contact.image)
                    holder.ivContactImage.visibility = View.VISIBLE
                    holder.tvContactPlaceholder.visibility = View.INVISIBLE
                } else {
                    holder.ivContactImage.visibility = View.INVISIBLE
                    holder.tvContactPlaceholder.visibility = View.VISIBLE
                }

                if (contact.hasKnocked != null && contact.hasKnocked!!) {
                    holder.cvContactImage.strokeColor = ContextCompat.getColor(context, R.color.coral);
                    holder.cvContactImage.strokeWidth = context.resources.getDimensionPixelSize(R.dimen._2sdp)
                } else {
                    holder.cvContactImage.strokeColor = ContextCompat.getColor(context, R.color.silver_sand);
                    holder.cvContactImage.strokeWidth = 0
                }
                holder.cvContactImage.invalidate()

                holder.tvPrimaryText?.text = contact.name?.split(' ')!![0]
                if (homeDataItem.type == RECENTLY_CONNECTED_CONTACTS) {
                    holder.tvSecondaryText?.text = contact.lastConnectedDateString()
                    holder.tvSecondaryText.visibility = View.VISIBLE
                } else {
                    holder.tvSecondaryText.visibility = View.GONE
                }

                holder.containerView.setOnClickListener {
                    listener.onContactClicked(contact, position, holder.containerView)
                }

                val resources = context.resources
                if (homeDataItem.type?.equals(RECENTLY_CONNECTED_CONTACTS) == true) {
                    val layoutParams: ViewGroup.LayoutParams = holder.cvContactImage.layoutParams
                    layoutParams.height = resources.getDimensionPixelSize(R.dimen._70sdp)
                    layoutParams.width = resources.getDimensionPixelSize(R.dimen._70sdp)
                    holder.cvContactImage.requestLayout()
                    holder.tvContactPlaceholder.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen._30ssp).toFloat())
                } else {
                    val layoutParams: ViewGroup.LayoutParams = holder.cvContactImage.layoutParams
                    layoutParams.height = context.resources.getDimensionPixelSize(R.dimen._50sdp)
                    layoutParams.width = context.resources.getDimensionPixelSize(R.dimen._50sdp)
                    holder.cvContactImage.requestLayout()
                    holder.tvContactPlaceholder.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen._22ssp).toFloat())
                }

                //Analytics stuff
                listener.onImpression(contact, position)
            }
        }

        if (holder.adapterPosition == itemCount - 1) {
            if (homeDataItem.hasNext != null && homeDataItem.hasNext!!) {
                listener.onLoadMoreData(pageNo)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (homeDataItem.type == RECENTLY_CONNECTED_CONTACTS && commonItemList.size > RECENTLY_LOWER_LIMIT) {
            RECENTLY_LOWER_LIMIT
        } else {
            commonItemList.size
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        val contact = commonItemList[holder.adapterPosition] as Contact
        holder.tvContactPlaceholder.text = contact.getInitialsName()
        holder.tvContactPlaceholder.visibility = View.VISIBLE
        if (CommonUtil.textIsNotEmpty(contact.image)) {
            ImageManager.loadImage(holder.ivContactImage, contact.image)
            holder.ivContactImage.visibility = View.VISIBLE
            holder.tvContactPlaceholder.visibility = View.INVISIBLE
        } else {
            holder.ivContactImage.visibility = View.INVISIBLE
            holder.tvContactPlaceholder.visibility = View.VISIBLE
        }
    }

    fun updateRecentlyConnected(contact: Contact) {
        var lastIndex = 0
        for (i in commonItemList.indices) {
            if (commonItemList[i] is Contact && (commonItemList[i] as Contact).phone == contact.phone) {
                lastIndex = i
                break
            }
        }
        if (lastIndex != 0) {
            commonItemList.remove(contact);
            commonItemList.add(0, contact);
            notifyItemMoved(lastIndex, 0)
            notifyItemChanged(0, contact)
        } else {
//            if (!commonItemList.contains(contact)) {
//                notifyItemInserted(0)
//                commonItemList.add(0, contact)
//            }
            if (commonItemList.size > 0 && (commonItemList[lastIndex] as Contact).phone != contact.phone) {
                notifyItemInserted(0)
                commonItemList.add(0, contact)
            } else {
                notifyItemChanged(0, contact)
            }
        }
    }
//
//    fun addMoreContactsData(items: ArrayList<Contact>, hasNext: Boolean) {
//        oldCount = itemCount
//        this.hasNext = hasNext
//        page_no++
//        commonItemList.clear()
//        commonItemList.addAll(items)
//        notifyItemRangeInserted(oldCount, itemCount - 1)
//    }

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer

    interface ContactsAdapterListener {
        fun onContactClicked(contact: Contact, position: Int, view: View?)
        fun onImpression(contact: Contact, itemRank: Int)
        fun onLoadMoreData(pageNo: Int)
    }
}