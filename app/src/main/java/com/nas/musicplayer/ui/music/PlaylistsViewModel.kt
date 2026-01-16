package com.nas.musicplayer.ui.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaylistsViewModel : ViewModel() {
    private val playlistManager = PlaylistManager
    val playlists: StateFlow<List<PlaylistEntity>> = playlistManager.playlists
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deletePlaylist(id: Int) {
        viewModelScope.launch {
            playlistManager.deletePlaylist(id)
        }
    }
}
