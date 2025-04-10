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
import com.sycodes.orbital.models.Bookmark
import java.io.File

class BookmarkHistoryAdapter(private var items: List<Bookmark>,
                             private val onBookmarkClickListener: (Bookmark) -> Unit,
                             private val onCloseButtonClickListener: (Bookmark) -> Unit)
    :RecyclerView.Adapter<BookmarkHistoryAdapter.ViewHolderAdapter>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolderAdapter {
        var inflater = LayoutInflater.from(parent.context).inflate(R.layout.bookmarklayout, parent, false)
        return ViewHolderAdapter(inflater)
    }

    override fun onBindViewHolder(
        holder: ViewHolderAdapter,
        position: Int
    ) {
        if (position >= items.size) return

        holder.itemTitle.text = items[position].title
        holder.itemUrl.text = items[position].url

        Glide.with(holder.itemView.context)
            .load(File(items[position].favicon))
            .placeholder(R.drawable.letter_b)
            .error(R.drawable.letter_b)
            .circleCrop()
            .into(holder.itemIcon)

        holder.itemView.setOnClickListener {
            onBookmarkClickListener(items[position])
        }

        holder.itemCloseButton.setOnClickListener {
            onCloseButtonClickListener(items[position])
        }

    }

    override fun getItemCount(): Int {
       return items.size
    }

    class ViewHolderAdapter(view: View) : RecyclerView.ViewHolder(view) {
        var itemIcon = view.findViewById<ImageView>(R.id.bookmarks_icon)!!
        var itemTitle = view.findViewById<TextView>(R.id.bookmarks_title)!!
        var itemUrl = view.findViewById<TextView>(R.id.bookmarks_url)!!
        var itemCloseButton = view.findViewById<ImageView>(R.id.bookmarks_close)!!
    }
}