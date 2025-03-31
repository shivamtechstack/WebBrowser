package com.sycodes.orbital.models

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TabData::class], version = 1, exportSchema = false)
abstract class TabDatabase : RoomDatabase(){
    abstract fun tabDataDao(): TabDataDao

    companion object {
        @Volatile
        private var INSTANCE: TabDatabase? = null

        fun getDatabase(context: Context): TabDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TabDatabase::class.java,
                    "tab_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}