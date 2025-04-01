package com.sycodes.orbital.models

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TabDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTabData(tabData: TabData): Long

    @Update
    suspend fun updateTabData(tabData: TabData)

    @Query("SELECT * FROM tabsData")
    suspend fun getAllTabs(): List<TabData>

    @Query("SELECT * FROM tabsData WHERE isActive = 1")
    suspend fun getActiveTab(): TabData?

    @Query("SELECT * FROM tabsData WHERE isPinned = 1")
    suspend fun getPinnedTabs(): List<TabData>

    @Query("DELETE FROM tabsData WHERE id = :tabId")
    suspend fun deleteTab(tabId: Int)

    @Query("DELETE FROM tabsData")
    suspend fun deleteAllTabs()

    @Query("SELECT * FROM tabsData WHERE id = :id")
    suspend fun getTab(id: Int): TabData?

    @Query("UPDATE tabsData SET isActive = 0")
    suspend fun deactivateAllTabs()
}