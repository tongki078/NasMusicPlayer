package com.nas.musicplayer.ui.music

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nas.musicplayer.MusicPlayerController
import com.nas.musicplayer.MusicRepository
import com.nas.musicplayer.MusicPlayerViewModel
import com.nas.musicplayer.db.getDatabase

class MusicPlayerViewModelFactory(private val context: Context, private val repository: MusicRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicPlayerViewModel::class.java)) {
            val musicPlayerController = MusicPlayerController(context.applicationContext)

            @Suppress("UNCHECKED_CAST")
            return MusicPlayerViewModel(musicPlayerController, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
