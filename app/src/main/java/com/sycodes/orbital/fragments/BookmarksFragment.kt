package com.sycodes.orbital.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.sycodes.orbital.adapters.BookmarkHistoryAdapter
import com.sycodes.orbital.databinding.FragmentBookmarksBinding
import com.sycodes.orbital.models.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookmarksFragment : Fragment() {
    private lateinit var binding: FragmentBookmarksBinding
    private lateinit var appDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBookmarksBinding.inflate(inflater, container, false)
        appDatabase = AppDatabase.getAppDatabase(requireContext())

        val recyclerView = binding.bookmarksRecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            val bookmarks = appDatabase.appDataDao().getAllBookmarks()
            requireActivity().runOnUiThread {
                recyclerView.adapter = BookmarkHistoryAdapter(bookmarks,onBookmarkClickListener = {}, onCloseButtonClickListener = {})
            }

        }
        return binding.root
    }

}