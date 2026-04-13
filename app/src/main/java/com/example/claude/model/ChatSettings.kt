package com.example.claude.model

data class ChatSettings(
    val answerFormat: String = "",
    val maxTokens: Int = 1024,
    val stopSequence: String = "",
    val temperature: Float = 1.0f
)
