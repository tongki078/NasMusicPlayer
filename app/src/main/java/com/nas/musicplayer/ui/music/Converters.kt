package com.nas.musicplayer.ui.music

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromString(value: String): List<Song> {
        val listType = object : TypeToken<List<Song>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<Song>): String {
        return Gson().toJson(list)
    }
}
