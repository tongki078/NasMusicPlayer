package com.nas.musicplayer.ui.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nas.musicplayer.MusicRepository
import com.nas.musicplayer.PlaylistViewModel

class PlaylistViewModelFactory(
    private val playlistId: Int,
    private val repository: MusicRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaylistViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlaylistViewModel(playlistId, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
