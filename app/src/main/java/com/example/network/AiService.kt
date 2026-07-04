package com.example.network

import com.example.BuildConfig

object AiService {
    suspend fun generateContent(prompt: String, systemInstructionStr: String? = null, temperature: Float? = null, responseMimeType: String? = null): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "YOUR_GEMINI_API_KEY") {
            throw Exception("No API Key")
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = systemInstructionStr?.let { Content(parts = listOf(Part(text = it))) },
            generationConfig = if (temperature != null || responseMimeType != null) {
                GenerationConfig(temperature = temperature, responseMimeType = responseMimeType)
            } else null
        )

        val response = RetrofitClient.service.generateContent(apiKey, request)
        return response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: throw Exception("Empty response")
    }

    suspend fun generateContentWithHistory(contents: List<Content>, systemInstructionStr: String? = null, temperature: Float? = null, responseMimeType: String? = null): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "YOUR_GEMINI_API_KEY") {
            throw Exception("No API Key")
        }

        val request = GenerateContentRequest(
            contents = contents,
            systemInstruction = systemInstructionStr?.let { Content(parts = listOf(Part(text = it))) },
            generationConfig = if (temperature != null || responseMimeType != null) {
                GenerationConfig(temperature = temperature, responseMimeType = responseMimeType)
            } else null
        )

        val response = RetrofitClient.service.generateContent(apiKey, request)
        return response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: throw Exception("Empty response")
    }
}
