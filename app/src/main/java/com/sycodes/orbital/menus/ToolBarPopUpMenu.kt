package com.sycodes.orbital.menus

import android.content.Context
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import com.sycodes.orbital.MainActivity
import com.sycodes.orbital.R
import com.sycodes.orbital.fragments.BookmarksFragment
import com.sycodes.orbital.fragments.HistoryFragment

object ToolBarPopUpMenu {

    fun showPopupMenu(fragment: Fragment, anchorView: View) {
        val context = fragment.requireContext()
        val popupMenu = PopupMenu(context, anchorView)
        popupMenu.menuInflater.inflate(R.menu.toolbarmenu, popupMenu.menu)

        popupMenu.setForceShowIcon(true)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.toolbar_bookmarks -> {
                    (fragment.activity as? MainActivity)?.openTabGroup(BookmarksFragment())
                    true
                }
                R.id.toolbar_history -> {
                    (fragment.activity as? MainActivity)?.openTabGroup(HistoryFragment())
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
}