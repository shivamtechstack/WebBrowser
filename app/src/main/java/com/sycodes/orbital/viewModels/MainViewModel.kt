package com.sycodes.orbital.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.sycodes.orbital.models.TabData
import com.sycodes.orbital.models.TabDatabase

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val tabDatabase = TabDatabase.getDatabase(application)
    val allTabs: LiveData<List<TabData>> = tabDatabase.tabDataDao().getAllTabsLive()
}
