package com.nas.musicplayer.ui.music

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.nas.musicplayer.ui.music.network.RetrofitInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MusicPlayerViewModel(context: Context) : ViewModel() {

    private var exoPlayer: ExoPlayer? = ExoPlayer.Builder(context).build().apply {
        volume = 1.0f
    }

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    val volume = _volume.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<Song>>(emptyList())
    val currentPlaylist = _currentPlaylist.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()

    init {
        exoPlayer?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                _isPlaying.value = isPlayingValue
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.mediaId?.toIntOrNull()?.let { index ->
                    if (index in _currentPlaylist.value.indices) {
                        _currentIndex.value = index
                        _currentSong.value = _currentPlaylist.value[index]
                    }
                }
            }
        })

        viewModelScope.launch {
            while (isActive) {
                _currentPosition.value = exoPlayer?.currentPosition ?: 0L
                _duration.value = exoPlayer?.duration ?: 0L
                delay(500)
            }
        }
    }

    fun playSong(song: Song, playlist: List<Song>) {
        val startIndex = playlist.indexOf(song).coerceAtLeast(0)
        _currentPlaylist.value = playlist
        _currentIndex.value = startIndex
        _currentSong.value = song

        exoPlayer?.let { player ->
            if (player.isPlaying) player.stop()
            player.clearMediaItems()

            playlist.forEachIndexed { index, currentSong ->
                currentSong.streamUrl?.let { streamUrl ->
                    val mediaItem = MediaItem.Builder()
                        .setUri(streamUrl)
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
        val coercedVolume = volumeValue.coerceIn(0f, 1f)
        _volume.value = coercedVolume
        exoPlayer?.volume = coercedVolume
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
        exoPlayer?.release()
        exoPlayer = null
    }
}
