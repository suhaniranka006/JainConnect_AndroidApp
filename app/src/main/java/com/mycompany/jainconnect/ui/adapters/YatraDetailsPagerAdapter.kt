package com.mycompany.jainconnect.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mycompany.jainconnect.data.models.Tirthyatra
import com.mycompany.jainconnect.ui.fragments.YatraChecklistFragment
import com.mycompany.jainconnect.ui.fragments.YatraItineraryFragment
import com.mycompany.jainconnect.ui.fragments.YatraMembersFragment

class YatraDetailsPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val tirthyatra: Tirthyatra
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> YatraItineraryFragment.newInstance(tirthyatra)
            1 -> YatraChecklistFragment.newInstance(tirthyatra)
            2 -> YatraMembersFragment.newInstance(tirthyatra)
            else -> YatraItineraryFragment.newInstance(tirthyatra)
        }
    }
}
