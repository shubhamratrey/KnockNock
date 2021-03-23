package com.sillylife.knocknock.views.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sillylife.knocknock.views.fragments.BaseFragment
import com.sillylife.knocknock.views.viewmodal.HomeFragmentViewModel
import com.sillylife.knocknock.views.viewmodal.ProfileViewModel

class FragmentViewModelFactory(private val fragment: BaseFragment) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(HomeFragmentViewModel::class.java) -> return HomeFragmentViewModel(fragment) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> return ProfileViewModel(fragment) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}