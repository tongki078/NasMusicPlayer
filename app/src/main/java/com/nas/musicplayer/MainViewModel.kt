package com.nas.musicplayer

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _outputText = MutableLiveData<String>()
    val outputText: LiveData<String> = _outputText

    private val geminiService: GeminiService

    init {
        // BuildConfig에서 API 키를 정확한 필드 이름으로 가져옵니다.
        val apiKey = BuildConfig.GEMINI_API_KEY
        Log.d("MainViewModel", "API Key used: $apiKey")
        geminiService = GeminiService(apiKey)
    }

    fun generateText(prompt: String) {
        Log.d("MainViewModel", "generateText called with prompt: $prompt")
        // No need for viewModelScope.launch here if the callback handles the main thread update
        geminiService.generateText(prompt) { result ->
            Log.d("MainViewModel", "Result from GeminiService: $result")
            _outputText.postValue(result ?: "An error occurred. Please try again later.")
        }
    }
}