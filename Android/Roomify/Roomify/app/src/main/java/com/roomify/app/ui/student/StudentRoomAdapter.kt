package com.roomify.app.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.bumptech.glide.Glide
import com.roomify.app.R
import com.roomify.app.data.remote.dto.rooms.RoomCardResponse
import com.roomify.app.databinding.ItemStudentRoomBinding

import java.text.NumberFormat
import java.util.Locale

class StudentRoomAdapter(
    private val horizontal: Boolean = false,
    private val onRoomClick:
        (RoomCardResponse) -> Unit = {}
) : ListAdapter<
        RoomCardResponse,
        StudentRoomAdapter.RoomViewHolder
        >(ROOM_DIFF) {

    private val currencyFormatter =
        NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("vi-VN")
        )

    inner class RoomViewHolder(
        private val binding:
        ItemStudentRoomBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            room: RoomCardResponse
        ) {

            // =============================
            // TITLE
            // =============================

            binding.tvRoomTitle.text =
                room.title

            // =============================
            // PRICE
            // =============================

            binding.tvRoomPrice.text =
                "${currencyFormatter.format(
                    room.rentPrice
                )}/tháng"

            // =============================
            // AREA
            // =============================

            val area =
                room.areaM2
                    .stripTrailingZeros()
                    .toPlainString()

            binding.tvRoomArea.text =
                "Diện tích: $area m²"

            // =============================
            // OCCUPANTS
            // =============================

            binding.tvRoomOccupants.text =
                "Tối đa ${room.maxOccupants} người"

            // =============================
            // ADDRESS
            // =============================

            val address =
                listOf(
                    room.addressLine,
                    room.wardName,
                    room.provinceName
                )
                    .filter {
                        it.isNotBlank()
                    }
                    .joinToString(", ")

            binding.tvRoomAddress.text =
                address.ifBlank {
                    "Địa chỉ đang cập nhật"
                }

            // =============================
            // STATUS
            // =============================

            val status =
                room.availabilityStatus
                    .uppercase(
                        Locale.ROOT
                    )

            val statusText =
                when (status) {

                    "AVAILABLE" ->
                        "Còn trống"

                    "RESERVED" ->
                        "Đã giữ chỗ"

                    "RENTED" ->
                        "Đã cho thuê"

                    else ->
                        "Đang cập nhật"
                }

            val statusColor =
                when (status) {

                    "AVAILABLE" ->
                        R.color.status_success

                    "RENTED" ->
                        R.color.status_error

                    else ->
                        R.color.text_muted
                }

            binding.tvRoomStatus.text =
                statusText

            binding.tvRoomStatus
                .setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        statusColor
                    )
                )

            // =============================
            // IMAGE
            // =============================

            val imageUrl =
                room.thumbnailUrl
                    ?.trim()
                    ?.takeIf {
                        it.startsWith(
                            "https://"
                        )
                    }

            binding.ivRoom
                .contentDescription =
                "Ảnh phòng ${room.title}"

            Glide.with(
                binding.ivRoom
            )
                .load(imageUrl)
                .placeholder(
                    R.drawable
                        .room_image_placeholder
                )
                .error(
                    R.drawable
                        .room_image_placeholder
                )
                .centerCrop()
                .into(
                    binding.ivRoom
                )
            binding.root.setOnClickListener {

                onRoomClick(
                    room
                )
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RoomViewHolder {

        val binding =
            ItemStudentRoomBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        // =====================================
        // HOME: CARD NGANG 288dp
        // SEARCH: GIỮ MATCH_PARENT
        // =====================================

        if (horizontal) {

            val density =
                parent.resources
                    .displayMetrics
                    .density

            val cardWidth =
                (288 * density)
                    .toInt()

            val cardSpacing =
                (12 * density)
                    .toInt()

            binding.root.layoutParams =
                RecyclerView.LayoutParams(
                    cardWidth,
                    ViewGroup.LayoutParams
                        .WRAP_CONTENT
                ).apply {

                    marginEnd =
                        cardSpacing
                }
        }

        return RoomViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(
        holder: RoomViewHolder,
        position: Int
    ) {

        holder.bind(
            getItem(position)
        )
    }

    companion object {

        private val ROOM_DIFF =
            object :
                DiffUtil.ItemCallback<
                        RoomCardResponse
                        >() {

                override fun areItemsTheSame(
                    oldItem: RoomCardResponse,
                    newItem: RoomCardResponse
                ): Boolean {

                    return oldItem.roomId ==
                            newItem.roomId
                }

                override fun areContentsTheSame(
                    oldItem: RoomCardResponse,
                    newItem: RoomCardResponse
                ): Boolean {

                    return oldItem ==
                            newItem
                }
            }
    }
}