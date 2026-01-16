package com.nas.musicplayer.ui.music.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

object RetrofitInstance {

    private const val BASE_URL = "https://music.yommi.mywire.org/"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    // ★ 모든 네트워크 요청에 API 키를 자동으로 추가하는 인터셉터
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val url = original.url.newBuilder()
            .addQueryParameter("apikey", "gommikey")
            .build()
        val request = original.newBuilder().url(url).build()
        chain.proceed(request)
    }

    // ★ ExoPlayer와 공유할 수 있도록 OkHttpClient를 public으로 변경
    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // 인증 클라이언트 사용
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    // ★ ViewModel에서 명확하게 사용할 수 있도록 이름 변경 (api -> musicApiService)
    val musicApiService: MusicApiService by lazy {
        retrofit.create(MusicApiService::class.java)
    }
}
