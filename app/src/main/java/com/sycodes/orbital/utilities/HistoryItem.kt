package com.sycodes.orbital.utilities

import com.sycodes.orbital.models.History

sealed class HistoryItem {
    data class DateHeader(val dateLabel: String) : HistoryItem()
    data class HistoryEntry(val history: History) : HistoryItem()
}
