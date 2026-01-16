package com.nas.musicplayer

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class GeminiService(private val apiKey: String) {

    private val client = OkHttpClient()

    fun generateText(prompt: String, callback: (String?) -> Unit) {
        val json = """
            {
                "contents": [{
                    "parts":[{
                        "text": "$prompt"
                    }]
                }]
            }
        """.trimIndent()

        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
//            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey")
            .url("https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=$apiKey")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("GeminiService", "OkHttp failed", e)
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                // 응답 본문은 한 번만 읽어야 합니다.
                val responseBody = response.body?.string()

                if (!response.isSuccessful || responseBody == null) {
                    Log.e("GeminiService", "Unsuccessful response. Code: ${response.code}. Body: $responseBody")
                    callback(null)
                    return
                }

                try {
                    val jsonObject = JSONObject(responseBody)

                    // 'candidates' 필드가 있는지 확인합니다.
                    if (jsonObject.has("candidates")) {
                        val candidatesArray = jsonObject.getJSONArray("candidates")
                        if (candidatesArray.length() > 0) {
                            val resultText = candidatesArray.getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text")
                            callback(resultText)
                        } else {
                            callback("응답을 받았지만 내용이 비어있습니다.")
                        }
                    } else {
                        // 'candidates' 필드가 없으면 오류 메시지를 파싱해봅니다.
                        val errorMessage = jsonObject.optJSONObject("error")?.optString("message") ?: "알 수 없는 오류"
                        Log.e("GeminiService", "API Error: $errorMessage")
                        callback("API 오류: $errorMessage")
                    }
                } catch (e: Exception) {
                    Log.e("GeminiService", "JSON parsing failed", e)
                    callback(null)
                }
            }
        })
    }
}
