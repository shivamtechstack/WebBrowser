package com.sycodes.orbital.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sycodes.orbital.R
import com.sycodes.orbital.models.History
import com.sycodes.orbital.utilities.HistoryItem
import java.io.File

class GroupedHistoryAdapter(
    private var items: MutableList<HistoryItem>,
    private val onHistoryClick: (History) -> Unit,
    private val onDeleteClick: (History) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_HISTORY = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HistoryItem.DateHeader -> VIEW_TYPE_HEADER
            is HistoryItem.HistoryEntry -> VIEW_TYPE_HISTORY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_history_date_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.bookmarklayout, parent, false)
            HistoryViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HistoryItem.DateHeader -> (holder as HeaderViewHolder).bind(item)
            is HistoryItem.HistoryEntry -> (holder as HistoryViewHolder).bind(item.history)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<HistoryItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val dateText: TextView = view.findViewById(R.id.header_text)
        fun bind(header: HistoryItem.DateHeader) {
            dateText.text = header.dateLabel
        }
    }

    inner class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.bookmarks_icon)
        private val title: TextView = view.findViewById(R.id.bookmarks_title)
        private val url: TextView = view.findViewById(R.id.bookmarks_url)
        private val delete: ImageView = view.findViewById(R.id.bookmarks_close)

        fun bind(history: History) {

            Glide.with(itemView.context)
                .load(history.favicon)
                .placeholder(R.drawable.letter_h)
                .error(R.drawable.letter_h)
                .circleCrop()
                .into(icon)

            title.text = history.title
            url.text = history.url
            itemView.setOnClickListener { onHistoryClick(history) }
            delete.setOnClickListener { onDeleteClick(history) }
        }
    }
}
