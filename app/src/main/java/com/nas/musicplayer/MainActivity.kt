package com.nas.musicplayer

import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.nas.musicplayer.ui.music.*
import com.nas.musicplayer.ui.theme.NasAppTheme

enum class Screen {
    SEARCH,
    ARTIST_DETAIL,
    NOW_PLAYING,
    ADD_TO_PLAYLIST,
    ALBUM_DETAIL,
    PLAYLISTS,
    PLAYLIST_DETAIL
}

@UnstableApi
class MainActivity : ComponentActivity() {
    private val searchViewModel: MusicSearchViewModel by viewModels()

    private val playerViewModel: MusicPlayerViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        val repository = MusicRepository(database.playlistDao())
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

                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                if (targetState == Screen.NOW_PLAYING || targetState == Screen.ADD_TO_PLAYLIST ||
                                    initialState == Screen.NOW_PLAYING || initialState == Screen.ADD_TO_PLAYLIST) {
                                    slideInVertically { it } + fadeIn() togetherWith slideOutVertically { it } + fadeOut()
                                } else {
                                    fadeIn() togetherWith fadeOut()
                                }
                            },
                            label = "ScreenTransition"
                        ) { targetScreen ->
                            when (targetScreen) {
                                Screen.SEARCH -> {
                                    MusicSearchScreen(
                                        viewModel = searchViewModel,
                                        onNavigateToArtist = { artist ->
                                            searchViewModel.loadArtistDetails(artist.name.hashCode().toLong(), artist.imageUrl)
                                            currentScreen = Screen.ARTIST_DETAIL
                                        },
                                        onNavigateToAddToPlaylist = { song ->
                                            songToAddToPlaylist = song
                                            currentScreen = Screen.ADD_TO_PLAYLIST
                                        },
                                        onNavigateToAlbum = {
                                            selectedAlbum = it
                                            currentScreen = Screen.ALBUM_DETAIL
                                        },
                                        onSongClick = { song ->
                                            playerViewModel.playSong(song, uiState.songs)
                                        },
                                        onNavigateToPlaylists = { currentScreen = Screen.PLAYLISTS }
                                    )
                                }
                                Screen.ARTIST_DETAIL -> {
                                    ArtistDetailScreen(
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
                                }
                                Screen.NOW_PLAYING -> {
                                    NowPlayingScreen(
                                        viewModel = playerViewModel,
                                        onBack = { currentScreen = Screen.SEARCH }
                                    )
                                }
                                Screen.ADD_TO_PLAYLIST -> {
                                    songToAddToPlaylist?.let {
                                        AddToPlaylistScreen(
                                            song = it,
                                            onBack = { currentScreen = Screen.SEARCH },
                                            onNavigateToPlaylists = {
                                                currentScreen = Screen.PLAYLISTS
                                            }
                                        )
                                    }
                                }
                                Screen.ALBUM_DETAIL -> {
                                    AlbumDetailScreen(
                                        album = selectedAlbum ?: emptyAlbum,
                                        onBack = { currentScreen = Screen.SEARCH },
                                        onSongClick = { song ->
                                            playerViewModel.playSong(song, (selectedAlbum ?: emptyAlbum).songs)
                                            currentScreen = Screen.NOW_PLAYING
                                        }
                                    )
                                }
                                Screen.PLAYLISTS -> {
                                    PlaylistsListScreen(
                                        onPlaylistClick = { playlistId ->
                                            selectedPlaylistId = playlistId
                                            currentScreen = Screen.PLAYLIST_DETAIL
                                        },
                                        onBack = { currentScreen = Screen.SEARCH },
                                        // ★★★ 에러 해결: 불필요한 factory 제거 ★★★
                                        viewModel = viewModel()
                                    )
                                }
                                Screen.PLAYLIST_DETAIL -> {
                                    val factory = remember(selectedPlaylistId) {
                                        object : ViewModelProvider.Factory {
                                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                                @Suppress("UNCHECKED_CAST")
                                                return PlaylistViewModel(SavedStateHandle(mapOf("playlistId" to selectedPlaylistId))) as T
                                            }
                                        }
                                    }
                                    val playlistViewModel: PlaylistViewModel = viewModel(factory = factory)

                                    PlaylistViewScreen(
                                        onBack = { currentScreen = Screen.PLAYLISTS },
                                        onSongClick = { song, list ->
                                            playerViewModel.playSong(song, list)
                                        },
                                        playerViewModel = playerViewModel,
                                        viewModel = playlistViewModel
                                    )
                                }
                            }
                        }
                    }

                    // 미니 플레이어
                    if (currentScreen != Screen.NOW_PLAYING && currentSong != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp, vertical = 24.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            MiniPlayer(
                                song = currentSong!!,
                                isPlaying = isPlaying,
                                onTogglePlay = { playerViewModel.togglePlayPause() },
                                onNextClick = { playerViewModel.playNext() },
                                onClick = { currentScreen = Screen.NOW_PLAYING }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MiniPlayer(
    song: Song,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onNextClick: () -> Unit,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(12.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.metaPoster ?: song.albumArtRes,
                contentDescription = null,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.name ?: "제목 없음",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onTogglePlay) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = primaryColor
                )
            }

            IconButton(onClick = onNextClick) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = primaryColor
                )
            }
        }
    }
}