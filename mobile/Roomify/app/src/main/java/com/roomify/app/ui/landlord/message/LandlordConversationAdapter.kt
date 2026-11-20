package com.roomify.app.ui.landlord.message

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.roomify.app.data.remote.dto.messaging.ConversationResponse
import com.roomify.app.databinding.ItemLandlordConversationBinding

import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class LandlordConversationAdapter(
    private val onClick:
        (ConversationResponse) -> Unit
) : ListAdapter<
        ConversationResponse,
        LandlordConversationAdapter.ViewHolder
        >(DiffCallback) {

    private val vietnamZone =
        ZoneId.of(
            "Asia/Ho_Chi_Minh"
        )

    inner class ViewHolder(
        private val binding:
        ItemLandlordConversationBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            item: ConversationResponse
        ) {

            binding.tvStudentName.text =
                item.studentName
                    .ifBlank {
                        "Sinh viên"
                    }

            binding.tvRoomTitle.text =
                item.roomTitle

            binding.tvAvatar.text =
                item.studentName
                    .trim()
                    .firstOrNull()
                    ?.uppercase()
                    ?: "S"

            binding.tvLastMessage.text =
                when {

                    item.lastMessageType
                        .equals(
                            "IMAGE",
                            ignoreCase = true
                        ) ->
                        "[Hình ảnh]"

                    !item.lastMessage
                        .isNullOrBlank() ->
                        item.lastMessage

                    else ->
                        "Chưa có tin nhắn."
                }

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
                if (
                    item.unreadCount > 99
                ) {
                    "99+"
                } else {
                    item.unreadCount.toString()
                }

            binding.tvStudentName.setTypeface(
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

            binding.cardConversation
                .setOnClickListener {

                    onClick(
                        item
                    )
                }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(
            ItemLandlordConversationBinding.inflate(
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
        value: String?
    ): String {

        if (
            value.isNullOrBlank()
        ) {
            return ""
        }

        val dateTime =
            runCatching {

                Instant
                    .parse(
                        value
                    )
                    .atZone(
                        vietnamZone
                    )

            }.recoverCatching {

                OffsetDateTime
                    .parse(
                        value
                    )
                    .atZoneSameInstant(
                        vietnamZone
                    )

            }.recoverCatching {

                LocalDateTime
                    .parse(
                        value
                    )
                    .atZone(
                        ZoneId.of(
                            "UTC"
                        )
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
                    "dd/MM"
                )
            )
        }
    }

    companion object {

        private val DiffCallback =
            object :
                DiffUtil.ItemCallback<
                        ConversationResponse
                        >() {

                override fun areItemsTheSame(
                    oldItem:
                    ConversationResponse,
                    newItem:
                    ConversationResponse
                ): Boolean {

                    return oldItem
                        .conversationId ==
                            newItem
                                .conversationId
                }

                override fun areContentsTheSame(
                    oldItem:
                    ConversationResponse,
                    newItem:
                    ConversationResponse
                ): Boolean {

                    return oldItem ==
                            newItem
                }
            }
    }
}