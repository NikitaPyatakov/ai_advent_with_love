package com.example.claude.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.claude.R
import com.example.claude.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter : ListAdapter<Message, MessageAdapter.MessageViewHolder>(DiffCallback) {

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_ASSISTANT = 1

        private val DiffCallback = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(oldItem: Message, newItem: Message) =
                oldItem === newItem

            override fun areContentsTheSame(oldItem: Message, newItem: Message) =
                oldItem == newItem
        }

        // Prices per 1M tokens in USD
        private val INPUT_PRICE = mapOf(
            "claude-opus-4-6" to 15.0,
            "claude-sonnet-4-6" to 3.0,
            "claude-haiku-4-5-20251001" to 0.8,
            "claude-opus-4-5" to 15.0,
            "claude-sonnet-4-5" to 3.0,
            "claude-3-opus-20240229" to 15.0
        )
        private val OUTPUT_PRICE = mapOf(
            "claude-opus-4-6" to 75.0,
            "claude-sonnet-4-6" to 15.0,
            "claude-haiku-4-5-20251001" to 4.0,
            "claude-opus-4-5" to 75.0,
            "claude-sonnet-4-5" to 15.0,
            "claude-3-opus-20240229" to 75.0
        )

        private val TIME_FORMAT = SimpleDateFormat("HH:mm:ss, dd MMM yyyy", Locale.getDefault())

        fun calculateCost(model: String, inputTokens: Int, outputTokens: Int): Double {
            val inPrice = INPUT_PRICE[model] ?: 3.0
            val outPrice = OUTPUT_PRICE[model] ?: 15.0
            return (inputTokens * inPrice + outputTokens * outPrice) / 1_000_000.0
        }
    }

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).isUser) VIEW_TYPE_USER else VIEW_TYPE_ASSISTANT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutId = if (viewType == VIEW_TYPE_USER) {
            R.layout.item_message_user
        } else {
            R.layout.item_message_assistant
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.tvMessageContent)
        private val tokenCountView: TextView = itemView.findViewById(R.id.tvTokenCount)

        fun bind(message: Message) {
            textView.text = message.content

            if (message.isUser) {
                tokenCountView.text = "~${message.inputTokens} tokens"
            } else {
                if (message.outputTokens > 0) {
                    tokenCountView.text = "↑${message.inputTokens} ↓${message.outputTokens} tokens"
                } else {
                    tokenCountView.visibility = View.GONE
                    return
                }
            }
            tokenCountView.visibility = View.VISIBLE

            itemView.setOnLongClickListener {
                showMessageDetails(message)
                true
            }
        }

        private fun showMessageDetails(message: Message) {
            val ctx = itemView.context
            val time = TIME_FORMAT.format(Date(message.timestamp))

            val sb = StringBuilder()
            sb.appendLine("Время: $time")
            sb.appendLine()

            if (message.isUser) {
                sb.appendLine("Токены (оценка): ~${message.inputTokens}")
            } else {
                sb.appendLine("Токены входящие: ${message.inputTokens}")
                sb.appendLine("Токены исходящие: ${message.outputTokens}")
                if (message.model.isNotEmpty()) {
                    val cost = calculateCost(message.model, message.inputTokens, message.outputTokens)
                    sb.appendLine()
                    sb.appendLine("Модель: ${message.model}")
                    sb.appendLine("Стоимость ответа: \$${String.format("%.6f", cost)}")
                }
                if (message.responseTimeMs > 0) {
                    val secs = message.responseTimeMs / 1000.0
                    sb.appendLine()
                    sb.appendLine("Скорость ответа: ${String.format("%.1f", secs)} с")
                    if (message.outputTokens > 0 && secs > 0) {
                        val tokensPerSec = message.outputTokens / secs
                        sb.appendLine("Скорость генерации: ${String.format("%.0f", tokensPerSec)} токенов/с")
                    }
                }
            }

            AlertDialog.Builder(ctx)
                .setTitle("Детали сообщения")
                .setMessage(sb.toString().trimEnd())
                .setPositiveButton("Копировать") { _, _ ->
                    copyToClipboard(ctx, message.content)
                }
                .setNegativeButton("Закрыть", null)
                .show()
        }

        private fun copyToClipboard(ctx: Context, text: String) {
            val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("message", text))
            Toast.makeText(ctx, "Скопировано", Toast.LENGTH_SHORT).show()
        }
    }
}
