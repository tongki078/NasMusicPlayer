package com.nas.musicplayer.ui.music

/**
 * 플레이리스트를 표현하기 위한 데이터 클래스
 */
data class Playlist(
    val id: Int,
    val name: String,
    val songs: List<Song> = emptyList()
)
