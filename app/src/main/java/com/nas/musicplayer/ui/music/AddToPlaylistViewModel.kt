package com.nas.musicplayer.ui.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nas.musicplayer.Song
import com.nas.musicplayer.db.PlaylistEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AddToPlaylistUiState(
    val playlists: List<PlaylistEntity> = emptyList()
)

class AddToPlaylistViewModel : ViewModel() {

    private val playlistManager = PlaylistManager

    val uiState: StateFlow<AddToPlaylistUiState> = playlistManager.playlists
        .map { AddToPlaylistUiState(playlists = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AddToPlaylistUiState()
        )

    fun createAndAddSong(name: String, song: Song) {
        viewModelScope.launch {
            val newId = playlistManager.createPlaylist(name)
            if(newId != 0L) {
                playlistManager.addSongToPlaylist(newId.toInt(), song)
            }
        }
    }

    fun addSongToPlaylist(playlistId: Int, song: Song) {
        viewModelScope.launch {
            playlistManager.addSongToPlaylist(playlistId, song)
        }
    }
}
