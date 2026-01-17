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

        val httpDataSourceFactory = OkHttpDataSource.Factory(RetrofitInstance.okHttpClient)
        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

        exoPlayer = ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .setAudioAttributes(audioAttributes, true)
            .build()
            .apply {
                this.volume = 1.0f // 플레이어 자체 볼륨 최대
            }

        exoPlayer?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                _isPlaying.value = isPlayingValue
                if (isPlayingValue) {
                    try {
                        loudnessEnhancer?.enabled = true
                    } catch (e: Exception) {
                        Log.e("AudioFX", "Error enabling effect", e)
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = exoPlayer?.currentMediaItemIndex ?: 0
                if (index in _currentPlaylist.value.indices) {
                    _currentIndex.value = index
                    _currentSong.value = _currentPlaylist.value[index]
                }
            }

            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                setupLoudnessEnhancer(audioSessionId)
            }
        })

        val id = exoPlayer?.audioSessionId ?: 0
        if (id != 0 && id != C.AUDIO_SESSION_ID_UNSET) {
            setupLoudnessEnhancer(id)
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
                // 기존 400에서 1500으로 높여 소리를 확실하게 키움
                setTargetGain(1500) 
                enabled = true
            }
        } catch (e: Exception) {
            Log.e("MusicPlayerViewModel", "LoudnessEnhancer failed", e)
        }
    }

    fun playSong(song: Song, playlist: List<Song>) {
        if (playlist.isEmpty()) return
        
        val indexInList = playlist.indexOfFirst { it.id == song.id }
        val startIndex = if (indexInList != -1) indexInList else 0
        
        _currentPlaylist.value = playlist
        _currentIndex.value = startIndex
        _currentSong.value = song

        exoPlayer?.let { player ->
            player.stop()
            player.clearMediaItems()

            playlist.forEach { s ->
                s.streamUrl?.let { url ->
                    val mediaItem = MediaItem.Builder()
                        .setUri(url)
                        .setMediaId(s.id.toString())
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
        if (exoPlayer?.isPlaying == true) {
            exoPlayer?.pause()
        } else {
            exoPlayer?.play()
        }
    }

    fun playNext() {
        exoPlayer?.seekToNextMediaItem()
    }

    fun playPrevious() {
        if ((exoPlayer?.currentPosition ?: 0) > 5000) {
            exoPlayer?.seekTo(0)
        } else {
            exoPlayer?.seekToPreviousMediaItem()
        }
    }

    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }

    fun skipToIndex(index: Int) {
        if (index in _currentPlaylist.value.indices) {
            exoPlayer?.seekTo(index, 0L)
            if (exoPlayer?.isPlaying == false) exoPlayer?.play()
        }
    }

    override fun onCleared() {
        super.onCleared()
        loudnessEnhancer?.release()
        exoPlayer?.release()
        exoPlayer = null
    }
}
