package com.example.claude.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.claude.model.ChatSettings
import com.example.claude.model.Message
import com.example.claude.network.ClaudeMessageRequest
import com.example.claude.network.ClaudeRepository
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val repository = ClaudeRepository()

    private val _messages = MutableLiveData<List<Message>>(emptyList())
    val messages: LiveData<List<Message>> = _messages

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val conversationHistory = mutableListOf<ClaudeMessageRequest>()

    private val _settings = MutableLiveData(ChatSettings())
    val settings: LiveData<ChatSettings> = _settings

    fun updateSettings(settings: ChatSettings) {
        _settings.value = settings
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isLoading.value == true) return

        val trimmed = text.trim()
        val estimatedTokens = (trimmed.length / 4).coerceAtLeast(1)
        val userMessage = Message(trimmed, isUser = true, inputTokens = estimatedTokens)
        addMessage(userMessage)
        conversationHistory.add(ClaudeMessageRequest(role = "user", content = trimmed))

        _isLoading.value = true
        _error.value = null

        val currentSettings = _settings.value ?: ChatSettings()

        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val result = repository.sendMessage(conversationHistory.toList(), currentSettings)
            val responseTimeMs = System.currentTimeMillis() - startTime
            result.onSuccess { msgResult ->
                conversationHistory.add(ClaudeMessageRequest(role = "assistant", content = msgResult.text))
                addMessage(
                    Message(
                        content = msgResult.text,
                        isUser = false,
                        inputTokens = msgResult.inputTokens,
                        outputTokens = msgResult.outputTokens,
                        model = currentSettings.model,
                        responseTimeMs = responseTimeMs
                    )
                )
            }.onFailure { throwable ->
                _error.value = throwable.message ?: "Unknown error"
            }
            _isLoading.value = false
        }
    }

    private fun addMessage(message: Message) {
        val current = _messages.value.orEmpty().toMutableList()
        current.add(message)
        _messages.value = current
    }

    fun clearChat() {
        conversationHistory.clear()
        _messages.value = emptyList()
    }

    fun clearError() {
        _error.value = null
    }
}
