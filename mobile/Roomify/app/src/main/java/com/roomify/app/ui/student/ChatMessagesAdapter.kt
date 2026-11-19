package com.roomify.app.ui.student

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.roomify.app.R
import com.roomify.app.data.remote.dto.messaging.MessageResponse
import com.roomify.app.databinding.ItemChatMessageBinding

class ChatMessagesAdapter :
    ListAdapter<
            MessageResponse,
            ChatMessagesAdapter.ViewHolder
            >(DIFF) {

    inner class ViewHolder(
        private val binding:
        ItemChatMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: MessageResponse
        ) {

            binding.tvMessage.text =
                if (
                    item.messageType.equals(
                        "IMAGE",
                        ignoreCase = true
                    )
                ) {
                    "[Hình ảnh]"
                } else {
                    item.content.orEmpty()
                }

            binding.tvTime.text =
                item.sentAt
                    .take(16)
                    .replace("T", " ")

            if (item.isMine) {

                binding.messageRoot.gravity =
                    Gravity.END

                binding.tvMessage.setBackgroundResource(
                    R.drawable.bg_message_mine
                )

                binding.tvMessage.setTextColor(
                    Color.WHITE
                )

            } else {

                binding.messageRoot.gravity =
                    Gravity.START

                binding.tvMessage.setBackgroundResource(
                    R.drawable.bg_message_other
                )

                binding.tvMessage.setTextColor(
                    Color.BLACK
                )
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(
            ItemChatMessageBinding.inflate(
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
                        MessageResponse
                        >() {

                override fun areItemsTheSame(
                    oldItem: MessageResponse,
                    newItem: MessageResponse
                ) =
                    oldItem.messageId ==
                            newItem.messageId

                override fun areContentsTheSame(
                    oldItem: MessageResponse,
                    newItem: MessageResponse
                ) =
                    oldItem == newItem
            }
    }
}