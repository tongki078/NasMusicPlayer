package com.nas.musicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.nas.musicplayer.ui.music.*
import com.nas.musicplayer.ui.theme.NasAppTheme

enum class Screen {
    SEARCH, LIBRARY, PLAYLISTS, ARTIST_DETAIL, NOW_PLAYING, ADD_TO_PLAYLIST, ALBUM_DETAIL, PLAYLIST_DETAIL
}

@UnstableApi
class MainActivity : ComponentActivity() {
    
    private val repository: MusicRepository by lazy {
        val database = AppDatabase.getDatabase(this)
        MusicRepository(database.playlistDao(), database.recentSearchDao())
    }

    private val searchViewModel: MusicSearchViewModel by viewModels {
        MusicSearchViewModelFactory(repository)
    }
    
    private val playerViewModel: MusicPlayerViewModel by viewModels {
        MusicPlayerViewModelFactory(this, repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        volumeControlStream = AudioManager.STREAM_MUSIC
        PlaylistManager.init(this)

        setContent {
            NasAppTheme {
                val uiState by searchViewModel.uiState.collectAsState()
                val currentSong by playerViewModel.currentSong.collectAsState()
                val isPlaying by playerViewModel.isPlaying.collectAsState()

                var currentScreen by remember { mutableStateOf(Screen.SEARCH) }
                var selectedAlbum by remember { mutableStateOf<Album?>(null) }
                var selectedPlaylistId by remember { mutableStateOf<Int?>(null) }
                var songToAddToPlaylist by remember { mutableStateOf<Song?>(null) }
                
                // 권한 요청
                val context = LocalContext.current
                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 
                    Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
                
                var hasPermission by remember {
                    mutableStateOf(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
                }

                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted -> hasPermission = isGranted }

                LaunchedEffect(Unit) {
                    if (!hasPermission) launcher.launch(permission)
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        bottomBar = {
                            if (currentScreen != Screen.NOW_PLAYING) {
                                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                                    // 순서 변경: 검색 -> 보관함
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.SEARCH,
                                        onClick = { currentScreen = Screen.SEARCH },
                                        icon = { Icon(Icons.Default.Search, "Search") },
                                        label = { Text("검색") }
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.LIBRARY,
                                        onClick = { currentScreen = Screen.LIBRARY },
                                        icon = { Icon(Icons.Default.LibraryMusic, "Library") },
                                        label = { Text("보관함") }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            AnimatedContent(targetState = currentScreen, label = "") { targetScreen ->
                                when (targetScreen) {
                                    Screen.SEARCH -> MusicSearchScreen(
                                        onNavigateToArtist = { artist ->
                                            searchViewModel.loadArtistDetails(artist.name)
                                            currentScreen = Screen.ARTIST_DETAIL
                                        },
                                        onNavigateToAddToPlaylist = {
                                            songToAddToPlaylist = it
                                            currentScreen = Screen.ADD_TO_PLAYLIST
                                        },
                                        onNavigateToAlbum = {
                                            selectedAlbum = it
                                            currentScreen = Screen.ALBUM_DETAIL
                                        },
                                        onSongClick = { playerViewModel.playSong(it, uiState.songs) },
                                        onNavigateToPlaylists = { currentScreen = Screen.PLAYLISTS }
                                    )
                                    Screen.LIBRARY -> LibraryScreen(
                                        onSongClick = { song, list -> playerViewModel.playSong(song, list) },
                                        onNavigateToAddToPlaylist = {
                                            songToAddToPlaylist = it
                                            currentScreen = Screen.ADD_TO_PLAYLIST
                                        },
                                        onNavigateToPlaylists = { currentScreen = Screen.PLAYLISTS }
                                    )
                                    Screen.ADD_TO_PLAYLIST -> songToAddToPlaylist?.let {
                                        AddToPlaylistScreen(
                                            song = it,
                                            onBack = { currentScreen = Screen.SEARCH },
                                            onNavigateToPlaylists = { currentScreen = Screen.SEARCH }
                                        )
                                    }
                                    Screen.PLAYLISTS -> PlaylistsListScreen(
                                        onPlaylistClick = {
                                            selectedPlaylistId = it
                                            currentScreen = Screen.PLAYLIST_DETAIL
                                        },
                                        onBack = { currentScreen = Screen.SEARCH }
                                    )
                                    Screen.PLAYLIST_DETAIL -> {
                                        val factory = remember(selectedPlaylistId) {
                                            object : ViewModelProvider.Factory {
                                                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                                                    PlaylistViewModel(SavedStateHandle(mapOf("playlistId" to selectedPlaylistId))) as T
                                            }
                                        }
                                        PlaylistViewScreen(
                                            onBack = { currentScreen = Screen.PLAYLISTS },
                                            onSongClick = { s, list -> playerViewModel.playSong(s, list) },
                                            playerViewModel = playerViewModel,
                                            viewModel = viewModel(factory = factory)
                                        )
                                    }
                                    Screen.ARTIST_DETAIL -> ArtistDetailScreen(
                                        artist = uiState.selectedArtist ?: emptyArtist,
                                        onBack = { currentScreen = Screen.SEARCH },
                                        onSongClick = { song ->
                                            playerViewModel.playSong(song, uiState.selectedArtist?.popularSongs ?: emptyList())
                                            currentScreen = Screen.NOW_PLAYING
                                        },
                                        onPlayAllClick = {
                                            val songs = uiState.selectedArtist?.popularSongs ?: emptyList()
                                            if (songs.isNotEmpty()) {
                                                playerViewModel.playSong(songs.first(), songs)
                                                currentScreen = Screen.NOW_PLAYING
                                            }
                                        }
                                    )
                                    Screen.NOW_PLAYING -> NowPlayingScreen(
                                        viewModel = playerViewModel,
                                        onBack = { currentScreen = Screen.SEARCH }
                                    )
                                    Screen.ALBUM_DETAIL -> AlbumDetailScreen(
                                        album = selectedAlbum ?: emptyAlbum,
                                        onBack = { currentScreen = Screen.SEARCH },
                                        onSongClick = { song ->
                                            playerViewModel.playSong(song, (selectedAlbum ?: emptyAlbum).songs)
                                            currentScreen = Screen.NOW_PLAYING
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (currentScreen != Screen.NOW_PLAYING && currentSong != null) {
                        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 84.dp), contentAlignment = Alignment.BottomCenter) {
                            MiniPlayer(currentSong!!, isPlaying, { playerViewModel.togglePlayPause() }, { playerViewModel.playNext() }, { currentScreen = Screen.NOW_PLAYING })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MiniPlayer(song: Song, isPlaying: Boolean, onTogglePlay: () -> Unit, onNextClick: () -> Unit, onClick: () -> Unit) {
    val primaryColor = Color(0xFFFA2D48)
    Surface(
        modifier = Modifier.fillMaxWidth().height(64.dp).shadow(12.dp, RoundedCornerShape(14.dp)).clip(RoundedCornerShape(14.dp)).clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
        tonalElevation = 8.dp
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = song.metaPoster ?: song.albumArtRes, contentDescription = null, modifier = Modifier.size(44.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = song.name ?: "", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = song.artist, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onTogglePlay) { Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, modifier = Modifier.size(32.dp), tint = primaryColor) }
            IconButton(onClick = onNextClick) { Icon(Icons.Default.SkipNext, null, modifier = Modifier.size(32.dp), tint = primaryColor) }
        }
    }
}
