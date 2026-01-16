package com.nas.musicplayer.ui.music.network

import com.nas.musicplayer.ui.music.Song
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 서버의 실제 응답 구조에 맞춘 래퍼 클래스
 */
@Serializable
data class SearchResponse(
    @SerialName("count") val count: Int = 0,
    @SerialName("list") val list: List<Song> = emptyList()
)

/**
 * SearchResponse를 보정된 Song 리스트로 변환하는 확장 함수
 */
fun SearchResponse.toSongList(): List<Song> {
    return this.list.map { item ->
        // API에서 반환된 stream_url(Song 객체의 streamUrl 필드)을 아무런 가공 없이 그대로 사용합니다.
        // 불필요한 URL 조립이나 인코딩 로직을 모두 제거했습니다.
        item.copy(
            id = if (item.id == 0L) item.path?.hashCode()?.toLong() ?: 0L else item.id,
            artist = if (item.isDir) "폴더" else item.artist,
            albumName = item.parentPath ?: item.albumName
        )
    }
}
