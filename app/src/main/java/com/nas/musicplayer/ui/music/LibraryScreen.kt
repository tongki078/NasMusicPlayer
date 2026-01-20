package com.nas.musicplayer.ui.music

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nas.musicplayer.Album
import com.nas.musicplayer.Artist
import com.nas.musicplayer.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onSongClick: (Song, List<Song>) -> Unit,
    onNavigateToAddToPlaylist: (Song) -> Unit,
    onNavigateToPlaylists: () -> Unit,
    onNavigateToArtist: (Artist) -> Unit,
    onNavigateToAlbum: (Album) -> Unit
) {
    // 라이브러리 UI 로직 (생략된 경우 기존 로직 유지)
    Scaffold(
        topBar = { TopAppBar(title = { Text("보관함") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // 여기에 보관함 목록 표시 로직이 들어갑니다.
            Text("보관함 화면 (구현 중)", modifier = Modifier.padding(16.dp))
        }
    }
}
