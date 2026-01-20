package com.nas.musicplayer.ui.music

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nas.musicplayer.Artist
import com.nas.musicplayer.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artist: Artist,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayAllClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(artist.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onPlayAllClick) {
                        Icon(Icons.Default.Shuffle, contentDescription = "Shuffle")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            item {
                // Artist header
            }
            items(artist.popularSongs) { song ->
                ListItem(
                    headlineContent = { Text(song.name ?: "") },
                    supportingContent = { Text(song.artist) },
                    modifier = Modifier.clickable { onSongClick(song) }
                )
            }
        }
    }
}
