package com.nas.musicplayer.ui.music

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.nas.musicplayer.Song
import com.nas.musicplayer.MusicRepository
import com.nas.musicplayer.db.getDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistScreen(
    song: Song,
    onBack: () -> Unit,
    onNavigateToPlaylists: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember {
        val database = getDatabase(context)
        MusicRepository(database.playlistDao(), database.recentSearchDao())
    }
    
    val playlists by repository.allPlaylists.collectAsState(initial = emptyList())
    var newPlaylistName by remember { mutableStateOf("") }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("플레이리스트에 추가") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ListItem(
                headlineContent = { Text(song.name ?: "Unknown Title") },
                supportingContent = { Text(song.artist) },
                leadingContent = { Icon(Icons.Default.MusicNote, null) }
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("새 플레이리스트 생성") },
                leadingContent = { Icon(Icons.Default.Add, null) },
                modifier = Modifier.clickable { showCreatePlaylistDialog = true }
            )
            LazyColumn {
                items(playlists) { playlist ->
                    ListItem(
                        headlineContent = { Text(playlist.name) },
                        modifier = Modifier.clickable {
                            scope.launch {
                                repository.addSongToPlaylist(playlist.id, song)
                                onNavigateToPlaylists()
                            }
                        }
                    )
                }
            }
        }
    }

    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("새 플레이리스트 생성") },
            text = {
                TextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text("플레이리스트 이름") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newPlaylistName.isNotBlank()) {
                        scope.launch {
                            val id = repository.createPlaylist(newPlaylistName)
                            repository.addSongToPlaylist(id.toInt(), song)
                            showCreatePlaylistDialog = false
                            onNavigateToPlaylists()
                        }
                    }
                }) {
                    Text("생성")
                }
            },
            dismissButton = {
                Button(onClick = { showCreatePlaylistDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}
