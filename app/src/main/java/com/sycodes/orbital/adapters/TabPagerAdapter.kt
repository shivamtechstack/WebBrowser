package com.sycodes.orbital.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sycodes.orbital.BrowserTabFragment
import com.sycodes.orbital.models.TabData

class TabPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    private val tabList = mutableListOf<TabData>()

    override fun getItemCount(): Int = tabList.size

    override fun createFragment(position: Int): Fragment {
        val tab = tabList[position]
        return BrowserTabFragment.newInstance(tab.url, tab.id)
    }

    fun addTab(tab: TabData) {
        tabList.add(tab)
        notifyDataSetChanged()
    }

    fun removeTab(position: Int) {
        if (position in tabList.indices) {
            tabList.removeAt(position)
            notifyDataSetChanged()
        }
    }

    fun updateTabs(newTabs: List<TabData>) {
        tabList.clear()
        tabList.addAll(newTabs)
        notifyDataSetChanged()
    }

    fun getTabPosition(tabId: Int): Int {
        return tabList.indexOfFirst { it.id == tabId }
    }
}



