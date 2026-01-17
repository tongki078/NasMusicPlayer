package com.nas.musicplayer.ui.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MusicSearchViewModelFactory(
    private val repository: MusicRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicSearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MusicSearchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
