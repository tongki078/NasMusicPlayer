package com.nas.musicplayer.ui.music

import com.nas.musicplayer.Album
import com.nas.musicplayer.Artist
import com.nas.musicplayer.Song

val emptySong = Song(id = 0L, name = "노래 정보 없음")
val emptyArtist = Artist(name = "알 수 없는 아티스트")
val emptyAlbum = Album("알 수 없는 앨범", "알 수 없는 아티스트", null, 0, emptyList())
