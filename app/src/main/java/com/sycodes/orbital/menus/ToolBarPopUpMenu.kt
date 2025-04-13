package com.sycodes.orbital.menus

import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.sycodes.orbital.MainActivity
import com.sycodes.orbital.R
import com.sycodes.orbital.fragments.BookmarksFragment
import com.sycodes.orbital.fragments.HistoryFragment
import com.sycodes.orbital.models.AppDatabase
import com.sycodes.orbital.models.Shortcut
import com.sycodes.orbital.utilities.WebPageMetaExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ToolBarPopUpMenu {

    fun showPopupMenu(fragment: Fragment, anchorView: View, webView: WebView) {
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
                R.id.toolbar_addToShortcut -> {
                   var shortcutDataBase = AppDatabase.getAppDatabase(context).appDataDao()
                    var shortcut = Shortcut(
                        url = webView.url ?: "",
                        title = webView.title ?: "",
                        favicon = WebPageMetaExtractor.extractFavicon(webView.favicon,context, "shortcut_${webView.title}"),
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        shortcutDataBase.insertShortcut(shortcut)
                    }
                    true
                }
                R.id.toolbar_newTab -> {
                    (fragment.activity as? MainActivity)?.addNewTab()
                    true
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
}