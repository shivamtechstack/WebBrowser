package com.sycodes.orbital.models

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDataDao {

    @Insert
    suspend fun insertHistory(history: History)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark)

    @Insert
    suspend fun insertDownload(download: Download)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(shortcut: Shortcut)

    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    suspend fun getAllHistory(): List<History>

    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    suspend fun getAllBookmarks(): List<Bookmark>

    @Query("SELECT * FROM bookmarks WHERE url = :url LIMIT 1")
    suspend fun getBookmarkByUrl(url: String): Bookmark?

    @Query("SELECT * FROM downloads ORDER BY timestamp DESC")
    suspend fun getAllDownloads(): List<Download>

    @Query("SELECT * FROM shortcuts ORDER BY timestamp DESC")
    suspend fun getAllShortcuts(): List<Shortcut>

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistory(id: Int)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: Int)

    @Delete
    suspend fun deleteBookmarkByUrl(bookmark: Bookmark)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownload(id: Int)

    @Query("DELETE FROM shortcuts WHERE id = :id")
    suspend fun deleteShortcut(id: Int)

    @Query("DELETE FROM history")
    suspend fun deleteAllHistory()

    @Query("DELETE FROM bookmarks")
    suspend fun deleteAllBookmarks()

    @Query("DELETE FROM downloads")
    suspend fun deleteAllDownloads()

    @Query("DELETE FROM shortcuts")
    suspend fun deleteAllShortcuts()

    @Query("""SELECT * FROM history WHERE timestamp >= :startOfDay ORDER BY timestamp DESC""")
    fun getHistoryForDay(startOfDay: Long): Flow<List<History>>

    @Query("DELETE FROM history WHERE timestamp >= :timeThreshold")
    suspend fun deleteHistorySince(timeThreshold: Long)

}