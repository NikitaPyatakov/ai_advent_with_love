package com.example.claude.model

data class ChatSettings(
    val answerFormat: String = "",
    val maxTokens: Int = 1024,
    val stopSequence: String = ""
)
