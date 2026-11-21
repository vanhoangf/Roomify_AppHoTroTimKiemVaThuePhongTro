package com.roomify.app.ui.landlord.message

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.roomify.app.R
import com.roomify.app.data.remote.dto.messaging.MessageResponse
import com.roomify.app.databinding.ItemLandlordChatMessageBinding
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class LandlordChatMessagesAdapter :
    ListAdapter<
            MessageResponse,
            LandlordChatMessagesAdapter.ViewHolder
            >(DiffCallback) {

    private val vietnamZone =
        ZoneId.of(
            "Asia/Ho_Chi_Minh"
        )

    inner class ViewHolder(
        private val binding:
        ItemLandlordChatMessageBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            item: MessageResponse
        ) {

            binding.tvMessage.text =
                when {

                    item.messageType
                        .equals(
                            "IMAGE",
                            ignoreCase = true
                        ) -> {
                        "[Hình ảnh]"
                    }

                    !item.content
                        .isNullOrBlank() -> {
                        item.content
                    }

                    else -> {
                        ""
                    }
                }

            binding.tvTime.text =
                formatTime(
                    item.sentAt
                )

            if (
                item.isMine
            ) {

                binding.messageRoot.gravity =
                    Gravity.END

                binding.tvMessage
                    .setBackgroundResource(
                        R.drawable.bg_message_mine
                    )

                binding.tvMessage
                    .setTextColor(
                        Color.WHITE
                    )

                binding.tvTime
                    .setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.text_muted
                        )
                    )

            } else {

                binding.messageRoot.gravity =
                    Gravity.START

                binding.tvMessage
                    .setBackgroundResource(
                        R.drawable.bg_message_other
                    )

                binding.tvMessage
                    .setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.text_primary
                        )
                    )

                binding.tvTime
                    .setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.text_muted
                        )
                    )
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(
            ItemLandlordChatMessageBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        holder.bind(
            getItem(position)
        )
    }

    private fun formatTime(
        value: String
    ): String {

        val dateTime =
            runCatching {

                Instant
                    .parse(value)
                    .atZone(
                        vietnamZone
                    )

            }.recoverCatching {

                OffsetDateTime
                    .parse(value)
                    .atZoneSameInstant(
                        vietnamZone
                    )

            }.recoverCatching {

                LocalDateTime
                    .parse(value)
                    .atZone(
                        ZoneId.of("UTC")
                    )
                    .withZoneSameInstant(
                        vietnamZone
                    )

            }.getOrNull()
                ?: return value
                    .take(16)
                    .replace(
                        "T",
                        " "
                    )

        val today =
            java.time.ZonedDateTime
                .now(
                    vietnamZone
                )
                .toLocalDate()

        return if (
            dateTime.toLocalDate() ==
            today
        ) {

            dateTime.format(
                DateTimeFormatter.ofPattern(
                    "HH:mm"
                )
            )

        } else {

            dateTime.format(
                DateTimeFormatter.ofPattern(
                    "dd/MM • HH:mm"
                )
            )
        }
    }

    companion object {

        private val DiffCallback =
            object :
                DiffUtil.ItemCallback<
                        MessageResponse
                        >() {

                override fun areItemsTheSame(
                    oldItem:
                    MessageResponse,
                    newItem:
                    MessageResponse
                ): Boolean {

                    return oldItem.messageId ==
                            newItem.messageId
                }

                override fun areContentsTheSame(
                    oldItem:
                    MessageResponse,
                    newItem:
                    MessageResponse
                ): Boolean {

                    return oldItem ==
                            newItem
                }
            }
    }
}