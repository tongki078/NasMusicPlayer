package com.nas.musicplayer.ui.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nas.musicplayer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artist: Artist,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayAllClick: () -> Unit
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // 1. 아티스트 헤더 (대형 이미지)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    AsyncImage(
                        model = artist.imageUrl ?: R.drawable.ic_launcher_background,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // 하단 텍스트 가독성을 위한 그래디언트
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                    startY = 400f
                                )
                            )
                    )
                    Text(
                        text = artist.name,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = (-1).sp
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 20.dp, bottom = 24.dp)
                    )
                }
            }

            // 2. 재생 및 셔플 버튼 섹션
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onPlayAllClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = null
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = appleRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("재생", color = appleRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Button(
                        onClick = onPlayAllClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = null
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = null, tint = appleRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("셔플", color = appleRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            // 3. 인기 곡 섹션 타이틀
            item {
                Text(
                    "인기 곡",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
            }

            // 4. 곡 리스트
            itemsIndexed(artist.popularSongs) { index, song ->
                ArtistSongItem(
                    index = index + 1,
                    song = song,
                    onClick = { onSongClick(song) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 64.dp, end = 20.dp),
                    thickness = 0.5.dp,
                    color = Color.LightGray.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
fun ArtistSongItem(
    index: Int,
    song: Song,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = index.toString(),
            modifier = Modifier.width(24.dp),
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
        )
        
        AsyncImage(
            model = song.metaPoster ?: R.drawable.ic_launcher_background,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.name ?: "제목 없음",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "인기 곡", // 또는 앨범 명
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
            )
        }
        
        IconButton(onClick = { /* 옵션 */ }) {
            Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}
