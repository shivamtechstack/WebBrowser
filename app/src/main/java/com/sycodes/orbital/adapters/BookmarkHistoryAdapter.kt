package com.sycodes.orbital.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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
        holder.itemTitle.text = items[position].title
        holder.itemUrl.text = items[position].url

        val faviconBitmap = loadBitmapFromPath(items[position].favicon)
        holder.itemIcon.setImageBitmap(faviconBitmap)

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

    fun loadBitmapFromPath(filePath: String?): Bitmap? {
        if (filePath.isNullOrEmpty()) return null
        val file = File(filePath)
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }
}