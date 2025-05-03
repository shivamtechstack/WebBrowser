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
import com.sycodes.orbital.models.Shortcut
import java.io.File

class ShortcutAdapter(private var items : List<Shortcut>, onShortcutClickListener: (Shortcut) -> Unit) : RecyclerView.Adapter<ShortcutAdapter.ShortcutViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ShortcutViewHolder {
        var inflater = LayoutInflater.from(parent.context).inflate(R.layout.shortcutslayout, parent, false)
        return ShortcutViewHolder(inflater)
    }

    override fun onBindViewHolder(
        holder: ShortcutViewHolder,
        position: Int
    ) {
        var faviconBitmap = loadBitmapFromPath(items[position].favicon)
        holder.itemTitle.text = items[position].title
        holder.itemImage.setImageBitmap(faviconBitmap)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ShortcutViewHolder(view: View) : RecyclerView.ViewHolder(view){
        var itemTitle = view.findViewById<TextView>(R.id.shortcutLayout_textView)!!
        var itemImage = view.findViewById<ImageView>(R.id.shortcutLayout_imageView)!!
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