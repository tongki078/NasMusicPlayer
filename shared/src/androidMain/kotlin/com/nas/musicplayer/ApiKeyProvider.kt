package com.nas.musicplayer

import com.nas.musicplayer.shared.BuildConfig

actual object ApiKeyProvider {
    actual fun getGeminiApiKey(): String {
        return BuildConfig.GEMINI_API_KEY
    }
}
