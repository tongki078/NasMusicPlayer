package com.nas.musicplayer.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual abstract class AppDatabase : RoomDatabase() {
    actual abstract fun playlistDao(): PlaylistDao
    actual abstract fun recentSearchDao(): RecentSearchDao
}

fun getDatabase(context: Context): AppDatabase {
    return Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "music_database"
    )
    .fallbackToDestructiveMigration()
    .build()
}
