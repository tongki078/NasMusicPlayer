package com.nas.musicplayer

data class Artist(
    val id: Long = 0L,
    val name: String,
    val profileImageRes: Int? = null,
    val imageUrl: String? = null,
    val followers: String = "0",
    val popularSongs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList() // 아티스트의 앨범 목록 추가
)
