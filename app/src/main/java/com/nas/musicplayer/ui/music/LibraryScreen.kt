package com.nas.musicplayer.ui.music

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onSongClick: (Song, List<Song>) -> Unit,
    onNavigateToAddToPlaylist: (Song) -> Unit
) {
    val context = LocalContext.current
    var localSongs by remember { mutableStateOf(emptyList<Song>()) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // 화면 진입 시 로컬 파일 로드
    LaunchedEffect(Unit) {
        localSongs = LocalMusicManager.getAllAudioFiles(context)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("보관함", fontWeight = FontWeight.ExtraBold) },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = 120.dp // 하단 플레이어 및 탭 바 공간 확보
            ),
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
            if (localSongs.isEmpty()) {
                item {
                    Text(
                        "기기에 저장된 음악이 없습니다.",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Gray
                    )
                }
            } else {
                items(localSongs) { song ->
                    SongListItem(
                        song = song,
                        onItemClick = { onSongClick(song, localSongs) },
                        onMoreClick = { onNavigateToAddToPlaylist(song) }
                    )
                }
            }
        }
    }
}
