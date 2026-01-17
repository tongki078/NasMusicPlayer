package com.nas.musicplayer.ui.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onSongClick: (Song, List<Song>) -> Unit,
    onNavigateToAddToPlaylist: (Song) -> Unit,
    onNavigateToPlaylists: () -> Unit,
    onNavigateToArtist: (Artist) -> Unit,
    onNavigateToAlbum: (Album) -> Unit
) {
    val context = LocalContext.current
    var localSongs by remember { mutableStateOf(emptyList<Song>()) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    // 바텀 시트 상태 관리
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var selectedSongForSheet by remember { mutableStateOf<Song?>(null) }

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
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 150.dp),
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            ) {
                // 상단 메뉴 리스트
                item {
                    val menuItems = listOf("플레이리스트", "아티스트", "앨범", "노래")
                    menuItems.forEach { menu ->
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { if(menu == "플레이리스트") onNavigateToPlaylists() }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(menu, fontSize = 18.sp, color = Color(0xFFFA2D48), fontWeight = FontWeight.Medium)
                                Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
                            }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                        }
                    }
                }

                item {
                    Text(
                        "최근 추가된 음악",
                        modifier = Modifier.padding(16.dp, 24.dp, 16.dp, 8.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (localSongs.isEmpty()) {
                    item {
                        Text("기기에 저장된 음악이 없습니다.", modifier = Modifier.padding(16.dp), color = Color.Gray)
                    }
                } else {
                    items(localSongs) { song ->
                        SongListItem(
                            song = song,
                            onItemClick = { onSongClick(song, localSongs) },
                            onMoreClick = {
                                // ★ 수정: 바로 이동하지 않고 바텀 시트를 띄움
                                selectedSongForSheet = song
                                scope.launch { sheetState.show() }
                            }
                        )
                    }
                }
            }

            // ★ 바텀 시트 구현 (MusicSearchScreen과 동일한 디자인)
            if (selectedSongForSheet != null) {
                ModalBottomSheet(
                    onDismissRequest = { selectedSongForSheet = null },
                    sheetState = sheetState
                ) {
                    val currentSong = selectedSongForSheet!!
                    MoreOptionsSheet(
                        song = currentSong,
                        onNavigateToArtist = {
                            onNavigateToArtist(Artist(name = currentSong.artist))
                            scope.launch { sheetState.hide() }.invokeOnCompletion { selectedSongForSheet = null }
                        },
                        onNavigateToAddToPlaylist = {
                            onNavigateToAddToPlaylist(it)
                            scope.launch { sheetState.hide() }.invokeOnCompletion { selectedSongForSheet = null }
                        },
                        onNavigateToAlbum = {
                            onNavigateToAlbum(Album(name = currentSong.albumName, artist = currentSong.artist))
                            scope.launch { sheetState.hide() }.invokeOnCompletion { selectedSongForSheet = null }
                        }
                    )
                }
            }
        }
    }
}
