package com.roomify.app.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.bumptech.glide.Glide

import com.roomify.app.R
import com.roomify.app.data.remote.dto.savedrooms.SavedRoomResponse
import com.roomify.app.databinding.ItemSavedRoomBinding

import java.text.NumberFormat
import java.util.Locale

class SavedRoomsAdapter(
    private val onDetail:
        (SavedRoomResponse) -> Unit,

    private val onRemove:
        (SavedRoomResponse) -> Unit
) : ListAdapter<
        SavedRoomResponse,
        SavedRoomsAdapter.SavedRoomViewHolder
        >(DIFF) {

    private val money =
        NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("vi-VN")
        )

    inner class SavedRoomViewHolder(
        private val binding:
        ItemSavedRoomBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            room: SavedRoomResponse
        ) {

            Glide.with(
                binding.ivRoom
            )
                .load(
                    room.thumbnailUrl
                )
                .placeholder(
                    R.drawable.room_image_placeholder
                )
                .error(
                    R.drawable.room_image_placeholder
                )
                .centerCrop()
                .into(
                    binding.ivRoom
                )

            binding.tvTitle.text =
                room.title

            binding.tvPropertyName.text =
                room.propertyName

            binding.tvAddress.text =
                listOf(
                    room.addressLine,
                    room.wardName,
                    room.provinceName
                )
                    .filter {
                        it.isNotBlank()
                    }
                    .joinToString(", ")

            binding.tvArea.text =
                "Diện tích: ${
                    room.areaM2
                        .stripTrailingZeros()
                        .toPlainString()
                } m²"

            binding.tvPrice.text =
                "${money.format(room.rentPrice)}/tháng"

            val status =
                room.availabilityStatus
                    .uppercase(Locale.ROOT)

            binding.chipStatus.text =
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

            val color =
                when (status) {

                    "AVAILABLE" ->
                        R.color.status_success

                    "RENTED" ->
                        R.color.status_error

                    else ->
                        R.color.text_muted
                }

            binding.chipStatus
                .setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        color
                    )
                )

            binding.btnDetail
                .setOnClickListener {
                    onDetail(room)
                }

            binding.btnRemove
                .setOnClickListener {
                    onRemove(room)
                }

            binding.root
                .setOnClickListener {
                    onDetail(room)
                }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SavedRoomViewHolder {

        return SavedRoomViewHolder(
            ItemSavedRoomBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: SavedRoomViewHolder,
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
                        SavedRoomResponse
                        >() {

                override fun areItemsTheSame(
                    oldItem: SavedRoomResponse,
                    newItem: SavedRoomResponse
                ): Boolean {

                    return oldItem.roomId ==
                            newItem.roomId
                }

                override fun areContentsTheSame(
                    oldItem: SavedRoomResponse,
                    newItem: SavedRoomResponse
                ): Boolean {

                    return oldItem ==
                            newItem
                }
            }
    }
}