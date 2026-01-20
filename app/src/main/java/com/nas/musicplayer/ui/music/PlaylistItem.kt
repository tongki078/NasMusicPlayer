package com.nas.musicplayer.ui.music

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nas.musicplayer.db.PlaylistEntity

@Composable
fun PlaylistItem(playlist: PlaylistEntity, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(playlist.name) },
        supportingContent = { Text("${playlist.songs.size} 곡") },
        leadingContent = {
            Icon(Icons.Default.MusicNote, null)
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
