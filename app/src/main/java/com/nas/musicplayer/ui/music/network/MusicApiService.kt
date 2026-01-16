package com.nas.musicplayer.ui.music.network

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 'music.yommi.mywire.org' 서버와 통신하기 위한 API 명세(endpoint)를 정의하는 인터페이스입니다.
 * Retrofit 라이브러리가 이 명세를 보고 실제 네트워크 요청 코드를 생성합니다.
 */
interface MusicApiService {

    /**
     * 음악을 검색합니다.
     * - 최종 요청 URL 예시: https://music.yommi.mywire.org/gds_dviewer/normal/explorer/search?query=김민종&apikey=gommikey
     *
     * - @GET("gds_dviewer/normal/explorer/search"): Retrofit의 BASE_URL 뒤에 붙는 정확한 경로(endpoint)입니다.
     * - @Query("query"): URL의 'query' 쿼리 파라미터에 함수의 'searchQuery' 인자 값을 넣습니다.
     *   (참고: apikey는 RetrofitInstance의 Interceptor가 모든 요청에 자동으로 추가합니다.)
     *
     * @param searchQuery 검색할 키워드
     *
     *dd
     */
    @GET("gds_dviewer/normal/explorer/search")
    suspend fun search(@Query("query") searchQuery: String): SearchResponse
}
