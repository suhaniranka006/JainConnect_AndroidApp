package com.mycompany.jainconnect.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mycompany.jainconnect.ui.fragments.SavedListFragment

class SavedItemsAdapter(fragmentActivity: FragmentActivity, private val categories: List<String>) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = categories.size

    override fun createFragment(position: Int): Fragment {
        return SavedListFragment.newInstance(categories[position])
    }
}
