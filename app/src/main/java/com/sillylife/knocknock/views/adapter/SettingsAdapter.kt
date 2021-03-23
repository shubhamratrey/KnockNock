package com.sillylife.knocknock.views.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sillylife.knocknock.BuildConfig
import com.sillylife.knocknock.R
import com.sillylife.knocknock.constants.RemoteConfigKeys
import com.sillylife.knocknock.managers.FirebaseAuthUserManager
import com.sillylife.knocknock.managers.FirebaseRemoteConfigManager
import com.sillylife.knocknock.models.SettingItem
import com.sillylife.knocknock.services.sharedpreference.SharedPreferenceManager
import com.sillylife.knocknock.utils.CommonUtil
import com.sillylife.knocknock.utils.ImageManager
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_settings_login_logout.*
import kotlinx.android.synthetic.main.item_settings_profile.*
import kotlinx.android.synthetic.main.item_settings_recyclerview.*
import kotlinx.android.synthetic.main.item_settings_social.*
import kotlinx.android.synthetic.main.item_settings_urls.*
import kotlinx.android.synthetic.main.item_settings_version.*
import kotlinx.android.synthetic.main.layout_contact.*
import org.json.JSONException
import org.json.JSONObject


class SettingsAdapter(val context: Context,
                      val listener: SettingListener) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    val commonItemLists = ArrayList<Any>()

    companion object {
        const val NOTIFICATION_ITEM = 1
        const val WEB_URLS_ITEM = 2
        const val SOCIAL_ITEM = 3
        const val LOGIN_LOGOUT_ITEM = 4
        const val VERSION_CODE_ITEM = 5
        const val PROFILE_ITEM = 6

        const val SOCIAL_ITEM_FB = -10
        const val SOCIAL_ITEM_TWITTER = -11
        const val SOCIAL_ITEM_INSTAGRAM = -12

        const val PROFILE_ITEM_AVATAR = -13
        const val PROFILE_ITEM_NAME = -14
        const val PROFILE_ITEM_USERNAME = -15
    }

    init {
        commonItemLists.add(PROFILE_ITEM)
        val jsonString = FirebaseRemoteConfigManager.getValue(RemoteConfigKeys.KK_SETTING_LISTS)
        try {
            val jsonObject = JSONObject(jsonString)
            val web_urls = ArrayList<SettingItem>()
            for (item in jsonObject.keys()) {
                if (CommonUtil.textIsNotEmpty(jsonObject.getString(item))) {
                    web_urls.add(SettingItem(title = item, web_url = jsonObject.getString(item)))
                }
            }
            if (web_urls.size > 0) {
                commonItemLists.add(web_urls)
            }

        } catch (err: JSONException) {
            Log.d("Error", err.toString())
        }
        commonItemLists.add(SOCIAL_ITEM)
        commonItemLists.add(LOGIN_LOGOUT_ITEM)
        commonItemLists.add(VERSION_CODE_ITEM)
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            commonItemLists[position] is ArrayList<*> -> {
                WEB_URLS_ITEM
            }
            commonItemLists[position] is Int -> {
                when (commonItemLists[position]) {
                    LOGIN_LOGOUT_ITEM -> LOGIN_LOGOUT_ITEM
                    SOCIAL_ITEM -> SOCIAL_ITEM
                    PROFILE_ITEM -> PROFILE_ITEM
                    else -> VERSION_CODE_ITEM
                }
            }
            else -> {
                VERSION_CODE_ITEM
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = when (viewType) {
            WEB_URLS_ITEM -> inflater.inflate(R.layout.item_settings_recyclerview, parent, false)
            LOGIN_LOGOUT_ITEM -> inflater.inflate(R.layout.item_settings_login_logout, parent, false)
            SOCIAL_ITEM -> inflater.inflate(R.layout.item_settings_social, parent, false)
            PROFILE_ITEM -> inflater.inflate(R.layout.item_settings_profile, parent, false)
            else -> inflater.inflate(R.layout.item_settings_version, parent, false)
        }
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return commonItemLists.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder.itemViewType) {
            WEB_URLS_ITEM -> {
                setWebUrlsView(holder)
            }
            LOGIN_LOGOUT_ITEM -> {
                if (FirebaseAuthUserManager.isUserLoggedIn()) {
                    holder.tvloginLogout.text = context.getString(R.string.logout)
                    listener.onLogout(holder.adapterPosition, holder.containerView)
                } else {
                    holder.tvloginLogout.text = context.getString(R.string.login)
                    listener.onLogin(holder.adapterPosition, holder.containerView)
                }
            }
            VERSION_CODE_ITEM -> {
                val buildVersion = "Version ${BuildConfig.VERSION_NAME}"
                holder.buildVersionTv.text = buildVersion
            }
            SOCIAL_ITEM -> {
                holder.ivFacebook.setOnClickListener {
                    listener.onSocial(SOCIAL_ITEM_FB, holder.adapterPosition, holder.containerView)
                }

                holder.ivTwitter.setOnClickListener {
                    listener.onSocial(SOCIAL_ITEM_TWITTER, holder.adapterPosition, holder.containerView)
                }

                holder.ivInstagram.setOnClickListener {
                    listener.onSocial(SOCIAL_ITEM_INSTAGRAM, holder.adapterPosition, holder.containerView)
                }
            }
            PROFILE_ITEM -> {
                setProfileData(holder)
            }
        }
    }

    private fun setProfileData(holder: ViewHolder) {
        val profile = SharedPreferenceManager.getUser()
        if (CommonUtil.textIsNotEmpty(profile?.originalAvatar)) {
            ImageManager.loadImage(holder.ivContactImage, profile?.originalAvatar)
            holder.ivContactImage.visibility = View.VISIBLE
        } else {
            holder.tvContactPlaceholder.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimensionPixelSize(R.dimen._47ssp).toFloat())
            holder.tvContactPlaceholder.text = profile?.getInitialsName()
        }
        holder.tvFullname.text = profile?.getFullName()
        holder.tvUsername.text = profile?.getUserName()
        holder.tvPhone.text = profile?.phone

        holder.tvFullname.setOnClickListener {
            listener.onProfileItem(PROFILE_ITEM_NAME, holder.adapterPosition, holder.containerView)
        }
        holder.tvUsername.setOnClickListener {
            listener.onProfileItem(PROFILE_ITEM_USERNAME, holder.adapterPosition, holder.containerView)
        }
        holder.userImageIv1.setOnClickListener {
            listener.onProfileItem(PROFILE_ITEM_AVATAR, holder.adapterPosition, holder.containerView)
        }
    }

    private fun setWebUrlsView(holder: ViewHolder) {
        val items = commonItemLists[holder.adapterPosition] as ArrayList<SettingItem>?
        if (items != null) {
            val adapter = SettingWebUrlAdapter(context = context, items = items) { url, position, view ->
                listener.onWebUrlClicked(url, position, view)
            }
            adapter.setHasStableIds(true)
            holder.rcvAll?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            holder.rcvAll?.adapter = adapter
            holder.rcvAll?.setHasFixedSize(true)
        }
    }

    fun notifyProfileChange() {
        for (i in commonItemLists.indices) {
            if (commonItemLists[i] == PROFILE_ITEM) {
                notifyItemChanged(i)
                break
            }
        }
    }

    fun updateItem(items: SettingItem) {
        for (i in commonItemLists.indices) {
            if (commonItemLists[i] is SettingItem && items.type == (commonItemLists[i] as SettingItem).type) {
                commonItemLists[i] = items
                notifyItemChanged(i)
                break
            }
        }
    }

    private fun getIntent(finalMessage: String, packageName: String?): Intent {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        if (!packageName.isNullOrEmpty())
            intent.setPackage(packageName)
        intent.putExtra(Intent.EXTRA_TEXT, finalMessage)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return intent
    }

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer

    inner class SettingWebUrlAdapter(val context: Context,
                                     var items: ArrayList<SettingItem>,
                                     val listener: (String, Int, View?) -> Unit) : RecyclerView.Adapter<SettingWebUrlAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_settings_urls, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[holder.adapterPosition]
            holder.tvWebTitle.text = item.title

            holder.containerView.setOnClickListener {
                listener(item.web_url!!, position, holder.containerView)
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }

        inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer
    }

    interface SettingListener {
        fun onWebUrlClicked(url: String, position: Int, view: View?)
        fun onLogout(position: Int, view: View?)
        fun onLogin(position: Int, view: View?)
        fun onSocial(type: Int, position: Int, view: View?)
        fun onProfileItem(type: Int, position: Int, view: View?)
    }
}
