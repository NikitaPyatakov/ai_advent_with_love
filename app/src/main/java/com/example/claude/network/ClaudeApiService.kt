package com.example.claude.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

data class ClaudeMessageRequest(
    val role: String,
    val content: String
)

data class ClaudeRequest(
    val model: String,
    val max_tokens: Int,
    val messages: List<ClaudeMessageRequest>
)

data class ContentBlock(
    val type: String,
    val text: String?
)

data class ClaudeResponse(
    val content: List<ContentBlock>
)

interface ClaudeApiService {

    @POST("v1/messages")
    @Headers(
        "Content-Type: application/json",
        "anthropic-version: 2023-06-01"
    )
    suspend fun sendMessage(
        @Header("x-api-key") apiKey: String,
        @Body request: ClaudeRequest
    ): ClaudeResponse
}
