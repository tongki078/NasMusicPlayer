package com.nas.musicplayer.ui.music

import android.content.Context
import android.media.audiofx.LoudnessEnhancer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.nas.musicplayer.ui.music.network.RetrofitInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MusicPlayerViewModel(
    private val repository: MusicRepository,
    context: Context
) : ViewModel() {

    private var exoPlayer: ExoPlayer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<Song>>(emptyList())
    val currentPlaylist: StateFlow<List<Song>> = _currentPlaylist.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    val playlistItems: StateFlow<List<PlaylistEntity>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        // ★ Retrofit과 동일한 OkHttpClient를 사용하여 API 키 인증을 공유합니다.
        val httpDataSourceFactory = OkHttpDataSource.Factory(RetrofitInstance.okHttpClient)
        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

        exoPlayer = ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .setAudioAttributes(audioAttributes, true)
            .build()
            .apply {
                this.volume = 1.0f
            }

        exoPlayer?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                _isPlaying.value = isPlayingValue
                if (isPlayingValue) loudnessEnhancer?.enabled = true
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.mediaId?.toIntOrNull()?.let { index ->
                    if (index in _currentPlaylist.value.indices) {
                        _currentIndex.value = index
                        _currentSong.value = _currentPlaylist.value[index]
                    }
                }
            }

            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                setupLoudnessEnhancer(audioSessionId)
            }
        })

        val initialSessionId = exoPlayer?.audioSessionId ?: 0
        if (initialSessionId != 0 && initialSessionId != C.AUDIO_SESSION_ID_UNSET) {
            setupLoudnessEnhancer(initialSessionId)
        }

        viewModelScope.launch {
            while (true) {
                _currentPosition.value = exoPlayer?.currentPosition ?: 0L
                _duration.value = exoPlayer?.duration ?: 0L
                delay(500)
            }
        }
    }

    private fun setupLoudnessEnhancer(sessionId: Int) {
        try {
            loudnessEnhancer?.release()
            loudnessEnhancer = LoudnessEnhancer(sessionId).apply {
                setTargetGain(3000)
                enabled = true
            }
        } catch (e: Exception) {
            Log.e("MusicPlayerViewModel", "Failed to set up LoudnessEnhancer", e)
        }
    }

    fun playSong(song: Song, playlist: List<Song>) {
        val startIndex = playlist.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        
        _currentPlaylist.value = playlist
        _currentIndex.value = startIndex
        _currentSong.value = song

        exoPlayer?.let { player ->
            player.stop()
            player.clearMediaItems()

            playlist.forEachIndexed { index, item ->
                item.streamUrl?.let { url ->
                    val mediaItem = MediaItem.Builder()
                        .setUri(url)
                        .setMediaId(index.toString())
                        .build()
                    player.addMediaItem(mediaItem)
                }
            }
            player.prepare()
            player.seekTo(startIndex, 0L)
            player.play()
        }
    }

    fun setVolume(volumeValue: Float) {
        val level = volumeValue.coerceIn(0f, 1f)
        _volume.value = level
        exoPlayer?.volume = level
    }

    fun togglePlayPause() {
        exoPlayer?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun playNext() {
        exoPlayer?.seekToNextMediaItem()
    }

    fun playPrevious() {
        exoPlayer?.seekToPreviousMediaItem()
    }

    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }

    fun skipToIndex(index: Int) {
        if (index in _currentPlaylist.value.indices) {
            exoPlayer?.seekTo(index, 0L)
            exoPlayer?.play()
        }
    }

    override fun onCleared() {
        super.onCleared()
        loudnessEnhancer?.release()
        exoPlayer?.release()
        exoPlayer = null
    }
}
