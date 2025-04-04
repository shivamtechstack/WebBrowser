package com.sycodes.orbital

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sycodes.orbital.adapters.TabPagerAdapter
import com.sycodes.orbital.databinding.ActivityMainBinding
import com.sycodes.orbital.fragments.TabGroupFragment
import com.sycodes.orbital.models.TabData
import com.sycodes.orbital.models.TabDatabase
import com.sycodes.orbital.viewModels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var tabAdapter: TabPagerAdapter
    private lateinit var tabDatabase: TabDatabase
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        tabDatabase = TabDatabase.getDatabase(this)
        tabAdapter = TabPagerAdapter(this)
        binding.viewPager.adapter = tabAdapter
        binding.viewPager.isUserInputEnabled = false

        //loadActiveTab()

        viewModel.allTabs.observe(this) { tabs ->
            if (tabs.isEmpty()) {
                addNewTab()
            } else {
                tabAdapter.updateTabs(tabs)
                val lastActiveTab = tabs.find { it.isActive }
                lastActiveTab?.let {
                    switchToTab(it.id)
                }
            }
        }
    }

    fun loadActiveTab() {
        CoroutineScope(Dispatchers.IO).launch {
            val activeTab = tabDatabase.tabDataDao().getActiveTab()
            withContext(Dispatchers.Main) {
                if (activeTab != null) {
                    tabAdapter.addTab(activeTab)
                } else {
                    addNewTab()
                }
            }
        }
    }

    fun addNewTab(url: String? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            tabDatabase.tabDataDao().deactivateAllTabs()
            val newTab = TabData(url = url ?: "", title = "New Tab", lastVisited = System.currentTimeMillis(), isActive = true)
            val newTabId = tabDatabase.tabDataDao().insertTabData(newTab).toInt()

            withContext(Dispatchers.Main) {
                val newTabData = TabData(url = url ?: "", id = newTabId, isActive = true)
                tabAdapter.addTab(newTabData)
                CoroutineScope(Dispatchers.Main).launch {
                    switchToTab(newTabId)
                }
            }
        }
    }

    fun switchToTab(tabId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val activeTabs = tabDatabase.tabDataDao().getAllTabs()
            withContext(Dispatchers.Main) {
                tabAdapter.updateTabs(activeTabs)
                val newPosition = tabAdapter.getTabPosition(tabId)
                if (newPosition >= 0) {
                    binding.viewPager.setCurrentItem(newPosition, false)
                }
            }
        }
    }

    fun openTabGroup() {
        binding.viewPager.visibility = View.GONE
        binding.mainFragmentContainer.visibility = View.VISIBLE

        supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_container, TabGroupFragment())
            .addToBackStack("TabGroup")
            .commit()
    }
    fun closeTabGroup() {
        supportFragmentManager.popBackStack()
        binding.viewPager.visibility = View.VISIBLE
        binding.mainFragmentContainer.visibility = View.GONE
    }
}