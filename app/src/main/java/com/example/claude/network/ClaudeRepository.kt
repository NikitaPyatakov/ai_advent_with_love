package com.example.claude.network

import com.example.claude.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ClaudeRepository {

    private val apiKey = BuildConfig.CLAUDE_API_KEY

    private val service: ClaudeApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.anthropic.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClaudeApiService::class.java)
    }

    suspend fun sendMessage(history: List<ClaudeMessageRequest>): Result<String> {
        return try {
            val request = ClaudeRequest(
                model = "claude-sonnet-4-6",
                max_tokens = 1024,
                messages = history
            )
            val response = service.sendMessage(apiKey, request)
            val text = response.content.firstOrNull { it.type == "text" }?.text
                ?: "No response"
            Result.success(text)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val apiMessage = errorBody?.let {
                runCatching {
                    JSONObject(it).getJSONObject("error").getString("message")
                }.getOrNull()
            }
            val message = if (apiMessage != null) {
                "HTTP ${e.code()}: $apiMessage"
            } else {
                "HTTP ${e.code()}: ${e.message()}"
            }
            Result.failure(Exception(message))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
