package com.example.futboldata.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class EquipoDetailPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    private val fragments = mutableListOf<Pair<Fragment, String>>()

    fun addFragment(fragment: Fragment, title: String) {
        fragments.add(Pair(fragment, title))
    }

    fun getTitle(position: Int): String = fragments[position].second

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position].first
}