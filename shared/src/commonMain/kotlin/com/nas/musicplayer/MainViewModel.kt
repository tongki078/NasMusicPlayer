package com.nas.musicplayer

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel : BaseViewModel() {

    private val musicPlayerState: MusicPlayerState

    val outputText: StateFlow<String>

    init {
        val apiKey = ApiKeyProvider.getGeminiApiKey()
        val geminiService = GeminiService(apiKey)
        musicPlayerState = MusicPlayerState(geminiService)
        outputText = musicPlayerState.outputText.stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ""
        )
    }

    fun generateText(prompt: String) {
        musicPlayerState.generateText(prompt)
    }
}