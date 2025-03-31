package com.sycodes.orbital.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sycodes.orbital.R
import com.sycodes.orbital.adapters.TabGroupAdapter.TabViewHolder
import com.sycodes.orbital.models.TabData

class TabGroupAdapter(private val tabs: List<TabData>, private var onTabClickListener : (TabData) -> Unit, private var onTabCloseListener : (TabData) -> Unit): RecyclerView.Adapter<TabViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TabViewHolder {
        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.tabgrouplayout,parent,false)
        return TabViewHolder(inflater)
    }

    override fun onBindViewHolder(
        holder: TabViewHolder,
        position: Int
    ) {
        holder.title.text = tabs[position].title

        holder.itemView.setOnClickListener {
            onTabClickListener(tabs[position])
        }

        holder.closeIcon.setOnClickListener {
            onTabCloseListener(tabs[position])
        }
    }

    override fun getItemCount(): Int {
        return tabs.size
    }

    class TabViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val favIcon = view.findViewById<ImageView>(R.id.tabGroup_favicon_image)
        val title = view.findViewById<TextView>(R.id.tabGroup_title_text)
        val closeIcon = view.findViewById<ImageView>(R.id.tabGroup_close_icon)
        val websitePreview = view.findViewById<ImageView>(R.id.tabGroup_websitePreview)
    }
}