package com.example.claude

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.claude.databinding.DialogSettingsBinding
import com.example.claude.model.ChatSettings

class SettingsDialog : DialogFragment() {

    interface Listener {
        fun onSettingsSaved(settings: ChatSettings)
    }

    private var currentSettings: ChatSettings = ChatSettings()

    fun setCurrentSettings(settings: ChatSettings) {
        currentSettings = settings
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogSettingsBinding.inflate(layoutInflater)

        binding.etAnswerFormat.setText(currentSettings.answerFormat)
        if (currentSettings.maxTokens > 0) {
            binding.etMaxTokens.setText(currentSettings.maxTokens.toString())
        }
        binding.etStopSequence.setText(currentSettings.stopSequence)

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.settings_title)
            .setView(binding.root)
            .setPositiveButton(R.string.settings_save) { _, _ ->
                val maxTokens = binding.etMaxTokens.text.toString().toIntOrNull()
                    ?.coerceIn(1, 8096) ?: 1024
                val settings = ChatSettings(
                    answerFormat = binding.etAnswerFormat.text.toString().trim(),
                    maxTokens = maxTokens,
                    stopSequence = binding.etStopSequence.text.toString().trim()
                )
                (activity as? Listener)?.onSettingsSaved(settings)
            }
            .setNegativeButton(R.string.settings_cancel, null)
            .create()
    }
}
