package com.sycodes.orbital.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabsData")
data class TabData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val title: String,
    val favicon: String,
    val isActive : Boolean = false,
    val isPinned : Boolean = false,
    val lastVisited: Long = System.currentTimeMillis()
)
