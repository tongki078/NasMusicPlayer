package com.nas.musicplayer.ui.music

import com.nas.musicplayer.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 앱 전체의 데이터 모델을 정의합니다.
 */

// --- Data Models ---
@Serializable
data class Song(
    @SerialName("id") val id: Long = 0L,
    @SerialName("name") val name: String? = null,
    @SerialName("path") val path: String? = null,
    @SerialName("is_dir") val isDir: Boolean = false,
    @SerialName("size") val size: Long = 0L,
    @SerialName("category") val category: String? = null,
    @SerialName("stream_url") val streamUrl: String? = null, // 서버의 stream_url과 매핑
    @SerialName("mtime") val mtime: String? = null,
    @SerialName("parent_path") val parentPath: String? = null,
    @SerialName("mtime_ts") val mtimeTs: Double? = null,
    @SerialName("meta_id") val metaId: String? = null,
    @SerialName("meta_poster") val metaPoster: String? = null,

    // UI 표시를 위한 보조 필드 (JSON에 없으므로 기본값 사용)
    val artist: String = "NAS Music",
    val albumName: String = "NAS Library",
    val albumArtRes: Int = R.drawable.ic_launcher_background
)

data class Artist(
    val name: String,
    val profileImageRes: Int = R.drawable.ic_launcher_background,
    val imageUrl: String? = null,
    val followers: String = "0",
    val popularSongs: List<Song> = emptyList()
)

data class Album(
    val name: String,
    val artist: String,
    val albumArtRes: Int = R.drawable.ic_launcher_background,
    val year: Int = 0,
    val songs: List<Song> = emptyList(),
    val imageUrl: String? = null
)

/**
 * API 호출 전이나 데이터가 없을 때 사용되는 기본 빈 데이터들입니다.
 */
val emptySong = Song(0L, "노래 정보 없음")
val emptyArtist = Artist("알 수 없는 아티스트", R.drawable.ic_launcher_background, null, "0", emptyList())
val emptyAlbum = Album("알 수 없는 앨범", "알 수 없는 아티스트", R.drawable.ic_launcher_background, 0, emptyList())

val allSongs = emptyList<Song>()
