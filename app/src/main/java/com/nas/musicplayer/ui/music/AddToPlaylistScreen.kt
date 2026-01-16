package com.nas.musicplayer.ui.music

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistScreen(
    song: Song,
    onBack: () -> Unit,
    onAddComplete: () -> Unit, // 추가 완료 후 콜백
    viewModel: AddToPlaylistViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showAddedMessage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    val primaryColor = MaterialTheme.colorScheme.primary

    // 상태 변화를 감지하여 화면 이동 처리
    LaunchedEffect(showAddedMessage) {
        if (showAddedMessage) {
            delay(1000) // 1초 동안 메시지 노출
            showAddedMessage = false
            onAddComplete() // 완료 콜back 호출 (MainActivity에서 Screen.SEARCH로 보냄)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기", tint = primaryColor)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !showAddedMessage) {
                                showCreatePlaylistDialog = true
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = primaryColor
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "새 플레이리스트",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = primaryColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            )
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 92.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }

                items(uiState.playlists) { playlist ->
                    SimplePlaylistItem(
                        playlist = playlist,
                        enabled = !showAddedMessage,
                        onClick = {
                            viewModel.addSongToPlaylist(playlist.id, song)
                            successMessage = "'${song.name}' 추가됨"
                            showAddedMessage = true
                        }
                    )
                }
            }
        }

        // --- 세련된 플로팅 토스트 ---
        Box(
            modifier = Modifier
                .align(Alignment.Center) // 화면 중앙 배치
        ) {
            AnimatedVisibility(
                visible = showAddedMessage,
                enter = scaleIn(initialScale = 0.8f) + fadeIn(),
                exit = scaleOut(targetScale = 0.8f) + fadeOut()
            ) {
                AppleStyleAddedToast(message = successMessage)
            }
        }
    }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onCreate = { name ->
                viewModel.createAndAddSong(name, song)
                successMessage = "'${name}' 생성 및 추가됨"
                showAddedMessage = true
                showCreatePlaylistDialog = false
            }
        )
    }
}

@Composable
fun AppleStyleAddedToast(message: String) {
    Surface(
        color = Color(0xFF1C1C1E).copy(alpha = 0.9f), // iOS Midnight Grey
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 12.dp,
        modifier = Modifier.size(150.dp) // 세련된 컴팩트 사이즈
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun SimplePlaylistItem(
    playlist: PlaylistEntity,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val coverImage = playlist.songs.firstOrNull()?.metaPoster

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (coverImage != null) {
                    AsyncImage(
                        model = coverImage,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                )
                Text(
                    text = "노래 ${playlist.songs.size}곡",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = 92.dp, end = 16.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("새 플레이리스트", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("제목") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    cursorColor = primaryColor
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (text.isNotBlank()) onCreate(text) }
            ) {
                Text("생성", color = primaryColor, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = primaryColor)
            }
        }
    )
}