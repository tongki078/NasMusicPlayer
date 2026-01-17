package com.nas.musicplayer.ui.music

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nas.musicplayer.ui.music.network.RetrofitInstance
import com.nas.musicplayer.ui.music.network.toSongList
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// UI 상태 구조
data class MusicSearchUiState(
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedArtist: Artist? = null,
    val isArtistLoading: Boolean = false
)

class MusicSearchViewModel : ViewModel() {

    private val musicApiService = RetrofitInstance.musicApiService

    private val _uiState = MutableStateFlow(MusicSearchUiState())
    val uiState: StateFlow<MusicSearchUiState> = _uiState.asStateFlow()

    init {
        loadTop100()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            loadTop100()
        }
    }

    private fun loadTop100() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val top100Songs = musicApiService.getTop100().toSongList().filter { !it.isDir }
                _uiState.update { it.copy(songs = top100Songs, isLoading = false) }
            } catch (e: Exception) {
                Log.e("MusicSearchViewModel", "Top100 Load Failed", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun performSearch() {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val searchResult = musicApiService.search(query).toSongList().filter { !it.isDir }
                _uiState.update { it.copy(songs = searchResult, isLoading = false) }
            } catch (e: Exception) {
                Log.e("MusicSearchViewModel", "Search Failed", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // 아티스트 이름을 기반으로 정보를 가져오는 함수
    fun loadArtistDetails(artistName: String, fallbackImageUrl: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isArtistLoading = true) }
            try {
                // 아티스트 이름으로 검색하여 관련 곡들을 가져옴
                val relatedSongs = musicApiService.search(artistName).toSongList().filter { !it.isDir }
                val artistInfo = Artist(
                    name = artistName,
                    imageUrl = fallbackImageUrl,
                    popularSongs = relatedSongs
                )
                _uiState.update {
                    it.copy(selectedArtist = artistInfo, isArtistLoading = false)
                }
            } catch (e: Exception) {
                Log.e("MusicSearchViewModel", "Artist Load Failed", e)
                _uiState.update { it.copy(isArtistLoading = false) }
            }
        }
    }
}