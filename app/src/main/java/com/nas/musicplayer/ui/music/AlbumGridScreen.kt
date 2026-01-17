package com.nas.musicplayer.ui.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nas.musicplayer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumGridScreen(
    title: String,
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit,
    onBack: () -> Unit
) {
    val appleRed = Color(0xFFFA2D48)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = appleRed)
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(albums) { album ->
                AlbumGridItem(album = album, onClick = { onAlbumClick(album) })
            }
        }
    }
}

@Composable
fun AlbumGridItem(album: Album, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        // 정사각형 앨범 아트
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp)
        ) {
            AsyncImage(
                model = album.imageUrl ?: R.drawable.ic_launcher_background,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 앨범 명
        Text(
            text = album.name,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        // 아티스트 명
        Text(
            text = album.artist,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.Gray,
                fontSize = 14.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
