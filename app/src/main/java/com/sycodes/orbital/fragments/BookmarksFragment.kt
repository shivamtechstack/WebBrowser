package com.sycodes.orbital.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.sycodes.orbital.MainActivity
import com.sycodes.orbital.R
import com.sycodes.orbital.adapters.BookmarkHistoryAdapter
import com.sycodes.orbital.databinding.FragmentBookmarksBinding
import com.sycodes.orbital.models.AppDatabase
import com.sycodes.orbital.utilities.WebPageMetaExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookmarksFragment : Fragment() {
    private lateinit var binding: FragmentBookmarksBinding
    private lateinit var appDatabase: AppDatabase
    private lateinit var backPressedCallback: OnBackPressedCallback
    lateinit var adapter: BookmarkHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                (activity as MainActivity).closeTabGroup()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBookmarksBinding.inflate(inflater, container, false)
        appDatabase = AppDatabase.getAppDatabase(requireContext())

        val recyclerView = binding.bookmarksRecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.bookmarksToolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.arrow_smallleft_24)
        binding.bookmarksToolbar.setNavigationOnClickListener {
            (requireActivity() as? MainActivity)?.closeTabGroup()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        CoroutineScope(Dispatchers.IO).launch {
            val bookmarks = appDatabase.appDataDao().getAllBookmarks().toMutableList()

            withContext(Dispatchers.Main) {
                adapter = BookmarkHistoryAdapter(
                    bookmarks,
                    onBookmarkClickListener = {
                        (activity as MainActivity).addNewTab(it.url)
                        (activity as MainActivity).closeTabGroup()
                    },
                    onCloseButtonClickListener = { bookmark ->
                        CoroutineScope(Dispatchers.IO).launch {
                            //WebPageMetaExtractor.deleteBookmarkFavicon(requireContext(),bookmark.url)
                            appDatabase.appDataDao().deleteBookmarkByUrl(bookmark)
                            withContext(Dispatchers.Main) {
                                val index = bookmarks.indexOf(bookmark)
                                if (index != -1) {
                                    bookmarks.removeAt(index)
                                    adapter.notifyItemRemoved(index)
                                }
                            }
                        }
                    }
                )

                recyclerView.adapter = adapter
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