package com.roomify.app.ui.student

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.roomify.app.data.remote.dto.messaging.ConversationResponse
import com.roomify.app.databinding.ItemConversationBinding

import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ConversationsAdapter(
    private val onClick:
        (ConversationResponse) -> Unit
) : ListAdapter<
        ConversationResponse,
        ConversationsAdapter.ViewHolder
        >(DIFF) {

    private val vietnamZone =
        ZoneId.of("Asia/Ho_Chi_Minh")

    inner class ViewHolder(
        private val binding:
        ItemConversationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: ConversationResponse
        ) {

            binding.tvLandlordName.text =
                item.landlordName

            binding.tvRoomTitle.text =
                item.roomTitle

            binding.tvLastMessage.text =
                item.lastMessage
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: "Chưa có tin nhắn."

            binding.tvTime.text =
                formatTime(
                    item.lastMessageAt
                        ?: item.createdAt
                )

            val hasUnread =
                item.unreadCount > 0

            binding.tvUnreadCount.visibility =
                if (hasUnread) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            binding.tvUnreadCount.text =
                if (item.unreadCount > 99) {
                    "99+"
                } else {
                    item.unreadCount.toString()
                }

            binding.tvLandlordName.setTypeface(
                null,
                if (hasUnread) {
                    Typeface.BOLD
                } else {
                    Typeface.NORMAL
                }
            )

            binding.tvLastMessage.setTypeface(
                null,
                if (hasUnread) {
                    Typeface.BOLD
                } else {
                    Typeface.NORMAL
                }
            )

            binding.root
                .setOnClickListener {

                    onClick(item)
                }
        }
    }

    private fun formatTime(
        value: String?
    ): String {

        if (value.isNullOrBlank()) {
            return ""
        }

        val formatterToday =
            DateTimeFormatter.ofPattern(
                "HH:mm"
            )

        val formatterDate =
            DateTimeFormatter.ofPattern(
                "dd/MM"
            )

        val dateTime =
            runCatching {

                Instant.parse(value)
                    .atZone(vietnamZone)

            }.recoverCatching {

                OffsetDateTime.parse(value)
                    .atZoneSameInstant(
                        vietnamZone
                    )

            }.recoverCatching {

                LocalDateTime.parse(value)
                    .atZone(
                        ZoneId.of("UTC")
                    )
                    .withZoneSameInstant(
                        vietnamZone
                    )

            }.getOrNull()
                ?: return value
                    .take(16)
                    .replace("T", " ")

        val today =
            java.time.ZonedDateTime
                .now(vietnamZone)
                .toLocalDate()

        return if (
            dateTime.toLocalDate() ==
            today
        ) {

            dateTime.format(
                formatterToday
            )

        } else {

            dateTime.format(
                formatterDate
            )
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(
            ItemConversationBinding.inflate(
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

    companion object {

        private val DIFF =
            object :
                DiffUtil.ItemCallback<
                        ConversationResponse
                        >() {

                override fun areItemsTheSame(
                    oldItem: ConversationResponse,
                    newItem: ConversationResponse
                ) =
                    oldItem.conversationId ==
                            newItem.conversationId

                override fun areContentsTheSame(
                    oldItem: ConversationResponse,
                    newItem: ConversationResponse
                ) =
                    oldItem == newItem
            }
    }
}