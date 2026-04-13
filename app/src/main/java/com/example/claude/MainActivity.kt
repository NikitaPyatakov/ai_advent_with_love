package com.example.claude

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.claude.adapter.MessageAdapter
import com.example.claude.databinding.ActivityMainBinding
import com.example.claude.model.ChatSettings
import com.example.claude.viewmodel.ChatViewModel

class MainActivity : AppCompatActivity(), SettingsDialog.Listener {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ChatViewModel by viewModels()
    private val adapter = MessageAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, maxOf(ime.bottom, bars.bottom))
            insets
        }

        setupRecyclerView()
        setupSendButton()
        observeViewModel()
        viewModel.updateSettings(loadSettings())
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.rvMessages.layoutManager = layoutManager
        binding.rvMessages.adapter = adapter
    }

    private fun setupSendButton() {
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString()
            if (text.isNotBlank()) {
                viewModel.sendMessage(text)
                binding.etMessage.setText("")
            }
        }
        binding.btnSettings.setOnClickListener {
            val dialog = SettingsDialog()
            dialog.setCurrentSettings(viewModel.settings.value ?: ChatSettings())
            dialog.show(supportFragmentManager, "settings")
        }

        binding.btnClearChat.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Очистить чат")
                .setMessage("Удалить всю историю переписки?")
                .setPositiveButton("Удалить") { _, _ -> viewModel.clearChat() }
                .setNegativeButton("Отмена", null)
                .show()
        }
    }

    override fun onSettingsSaved(settings: ChatSettings) {
        viewModel.updateSettings(settings)
        saveSettings(settings)
    }

    private fun loadSettings(): ChatSettings {
        val prefs = getSharedPreferences("claude_settings", Context.MODE_PRIVATE)
        return ChatSettings(
            answerFormat = prefs.getString("answer_format", "") ?: "",
            maxTokens = prefs.getInt("max_tokens", 1024),
            stopSequence = prefs.getString("stop_sequence", "") ?: ""
        )
    }

    private fun saveSettings(settings: ChatSettings) {
        getSharedPreferences("claude_settings", Context.MODE_PRIVATE)
            .edit()
            .putString("answer_format", settings.answerFormat)
            .putInt("max_tokens", settings.maxTokens)
            .putString("stop_sequence", settings.stopSequence)
            .apply()
    }

    private fun observeViewModel() {
        viewModel.messages.observe(this) { messages ->
            adapter.submitList(messages.toList())
            if (messages.isNotEmpty()) {
                binding.rvMessages.scrollToPosition(messages.size - 1)
            }
        }

        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnSend.isEnabled = !loading
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Ошибка")
                    .setMessage(it)
                    .setPositiveButton("ОК") { dialog, _ ->
                        dialog.dismiss()
                        viewModel.clearError()
                    }
                    .show()
            }
        }
    }
}
