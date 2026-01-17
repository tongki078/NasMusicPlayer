package com.nas.musicplayer.ui.music

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nas.musicplayer.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "playlist_table")
data class Song(
    @PrimaryKey(autoGenerate = true)
    val dbId: Int = 0,
    @SerialName("id") val id: Long = 0L,
    @SerialName("name") val name: String? = null,
    @SerialName("path") val path: String? = null,
    @SerialName("is_dir") val isDir: Boolean = false,
    @SerialName("size") val size: Long = 0L,
    @SerialName("category") val category: String? = null,
    @SerialName("stream_url") val streamUrl: String? = null,
    @SerialName("mtime") val mtime: String? = null,
    @SerialName("parent_path") val parentPath: String? = null,
    @SerialName("mtime_ts") val mtimeTs: Double? = null,
    @SerialName("meta_id") val metaId: String? = null,
    @SerialName("meta_poster") val metaPoster: String? = null,
    val artist: String = "Unknown Artist",
    val albumName: String = "Unknown Album",
    val albumArtRes: Int? = R.drawable.ic_launcher_background,
    // 차후 album_info 객체를 위한 필드 (현재는 임시 String)
    val albumInfo: String? = null 
)

data class Artist(
    val id: Long = 0L,
    val name: String,
    val profileImageRes: Int = R.drawable.ic_launcher_background,
    val imageUrl: String? = null,
    val followers: String = "0",
    val popularSongs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList() // 아티스트의 앨범 목록 추가
)

data class Album(
    val name: String,
    val artist: String,
    val albumArtRes: Int = R.drawable.ic_launcher_background,
    val year: Int = 0,
    val songs: List<Song> = emptyList(),
    val imageUrl: String? = null
)

val emptySong = Song(id = 0L, name = "노래 정보 없음")
val emptyArtist = Artist(name = "알 수 없는 아티스트")
val emptyAlbum = Album("알 수 없는 앨범", "알 수 없는 아티스트", R.drawable.ic_launcher_background, 0, emptyList())
