package com.nas.musicplayer.ui.music

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.nas.musicplayer.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicSearchScreen(
    onNavigateToArtist: (Artist) -> Unit,
    onNavigateToAddToPlaylist: (Song) -> Unit,
    onNavigateToAlbum: (Album) -> Unit,
    onSongClick: (Song) -> Unit,
    onNavigateToPlaylists: () -> Unit,
    viewModel: MusicSearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var selectedSongForSheet by remember { mutableStateOf<Song?>(null) }

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            if (!spokenText.isNullOrBlank()) {
                viewModel.onSearchQueryChanged(spokenText)
                viewModel.performSearch()
            }
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = onNavigateToPlaylists) {
                        Icon(
                            Icons.Rounded.PlaylistPlay,
                            contentDescription = "Playlists",
                            modifier = Modifier.size(32.dp),
                            tint = primaryColor
                        )
                    }
                    IconButton(onClick = { /* 프로필 */ }) {
                        Icon(
                            Icons.Rounded.AccountCircle,
                            contentDescription = "Profile",
                            modifier = Modifier.size(32.dp),
                            tint = primaryColor
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 검색바
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                TextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("아티스트, 노래, 앨범 등", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    viewModel.onSearchQueryChanged("")
                                    focusManager.clearFocus()
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                            IconButton(onClick = {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
                                }
                                try { voiceLauncher.launch(intent) } catch (e: Exception) {}
                            }) {
                                Icon(Icons.Rounded.Mic, contentDescription = "Voice", tint = primaryColor)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        viewModel.performSearch()
                        focusManager.clearFocus()
                    }),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            }

            // 검색 결과 리스트
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.songs.isEmpty() && uiState.searchQuery.isNotBlank()) {
                    NoResultsView(query = uiState.searchQuery)
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        items(uiState.songs, key = { it.id }) { song ->
                            SongListItem(
                                song = song,
                                onItemClick = { onSongClick(song) },
                                onMoreClick = {
                                    selectedSongForSheet = song
                                    scope.launch { sheetState.show() }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // 더보기 메뉴 시트 (Bottom Sheet)
    if (selectedSongForSheet != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedSongForSheet = null },
            sheetState = sheetState
        ) {
            MoreOptionsSheet(
                song = selectedSongForSheet!!,
                onNavigateToArtist = {
                    onNavigateToArtist(Artist(selectedSongForSheet!!.artist))
                    scope.launch { sheetState.hide() }.invokeOnCompletion { selectedSongForSheet = null }
                },
                onNavigateToAddToPlaylist = {
                    onNavigateToAddToPlaylist(it)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { selectedSongForSheet = null }
                },
                onNavigateToAlbum = {
                    onNavigateToAlbum(Album(selectedSongForSheet!!.albumName, selectedSongForSheet!!.artist))
                    scope.launch { sheetState.hide() }.invokeOnCompletion { selectedSongForSheet = null }
                }
            )
        }
    }
}

@Composable
fun MoreOptionsSheet(
    song: Song,
    onNavigateToArtist: () -> Unit,
    onNavigateToAddToPlaylist: (Song) -> Unit,
    onNavigateToAlbum: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.padding(bottom = 32.dp)) {
        // 상단 곡 정보
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            val imageRequest = remember(song.id) {
                ImageRequest.Builder(context)
                    .data(song.metaPoster ?: song.albumArtRes)
                    .crossfade(true)
                    .build()
            }

            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(song.name ?: "제목 없음", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(song.artist, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))

        // 메뉴 리스트
        ListItem(
            headlineContent = { Text("플레이리스트에 추가") },
            leadingContent = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null, tint = primaryColor) },
            modifier = Modifier.clickable { onNavigateToAddToPlaylist(song) }
        )
        ListItem(
            headlineContent = { Text("아티스트 보기") },
            leadingContent = { Icon(Icons.Default.Person, contentDescription = null, tint = primaryColor) },
            modifier = Modifier.clickable { onNavigateToArtist() }
        )
        ListItem(
            headlineContent = { Text("앨범 보기") },
            leadingContent = { Icon(Icons.Default.Album, contentDescription = null, tint = primaryColor) },
            modifier = Modifier.clickable { onNavigateToAlbum() }
        )
    }
}

@Composable
fun NoResultsView(query: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "'$query'에 대한 결과가 없습니다.", color = Color.Gray)
    }
}

@Composable
fun SongListItem(song: Song, onItemClick: () -> Unit, onMoreClick: () -> Unit) {
    val context = LocalContext.current

    // 이미지 로딩 최적화
    val imageRequest = remember(song.id) {
        ImageRequest.Builder(context)
            .data(song.metaPoster ?: song.albumArtRes)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .size(200, 200)
            .build()
    }

    Column(modifier = Modifier.clickable { onItemClick() }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 이미지 로딩 전 배경과 아이콘을 모두 제거하여 공간만 확보
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(song.name ?: "제목 없음", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artist, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onMoreClick) {
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.Gray)
            }
        }
        HorizontalDivider(modifier = Modifier.padding(start = 88.dp, end = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
    }
}