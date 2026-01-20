package com.nas.musicplayer.network

/**
 * 'music.yommi.mywire.org' 서버와 통신하기 위한 API 명세를 정의합니다.dd
 */
interface MusicApiService {

    /**
     * TOP 100 음악 목록을 가져옵니다.
     * - 최종 요청 URL: https://music.yommi.mywire.org/gds_dviewer/normal/explorer/top100?apikey=gommikey
     */
    suspend fun getTop100(): SearchResponse

    /**
     * 키워드로 음악을 검색합니다.
     * - 최종 요청 URL: https://music.yommi.mywire.org/gds_dviewer/normal/explorer/search?query=...
     */
    suspend fun search(searchQuery: String): SearchResponse
}
