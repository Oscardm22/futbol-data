package com.example.futboldata.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.futboldata.ui.equipos.fragments.PartidosFragment
import com.example.futboldata.ui.equipos.fragments.JugadoresFragment
import com.example.futboldata.ui.equipos.fragments.StatsFragment

class TeamDetailPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> StatsFragment()
            1 -> PartidosFragment()
            2 -> JugadoresFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
}