package com.sycodes.orbital

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sycodes.orbital.adapters.TabPagerAdapter
import com.sycodes.orbital.databinding.ActivityMainBinding
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
    private var isTabBeingCreated = false

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

        loadInitialTab()
    }

    fun loadInitialTab() {
        CoroutineScope(Dispatchers.IO).launch {
            val tabs = tabDatabase.tabDataDao().getAllTabs()

            if (tabs.isEmpty()) {
                if (!isTabBeingCreated) {
                    isTabBeingCreated = true
                    withContext(Dispatchers.Main) {
                        addNewTab()
                    }
                }
            } else {
                val activeTab = tabs.find { it.isActive }

                withContext(Dispatchers.Main) {
                    tabAdapter.updateTabs(tabs)
                    activeTab?.let {
                        switchToTab(it.id)
                    }
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
                switchToTab(newTabId)

                isTabBeingCreated = false
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

    fun openTabGroup(fragment : androidx.fragment.app.Fragment) {
        binding.viewPager.visibility = View.GONE
        binding.mainFragmentContainer.visibility = View.VISIBLE

        supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_container, fragment)
            .addToBackStack("TabGroup")
            .commit()
    }

    fun closeTabGroup() {
        supportFragmentManager.popBackStack()
        binding.viewPager.visibility = View.VISIBLE
        binding.mainFragmentContainer.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            val tabs = tabDatabase.tabDataDao().getAllTabs()
            withContext(Dispatchers.Main) {
                if (tabs.isEmpty()) {
                    addNewTab()
                } else {
                    tabAdapter.updateTabs(tabs)
                    val activeTab = tabs.find { it.isActive }
                    activeTab?.let { switchToTab(it.id) }
                }
            }
        }
    }

}