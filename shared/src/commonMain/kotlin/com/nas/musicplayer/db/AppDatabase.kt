package com.nas.musicplayer.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [PlaylistEntity::class, RecentSearch::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
expect abstract class AppDatabase : RoomDatabase {
    abstract fun playlistDao(): PlaylistDao
    abstract fun recentSearchDao(): RecentSearchDao
}
