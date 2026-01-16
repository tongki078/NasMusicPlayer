package com.nas.musicplayer.ui.music

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val musicApiService = RetrofitInstance.musicApiService

    private val _uiState = MutableStateFlow(MusicSearchUiState())
    val uiState: StateFlow<MusicSearchUiState> = _uiState.asStateFlow()

    init {
        performSearch("아이유")
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun saveScrollPosition(index: Int, offset: Int) {
        _uiState.update { it.copy(scrollIndex = index, scrollOffset = offset) }
    }

    fun performSearch(query: String = _uiState.value.searchQuery) {
        if (query.isBlank()) {
            _uiState.update { it.copy(songs = emptyList()) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val allItems = musicApiService.search(query).toSongList()
                val playableSongs = allItems.filter { !it.isDir }
                
                _uiState.update { 
                    it.copy(
                        songs = playableSongs,
                        isLoading = false,
                        scrollIndex = 0,
                        scrollOffset = 0
                    )
                }
            } catch (e: Exception) {
                // ★ 네트워크 에러 발생 시 Logcat에 에러 메시지 출력
                Log.e("MusicSearchViewModel", "Search failed", e)
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
                    Log.e("MusicSearchViewModel", "Load artist details failed", e)
                    _uiState.update { it.copy(isArtistLoading = false) }
                }
            } else {
                _uiState.update { it.copy(isArtistLoading = false) }
            }
        }
    }
}
