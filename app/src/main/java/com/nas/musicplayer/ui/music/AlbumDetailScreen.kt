package com.nas.musicplayer.ui.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nas.musicplayer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    album: Album,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit = {}
) {
    val appleRed = Color(0xFFFA2D48)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = appleRed)
                    }
                },
                actions = {
                    IconButton(onClick = { /* 옵션 */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = appleRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // 1. 중앙 앨범 아트 및 기본 정보
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .size(260.dp)
                            .shadow(20.dp, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        AsyncImage(
                            model = album.imageUrl ?: R.drawable.ic_launcher_background,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(28.dp))
                    
                    Text(
                        text = album.name,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    
                    Text(
                        text = album.artist,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = appleRed,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Text(
                        text = "앨범 · ${if(album.year > 0) "${album.year}년" else "정보 없음"}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // 2. 재생 & 셔플 버튼
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { if (album.songs.isNotEmpty()) onSongClick(album.songs.first()) },
                        modifier = Modifier.weight(1f).height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = null
                    ) {
                        Icon(Icons.Default.PlayArrow, null, tint = appleRed, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("재생", color = appleRed, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                    
                    Button(
                        onClick = { if (album.songs.isNotEmpty()) onSongClick(album.songs.random()) },
                        modifier = Modifier.weight(1f).height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = null
                    ) {
                        Icon(Icons.Default.Shuffle, null, tint = appleRed, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("셔플", color = appleRed, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                }
            }

            // 3. 트랙 리스트
            if (album.songs.isEmpty()) {
                item {
                    Text(
                        "수록곡 정보를 불러올 수 없습니다.",
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            } else {
                itemsIndexed(album.songs) { index, song ->
                    AlbumTrackRow(
                        index = index + 1,
                        song = song,
                        onClick = { onSongClick(song) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp),
                        thickness = 0.5.dp,
                        color = Color.LightGray.copy(alpha = 0.2f)
                    )
                }
            }
            
            // 4. 하단 추천 앨범 그리드 (그리드 리스트 형태 추가)
            item {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    "아티스트의 다른 앨범",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                
                // 가로 스크롤 또는 그리드 모방 (LazyColumn 내부이므로 Row 조합)
                // 여기서는 2열 그리드 형태로 4개 정도 예시를 보여줍니다.
                val dummyAlbums = List(4) { album.copy(name = "관련 앨범 ${it + 1}") }
                
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    for (i in dummyAlbums.indices step 2) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            AlbumSmallGridItem(dummyAlbums[i], Modifier.weight(1f), onAlbumClick)
                            if (i + 1 < dummyAlbums.size) {
                                AlbumSmallGridItem(dummyAlbums[i+1], Modifier.weight(1f), onAlbumClick)
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumSmallGridItem(album: Album, modifier: Modifier, onClick: (Album) -> Unit) {
    Column(
        modifier = modifier
            .padding(8.dp)
            .clickable { onClick(album) }
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            AsyncImage(
                model = album.imageUrl ?: R.drawable.ic_launcher_background,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(album.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
        Text(album.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
fun AlbumTrackRow(index: Int, song: Song, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = index.toString(), modifier = Modifier.width(32.dp), style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = song.name ?: "제목 없음", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = song.artist, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray), maxLines = 1)
        }
        IconButton(onClick = { /* 옵션 */ }) {
            Icon(Icons.Default.MoreVert, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}
