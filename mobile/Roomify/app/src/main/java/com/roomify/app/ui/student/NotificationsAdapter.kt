package com.roomify.app.ui.student

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.roomify.app.data.remote.dto.notifications.NotificationResponse
import com.roomify.app.databinding.ItemNotificationBinding

import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NotificationsAdapter(
    private val onClick:
        (NotificationResponse) -> Unit
) : ListAdapter<
        NotificationResponse,
        NotificationsAdapter.ViewHolder
        >(DIFF) {

    private val zone =
        ZoneId.of("Asia/Ho_Chi_Minh")

    inner class ViewHolder(
        private val binding:
        ItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: NotificationResponse
        ) {

            binding.tvTitle.text =
                item.title

            binding.tvMessage.text =
                item.message

            binding.tvTime.text =
                formatDateTime(
                    item.createdAt
                )

            binding.chipUnread.visibility =
                if (item.isRead) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

            binding.tvTitle.setTypeface(
                null,
                if (item.isRead) {
                    Typeface.NORMAL
                } else {
                    Typeface.BOLD
                }
            )

            binding.root
                .setOnClickListener {

                    onClick(item)
                }
        }
    }

    private fun formatDateTime(
        value: String
    ): String {

        val formatter =
            DateTimeFormatter.ofPattern(
                "dd/MM/yyyy • HH:mm"
            )

        return runCatching {

            Instant.parse(value)
                .atZone(zone)
                .format(formatter)

        }.recoverCatching {

            OffsetDateTime.parse(value)
                .atZoneSameInstant(zone)
                .format(formatter)

        }.recoverCatching {

            LocalDateTime.parse(value)
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(zone)
                .format(formatter)

        }.getOrElse {

            value
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(
            ItemNotificationBinding.inflate(
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
                        NotificationResponse
                        >() {

                override fun areItemsTheSame(
                    oldItem: NotificationResponse,
                    newItem: NotificationResponse
                ) =
                    oldItem.notificationId ==
                            newItem.notificationId

                override fun areContentsTheSame(
                    oldItem: NotificationResponse,
                    newItem: NotificationResponse
                ) =
                    oldItem == newItem
            }
    }
}