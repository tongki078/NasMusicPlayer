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
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
    
    // 검색창 포커스 상태 관리
    var isSearchFocused by remember { mutableStateOf(false) }

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
                isSearchFocused = false
            }
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { 
                    Text("검색", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                },
                actions = {
                    IconButton(onClick = onNavigateToPlaylists) {
                        Icon(Icons.AutoMirrored.Rounded.PlaylistPlay, null, modifier = Modifier.size(32.dp), tint = primaryColor)
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
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                TextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("아티스트, 노래, 앨범 등") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        IconButton(onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
                            }
                            voiceLauncher.launch(intent)
                        }) {
                            Icon(Icons.Rounded.Mic, null, tint = primaryColor)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .onFocusChanged { isSearchFocused = it.isFocused }, // 포커스 감지
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        viewModel.performSearch()
                        focusManager.clearFocus()
                        isSearchFocused = false
                    }),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // 검색 결과 또는 Top100 리스트 (포커스가 없을 때 보여줌)
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 120.dp),
                    modifier = Modifier.fillMaxSize()
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

                // 최근 검색어 (검색창에 포커스가 있고 검색어가 비어 있을 때만 오버레이로 보여줌)
                if (isSearchFocused && uiState.searchQuery.isEmpty() && uiState.recentSearches.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        RecentSearchesView(
                            recentSearches = uiState.recentSearches,
                            onSearchClick = { 
                                viewModel.performSearch(it)
                                focusManager.clearFocus()
                                isSearchFocused = false
                            },
                            onDeleteClick = { viewModel.deleteRecentSearch(it) },
                            onClearAll = { viewModel.clearAllRecentSearches() }
                        )
                    }
                }

                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }

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

@Composable
fun RecentSearchesView(
    recentSearches: List<RecentSearch>,
    onSearchClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onClearAll: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("최근 검색어", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("지우기", color = primaryColor, modifier = Modifier.clickable { onClearAll() })
            }
        }
        items(recentSearches) { search ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onSearchClick(search.query) }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.History, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(search.query, modifier = Modifier.weight(1f), fontSize = 17.sp)
                IconButton(onClick = { onDeleteClick(search.query) }) {
                    Icon(Icons.Default.Close, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }
            HorizontalDivider(modifier = Modifier.padding(start = 52.dp, end = 16.dp), thickness = 0.5.dp)
        }
    }
}

@Composable
fun SongListItem(song: Song, onItemClick: () -> Unit, onMoreClick: () -> Unit) {
    Column(modifier = Modifier.clickable { onItemClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            val imageRequest = remember(song.id) {
                ImageRequest.Builder(context).data(song.metaPoster ?: song.albumArtRes).crossfade(true).build()
            }
            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(song.name ?: "제목 없음", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(song.artist, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, maxLines = 1)
            }
            IconButton(onClick = onMoreClick) {
                Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
            }
        }
        HorizontalDivider(modifier = Modifier.padding(start = 88.dp, end = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
    }
}

@Composable
fun MoreOptionsSheet(song: Song, onNavigateToArtist: () -> Unit, onNavigateToAddToPlaylist: (Song) -> Unit, onNavigateToAlbum: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Column(modifier = Modifier.padding(bottom = 32.dp)) {
        ListItem(
            headlineContent = { Text(song.name ?: "") },
            supportingContent = { Text(song.artist) },
            leadingContent = { AsyncImage(model = song.metaPoster, contentDescription = null, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop) }
        )
        HorizontalDivider()
        ListItem(headlineContent = { Text("플레이리스트에 추가") }, leadingContent = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null, tint = primaryColor) }, modifier = Modifier.clickable { onNavigateToAddToPlaylist(song) })
        ListItem(headlineContent = { Text("아티스트 보기") }, leadingContent = { Icon(Icons.Default.Person, null, tint = primaryColor) }, modifier = Modifier.clickable { onNavigateToArtist() })
        ListItem(headlineContent = { Text("앨범 보기") }, leadingContent = { Icon(Icons.Default.Album, null, tint = primaryColor) }, modifier = Modifier.clickable { onNavigateToAlbum() })
    }
}
