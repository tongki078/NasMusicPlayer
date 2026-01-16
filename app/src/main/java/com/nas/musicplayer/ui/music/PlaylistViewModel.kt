package com.nas.musicplayer.ui.music

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    // PlaylistManager를 통해 DB의 전체 플레이리스트 목록을 관찰합니다.
    val uiState: StateFlow<PlaylistUiState> = PlaylistManager.playlists
        .map { playlists ->
            // 전체 목록에서 현재 ID와 일치하는 플레이리스트를 찾습니다.
            val playlist = playlists.find { it.id == playlistId }
            PlaylistUiState(playlist = playlist)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlaylistUiState()
        )
}
