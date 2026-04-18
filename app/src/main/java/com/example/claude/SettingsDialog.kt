package com.example.claude

import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.claude.databinding.DialogSettingsBinding
import com.example.claude.model.ChatSettings

private sealed class ModelItem {
    class Header(val title: String) : ModelItem()
    class Entry(val id: String, val label: String) : ModelItem()
}

private class ModelSpinnerAdapter(
    context: Context,
    private val items: List<ModelItem>
) : ArrayAdapter<ModelItem>(context, 0, items) {

    override fun isEnabled(position: Int) = items[position] is ModelItem.Entry
    override fun areAllItemsEnabled() = false

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val tv = TextView(context)
        tv.textSize = 14f
        tv.setTextColor(0xFF1A1A1A.toInt())
        val item = items[position]
        tv.text = if (item is ModelItem.Entry) item.label else ""
        return tv
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = items[position]
        val tv = TextView(context)
        return when (item) {
            is ModelItem.Header -> {
                tv.text = item.title
                tv.setTypeface(null, Typeface.BOLD)
                tv.textSize = 11f
                tv.setTextColor(0xFF4A90D9.toInt())
                tv.setPadding(dp(16), dp(12), dp(16), dp(4))
                tv
            }
            is ModelItem.Entry -> {
                tv.text = item.label
                tv.textSize = 14f
                tv.setTextColor(0xFF1A1A1A.toInt())
                tv.setPadding(dp(24), dp(12), dp(16), dp(12))
                tv
            }
        }
    }

    private fun dp(value: Int) = (value * context.resources.displayMetrics.density).toInt()
}

class SettingsDialog : DialogFragment() {

    interface Listener {
        fun onSettingsSaved(settings: ChatSettings)
    }

    companion object {
        private const val ARG_MODEL = "model"
        private const val ARG_FORMAT = "answer_format"
        private const val ARG_MAX_TOKENS = "max_tokens"
        private const val ARG_STOP = "stop_sequence"
        private const val ARG_TEMPERATURE = "temperature"

        private val MODEL_ITEMS = listOf(
            ModelItem.Header("Opus"),
            ModelItem.Entry("claude-opus-4-6",        "Opus 4.6 — Глубокий анализ, научные задачи"),
            ModelItem.Entry("claude-opus-4-5",        "Opus 4.5 — Сложные рассуждения и стратегии"),
            ModelItem.Entry("claude-3-opus-20240229", "Opus 3 — Трудные задачи и программирование"),
            ModelItem.Header("Sonnet"),
            ModelItem.Entry("claude-sonnet-4-6",      "Sonnet 4.6 — Код, тексты, повседневные задачи"),
            ModelItem.Entry("claude-sonnet-4-5",      "Sonnet 4.5 — Универсальный помощник"),
            ModelItem.Header("Haiku"),
            ModelItem.Entry("claude-haiku-4-5-20251001", "Haiku 4.5 — Быстрые ответы, экономия бюджета")
        )

        val MODELS: List<String> = MODEL_ITEMS.filterIsInstance<ModelItem.Entry>().map { it.id }

        fun newInstance(settings: ChatSettings) = SettingsDialog().apply {
            arguments = Bundle().apply {
                putString(ARG_MODEL, settings.model)
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

        val model = arguments?.getString(ARG_MODEL, "claude-sonnet-4-6") ?: "claude-sonnet-4-6"
        val answerFormat = arguments?.getString(ARG_FORMAT, "") ?: ""
        val maxTokens = arguments?.getInt(ARG_MAX_TOKENS, 1024) ?: 1024
        val stopSequence = arguments?.getString(ARG_STOP, "") ?: ""
        val temperature = arguments?.getFloat(ARG_TEMPERATURE, 1.0f) ?: 1.0f

        val spinnerAdapter = ModelSpinnerAdapter(requireContext(), MODEL_ITEMS)
        binding.spinnerModel.adapter = spinnerAdapter
        val modelPos = MODEL_ITEMS.indexOfFirst { it is ModelItem.Entry && it.id == model }
        if (modelPos >= 0) binding.spinnerModel.setSelection(modelPos)

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
                val selected = MODEL_ITEMS[b.spinnerModel.selectedItemPosition] as? ModelItem.Entry
                val settings = ChatSettings(
                    model = selected?.id ?: "claude-sonnet-4-6",
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
