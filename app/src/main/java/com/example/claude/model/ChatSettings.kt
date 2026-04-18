package com.example.claude.model

data class ChatSettings(
    val model: String = "claude-sonnet-4-6",
    val answerFormat: String = "",
    val maxTokens: Int = 1024,
    val stopSequence: String = "",
    val temperature: Float = 1.0f
)
