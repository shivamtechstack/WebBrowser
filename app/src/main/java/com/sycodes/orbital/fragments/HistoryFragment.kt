package com.sycodes.orbital.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sycodes.orbital.MainActivity
import com.sycodes.orbital.R
import com.sycodes.orbital.adapters.GroupedHistoryAdapter
import com.sycodes.orbital.models.AppDatabase
import com.sycodes.orbital.models.History
import com.sycodes.orbital.utilities.HistoryItem
import com.sycodes.orbital.utilities.WebPageMetaExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GroupedHistoryAdapter
    private lateinit var appDatabase: AppDatabase
    private lateinit var backPressedCallback : OnBackPressedCallback

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

        return inflater.inflate(R.layout.fragment_history, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.historyRecyclerView)
        appDatabase = AppDatabase.getAppDatabase(requireContext())

        adapter = GroupedHistoryAdapter(mutableListOf(), onHistoryClick = {
            (activity as MainActivity).addNewTab(it.url)
            (activity as MainActivity).closeTabGroup()
        }, onDeleteClick = { history ->
            CoroutineScope(Dispatchers.IO).launch {
                //WebPageMetaExtractor.deleteHistoryFavicon(requireContext(),history.url)
                appDatabase.appDataDao().deleteHistory(history.id)
                loadHistory()
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadHistory()
    }

    private fun loadHistory() {
        CoroutineScope(Dispatchers.IO).launch {
            val allHistory = appDatabase.appDataDao().getAllHistory()
            val groupedItems = groupHistoryItems(allHistory)

            withContext(Dispatchers.Main) {
                adapter.updateList(groupedItems)
            }
        }
    }

    private fun groupHistoryItems(historyList: List<History>): List<HistoryItem> {
        val grouped = LinkedHashMap<String, MutableList<History>>()

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

        for (history in historyList) {
            val date = Instant.ofEpochMilli(history.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            val label = when (date) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> date.format(formatter)
            }

            if (grouped[label] == null) {
                grouped[label] = mutableListOf()
            }
            grouped[label]?.add(history)
        }

        val result = mutableListOf<HistoryItem>()
        for ((label, entries) in grouped) {
            result.add(HistoryItem.DateHeader(label))
            result.addAll(entries.map { HistoryItem.HistoryEntry(it) })
        }
        return result
    }

}