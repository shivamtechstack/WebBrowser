package com.sycodes.orbital.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.sycodes.orbital.BrowserTabFragment
import com.sycodes.orbital.MainActivity
import com.sycodes.orbital.R
import com.sycodes.orbital.adapters.TabGroupAdapter
import com.sycodes.orbital.databinding.FragmentTabGroupBinding
import com.sycodes.orbital.models.TabDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TabGroupFragment : Fragment() {
    private lateinit var binding: FragmentTabGroupBinding
    private lateinit var tabDatabase: TabDatabase
    private lateinit var backPressedCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                (activity as MainActivity).closeTabGroup()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTabGroupBinding.inflate(inflater,container,false)
        tabDatabase = TabDatabase.getDatabase(requireContext())

        var recyclerView = binding.tabGroupRecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(requireContext(),2)

        binding.tabGroupToolBar.navigationIcon =
            ContextCompat.getDrawable(requireContext(), R.drawable.arrow_smallleft_24)
        binding.tabGroupToolBar.setNavigationOnClickListener {
            (requireActivity() as? MainActivity)?.closeTabGroup()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        CoroutineScope(Dispatchers.IO).launch {
            val tabGroupDao = TabDatabase.getDatabase(requireContext()).tabDataDao()
            val tabs = tabGroupDao.getAllTabs()
            requireActivity().runOnUiThread {
                binding.tabGroupRecyclerView.adapter = TabGroupAdapter(tabs, onTabClickListener = { selectedTab ->
                    CoroutineScope(Dispatchers.IO).launch {
                        tabDatabase.tabDataDao().deactivateAllTabs()
                        tabDatabase.tabDataDao().updateTabData(selectedTab.copy(isActive = true))

                        withContext(Dispatchers.Main) {
                            activity?.let { safeActivity ->
                                (safeActivity as? MainActivity)?.apply {
                                    //loadActiveTab()
                                    switchToTab(selectedTab.id)
                                    closeTabGroup()
                                }
                            }
                        }
                    }
                },
                    onTabCloseListener = { tabToClose ->
                        CoroutineScope(Dispatchers.IO).launch {
                            tabDatabase.tabDataDao().deleteTab(tabToClose.id)
                            val updatedTabs = tabDatabase.tabDataDao().getAllTabs()
                            withContext(Dispatchers.Main) {
                                (binding.tabGroupRecyclerView.adapter as? TabGroupAdapter)?.updateTabs(updatedTabs)
                            }
                        }
                    })
            }
            }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        backPressedCallback.isEnabled = true
    }

    override fun onPause() {
        super.onPause()
        backPressedCallback.isEnabled = false
    }
}