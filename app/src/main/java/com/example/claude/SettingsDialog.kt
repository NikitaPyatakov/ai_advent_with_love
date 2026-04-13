package com.example.claude

import android.app.Dialog
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.claude.databinding.DialogSettingsBinding
import com.example.claude.model.ChatSettings

class SettingsDialog : DialogFragment() {

    interface Listener {
        fun onSettingsSaved(settings: ChatSettings)
    }

    companion object {
        private const val ARG_FORMAT = "answer_format"
        private const val ARG_MAX_TOKENS = "max_tokens"
        private const val ARG_STOP = "stop_sequence"
        private const val ARG_TEMPERATURE = "temperature"

        fun newInstance(settings: ChatSettings) = SettingsDialog().apply {
            arguments = Bundle().apply {
                putString(ARG_FORMAT, settings.answerFormat)
                putInt(ARG_MAX_TOKENS, settings.maxTokens)
                putString(ARG_STOP, settings.stopSequence)
                putFloat(ARG_TEMPERATURE, settings.temperature)
            }
        }
    }

    private var _binding: DialogSettingsBinding? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogSettingsBinding.inflate(layoutInflater)
        _binding = binding

        val answerFormat = arguments?.getString(ARG_FORMAT, "") ?: ""
        val maxTokens = arguments?.getInt(ARG_MAX_TOKENS, 1024) ?: 1024
        val stopSequence = arguments?.getString(ARG_STOP, "") ?: ""
        val temperature = arguments?.getFloat(ARG_TEMPERATURE, 1.0f) ?: 1.0f

        binding.etAnswerFormat.setText(answerFormat)
        binding.etMaxTokens.setText(maxTokens.toString())
        binding.etStopSequence.setText(stopSequence)

        val initialProgress = (temperature * 10).toInt().coerceIn(0, 10)
        binding.seekBarTemperature.progress = initialProgress
        binding.tvTemperatureLabel.text = getString(R.string.settings_temperature_label, temperature)

        binding.seekBarTemperature.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.tvTemperatureLabel.text = getString(R.string.settings_temperature_label, progress / 10f)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.settings_title)
            .setView(binding.root)
            .setPositiveButton(R.string.settings_save) { _, _ ->
                val b = _binding ?: return@setPositiveButton
                val settings = ChatSettings(
                    answerFormat = b.etAnswerFormat.text.toString().trim(),
                    maxTokens = b.etMaxTokens.text.toString().toIntOrNull()?.coerceIn(1, 8096) ?: 1024,
                    stopSequence = b.etStopSequence.text.toString().trim(),
                    temperature = b.seekBarTemperature.progress / 10f
                )
                (activity as? Listener)?.onSettingsSaved(settings)
            }
            .setNegativeButton(R.string.settings_cancel, null)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
