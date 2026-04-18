package com.example.claude.model

data class Message(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val inputTokens: Int = 0,
    val outputTokens: Int = 0,
    val model: String = "",
    val responseTimeMs: Long = 0
)
