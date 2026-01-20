package com.nas.musicplayer.ui.music

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nas.musicplayer.db.PlaylistEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class PlaylistUiState(
    val playlist: PlaylistEntity? = null
)

class PlaylistViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val playlistId: Int = savedStateHandle.get<Int>("playlistId")!!

    val uiState: StateFlow<PlaylistUiState> = PlaylistManager.playlists
        .map { playlists ->
            val playlist = playlists.find { it.id == playlistId }
            PlaylistUiState(playlist = playlist)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlaylistUiState()
        )
}
