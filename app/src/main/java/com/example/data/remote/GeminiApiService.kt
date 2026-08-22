package com.example.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiApiService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateContent(apiKey: String, prompt: String, systemInstruction: String? = null): String {
        val rootJson = JSONObject()
        
        // System instruction if provided
        if (!systemInstruction.isNullOrEmpty()) {
            val sysPart = JSONObject().put("text", systemInstruction)
            val sysParts = JSONArray().put(sysPart)
            val sysContent = JSONObject().put("parts", sysParts)
            rootJson.put("systemInstruction", sysContent)
        }

        // Contents
        val part = JSONObject().put("text", prompt)
        val parts = JSONArray().put(part)
        val content = JSONObject().put("parts", parts)
        val contents = JSONArray().put(content)
        rootJson.put("contents", contents)

        // Generation Config
        val genConfig = JSONObject()
        genConfig.put("temperature", 0.4)
        genConfig.put("topP", 0.95)
        rootJson.put("generationConfig", genConfig)

        val requestBody = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val url = "$BASE_URL?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                throw Exception("HTTP ${response.code}: $errorBody")
            }
            val responseString = response.body?.string() ?: throw Exception("Empty response from Gemini API")
            val respJson = JSONObject(responseString)
            val candidates = respJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val cContent = candidate.optJSONObject("content")
                val cParts = cContent?.optJSONArray("parts")
                if (cParts != null && cParts.length() > 0) {
                    return cParts.getJSONObject(0).optString("text", "")
                }
            }
            throw Exception("No text candidates found in Gemini response")
        }
    }
}
