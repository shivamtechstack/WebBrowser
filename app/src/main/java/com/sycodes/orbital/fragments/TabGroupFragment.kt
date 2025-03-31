package com.sycodes.orbital.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.sycodes.orbital.BrowserTabFragment
import com.sycodes.orbital.R
import com.sycodes.orbital.adapters.TabGroupAdapter
import com.sycodes.orbital.databinding.FragmentTabGroupBinding
import com.sycodes.orbital.models.TabDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        CoroutineScope(Dispatchers.IO).launch {
            val tabGroupDao = TabDatabase.getDatabase(requireContext()).tabDataDao()
            val tabs = tabGroupDao.getAllTabs()
            requireActivity().runOnUiThread {
                binding.tabGroupRecyclerView.adapter = TabGroupAdapter(tabs, onTabClickListener = {
                    val fragment = BrowserTabFragment.newInstance(it.url)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_Fragment_Container,fragment)
                        .commit()
                },
                    onTabCloseListener = {
                        CoroutineScope(Dispatchers.IO).launch {
                            tabGroupDao.deleteTab(it.id)
                        }
                    })
                }
            }

        return binding.root
    }
}