package com.nas.musicplayer.ui.music

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nas.musicplayer.ui.music.network.MusicApiService
import com.nas.musicplayer.ui.music.network.RetrofitInstance
import com.nas.musicplayer.ui.music.network.toSongList
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MusicSearchUiState(
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedArtist: Artist? = null,
    val isArtistLoading: Boolean = false,
    val scrollIndex: Int = 0,
    val scrollOffset: Int = 0
)

class MusicSearchViewModel : ViewModel() {

    private val musicApiService: MusicApiService = RetrofitInstance.musicApiService

    private val _uiState = MutableStateFlow(MusicSearchUiState())
    val uiState: StateFlow<MusicSearchUiState> = _uiState.asStateFlow()

    init {
        // 앱 시작 시 TOP 100 목록을 기본으로 불러옵니다.
        loadTop100()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            // 검색어가 비면 TOP 100 목록으로 돌아갑니다.
            loadTop100()
        }
    }

    fun saveScrollPosition(index: Int, offset: Int) {
        _uiState.update { it.copy(scrollIndex = index, scrollOffset = offset) }
    }

    /**
     * TOP 100 음악 목록을 불러옵니다.
     */
    private fun loadTop100() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, searchQuery = "") }
            try {
                val top100Songs = musicApiService.getTop100().toSongList().filter { !it.isDir }
                _uiState.update {
                    it.copy(
                        songs = top100Songs,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("MusicSearchViewModel", "Failed to load TOP 100", e)
                _uiState.update { it.copy(isLoading = false, songs = emptyList()) }
            }
        }
    }

    /**
     * 키워드로 음악을 검색합니다.
     */
    fun performSearch() {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val searchResult = musicApiService.search(query).toSongList().filter { !it.isDir }
                _uiState.update { 
                    it.copy(
                        songs = searchResult,
                        isLoading = false,
                        scrollIndex = 0,
                        scrollOffset = 0
                    )
                }
            } catch (e: Exception) {
                Log.e("MusicSearchViewModel", "Search failed for query: $query", e)
                _uiState.update { it.copy(isLoading = false, songs = emptyList()) }
            }
        }
    }

    fun loadArtistDetails(artistId: Long, fallbackImageUrl: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isArtistLoading = true) }
            
            val artistName = _uiState.value.songs.find { it.id == artistId }?.artist
            
            if (artistName != null) {
                try {
                    val relatedSongs = musicApiService.search(artistName).toSongList().filter { !it.isDir }
                    val artistInfo = Artist(
                        name = artistName,
                        imageUrl = fallbackImageUrl,
                        popularSongs = relatedSongs
                    )
                    _uiState.update { 
                        it.copy(
                            selectedArtist = artistInfo,
                            isArtistLoading = false
                        )
                    }
                } catch (e: Exception) {
                    Log.e("MusicSearchViewModel", "Load artist details failed for: $artistName", e)
                    _uiState.update { it.copy(isArtistLoading = false) }
                }
            } else {
                _uiState.update { it.copy(isArtistLoading = false) }
            }
        }
    }
}
