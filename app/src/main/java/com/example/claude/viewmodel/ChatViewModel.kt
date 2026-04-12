package com.example.claude.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun sendMessage(text: String) {
        if (text.isBlank() || _isLoading.value == true) return

        val userMessage = Message(text.trim(), isUser = true)
        addMessage(userMessage)
        conversationHistory.add(ClaudeMessageRequest(role = "user", content = text.trim()))

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            val result = repository.sendMessage(conversationHistory.toList())
            result.onSuccess { responseText ->
                conversationHistory.add(ClaudeMessageRequest(role = "assistant", content = responseText))
                addMessage(Message(responseText, isUser = false))
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

    fun clearError() {
        _error.value = null
    }
}
