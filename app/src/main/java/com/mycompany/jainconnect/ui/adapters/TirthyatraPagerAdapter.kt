package com.mycompany.jainconnect.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mycompany.jainconnect.ui.fragments.TirthyatraExploreFragment
import com.mycompany.jainconnect.ui.fragments.TirthyatraMyYatrasFragment
import com.mycompany.jainconnect.ui.fragments.TirthyatraCommunityFragment

class TirthyatraPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TirthyatraExploreFragment()
            1 -> TirthyatraMyYatrasFragment()
            2 -> TirthyatraCommunityFragment()
            else -> TirthyatraExploreFragment()
        }
    }
}
