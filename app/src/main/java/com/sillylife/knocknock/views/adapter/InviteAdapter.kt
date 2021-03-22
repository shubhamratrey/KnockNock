package com.sillylife.knocknock.views.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sillylife.knocknock.R
import com.sillylife.knocknock.models.Contact
import com.sillylife.knocknock.utils.CommonUtil
import com.sillylife.knocknock.utils.ImageManager
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_contact_invite.*
import kotlinx.android.synthetic.main.layout_contact.*

class InviteAdapter(val context: Context,
                    var items: ArrayList<Contact>,
                    val listener: Listeners) : RecyclerView.Adapter<InviteAdapter.ViewHolder>(), Filterable {
    var commonItemList = ArrayList<Any>()
    private var valueFilter: ValueFilter? = null

    init {
        commonItemList.addAll(items)
        commonItemList.add(FOOTER)
    }

    companion object {
        const val PROGRESS_VIEW = 0
        const val CONTACT_ITEM = 1
        const val FOOTER = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            commonItemList[position] is Contact -> {
                CONTACT_ITEM
            }
            commonItemList[position] is Int && commonItemList[position] == FOOTER -> {
                FOOTER
            }
            else -> {
                PROGRESS_VIEW
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = when (viewType) {
            CONTACT_ITEM -> LayoutInflater.from(context).inflate(R.layout.item_contact_invite, parent, false)
            FOOTER -> LayoutInflater.from(context).inflate(R.layout.item_footer, parent, false)
            else -> LayoutInflater.from(context).inflate(R.layout.item_progress, parent, false)
        }
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder.itemViewType) {
            CONTACT_ITEM -> {
                val contact = commonItemList[holder.adapterPosition] as Contact
                if (CommonUtil.textIsNotEmpty(contact.image)) {
                    holder.ivContactImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_place_holder_colors)
                            ?: throw IllegalArgumentException("Cannot load drawable"))
                    ImageManager.loadImage(holder.ivContactImage, contact.image)
                    holder.ivContactImage.visibility = View.VISIBLE
                    holder.tvContactPlaceholder.visibility = View.INVISIBLE
                } else {
                    holder.tvContactPlaceholder.text = contact.getInitialsName()
                    holder.ivContactImage.visibility = View.INVISIBLE
                    holder.tvContactPlaceholder.visibility = View.VISIBLE
                }

                holder.tvPrimary?.text = contact.name
                holder.tvSecondary?.text = contact.phone

                holder.containerView.isFocusable = false
                holder.containerView.isEnabled = false
                holder.tvButton.text = null
                holder.tvButton.visibility = View.GONE
                holder.tvJoined.visibility = View.GONE
                when {
                    contact.availableOnPlatform!! -> {
                        holder.containerView.isFocusable = true
                        holder.containerView.isEnabled = true
                        holder.tvJoined.visibility = View.VISIBLE
                        holder.containerView.setOnClickListener {
                            listener.onContactClicked(contact, position, holder.containerView)
                        }
                    }
                    contact.hasInvited!! -> {
                        holder.tvButton.visibility = View.VISIBLE
                        holder.tvButton.text = context.getString(R.string.send_reminder)
                        holder.tvButton.setOnClickListener {
                            listener.onSendRemindedClicked(contact, position, holder.containerView)
                        }
                    }
                    else -> {
                        holder.tvButton.visibility = View.VISIBLE
                        holder.tvButton.text = context.getString(R.string.invite)
                        holder.tvButton.setOnClickListener {
                            listener.onInviteClicked(contact, position, holder.containerView)
                        }
                    }
                }

                //Analytics stuff
                listener.onImpression(contact, position)
            }
        }


    }

    override fun getItemCount(): Int {
        return commonItemList.size
    }

    fun updateContact(contact: Contact) {
        for (i in commonItemList.indices) {
            if (commonItemList[i] is Contact) {
                if ((commonItemList[i] as Contact).phone?.equals(contact.phone) == true) {
                    commonItemList[i] = contact
                    notifyItemChanged(i)
                    break
                }
            }
        }
    }

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer

    inner class ValueFilter : Filter() {
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            commonItemList = results!!.values as ArrayList<Any>
            notifyDataSetChanged()
        }

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filterResults = FilterResults()
            if (TextUtils.isEmpty(constraint)) {
                filterResults.count = items.size
                filterResults.values = items
            } else {
                val filterList: ArrayList<Contact> = ArrayList()
                for (item in items) {
                    if (item.phone?.contains(constraint!!, ignoreCase = true)!! || (item.name?.contains(constraint!!, ignoreCase = true)!!)) {
                        filterList.add(item)
                    }
                }
                filterResults.count = filterList.size
                filterResults.values = filterList
            }
            return filterResults
        }
    }

    override fun getFilter(): Filter {
        if (valueFilter == null) {
            valueFilter = ValueFilter()
        }
        return valueFilter!!
    }

    interface Listeners {
        fun onContactClicked(contact: Contact, position: Int, view: View?)
        fun onInviteClicked(contact: Contact, position: Int, view: View?)
        fun onSendRemindedClicked(contact: Contact, position: Int, view: View?)
        fun onImpression(contact: Contact, itemRank: Int)
    }
}