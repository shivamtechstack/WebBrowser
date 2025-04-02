package com.sycodes.orbital.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.sycodes.orbital.BrowserTabFragment
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTabGroupBinding.inflate(inflater,container,false)

        var recyclerView = binding.tabGroupRecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(requireContext(),2)
        binding.tabGroupToolBar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.arrow_smallleft_24)
        binding.tabGroupToolBar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }


        CoroutineScope(Dispatchers.IO).launch {
            val tabGroupDao = TabDatabase.getDatabase(requireContext()).tabDataDao()
            val tabs = tabGroupDao.getAllTabs()
            requireActivity().runOnUiThread {
                binding.tabGroupRecyclerView.adapter = TabGroupAdapter(tabs, onTabClickListener = { selectedTab ->
                    CoroutineScope(Dispatchers.IO).launch {
                        TabDatabase.getDatabase(requireActivity()).tabDataDao().deactivateAllTabs()
                        TabDatabase.getDatabase(requireActivity()).tabDataDao().updateTabData(selectedTab.copy(isActive = true))

                        withContext(Dispatchers.Main) {
                            val fragment = BrowserTabFragment.newInstance(selectedTab.url, selectedTab.id)
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.main_Fragment_Container, fragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                },
                    onTabCloseListener = { tabToClose ->
                        CoroutineScope(Dispatchers.IO).launch {
                           TabDatabase.getDatabase(requireActivity()).tabDataDao().deleteTab(tabToClose.id)
                        }
                    })
                }
            }

        return binding.root
    }
}