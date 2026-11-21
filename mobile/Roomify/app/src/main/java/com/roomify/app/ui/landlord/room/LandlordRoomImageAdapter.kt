package com.roomify.app.ui.landlord.room

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView

import com.bumptech.glide.Glide
import com.roomify.app.R
import com.roomify.app.data.remote.dto.landlord.LandlordRoomImageResponse
import com.roomify.app.databinding.ItemLandlordRoomImageBinding

class LandlordRoomImageAdapter(
    private val onSetPrimary:
        (LandlordRoomImageResponse) -> Unit,

    private val onDelete:
        (LandlordRoomImageResponse) -> Unit
) : RecyclerView.Adapter<
        LandlordRoomImageAdapter.RoomImageViewHolder
        >() {

    private val items =
        mutableListOf<LandlordRoomImageResponse>()

    fun submitImages(
        images: List<LandlordRoomImageResponse>
    ) {

        items.clear()
        items.addAll(images)

        notifyDataSetChanged()
    }

    fun getImages():
            List<LandlordRoomImageResponse> {

        return items.toList()
    }

    fun moveImage(
        fromPosition: Int,
        toPosition: Int
    ): Boolean {

        if (
            fromPosition !in items.indices ||
            toPosition !in items.indices
        ) {
            return false
        }

        // Ảnh đại diện luôn giữ vị trí đầu.
        if (
            items[fromPosition].isPrimary ||
            toPosition == 0
        ) {
            return false
        }

        val item =
            items.removeAt(
                fromPosition
            )

        items.add(
            toPosition,
            item
        )

        notifyItemMoved(
            fromPosition,
            toPosition
        )

        return true
    }

    override fun getItemCount():
            Int =
        items.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RoomImageViewHolder {

        val binding =
            ItemLandlordRoomImageBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        return RoomImageViewHolder(
            binding,
            onSetPrimary,
            onDelete
        )
    }

    override fun onBindViewHolder(
        holder: RoomImageViewHolder,
        position: Int
    ) {

        holder.bind(
            items[position],
            position
        )
    }

    class RoomImageViewHolder(
        private val binding:
        ItemLandlordRoomImageBinding,

        private val onSetPrimary:
            (LandlordRoomImageResponse) -> Unit,

        private val onDelete:
            (LandlordRoomImageResponse) -> Unit
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            image: LandlordRoomImageResponse,
            position: Int
        ) {

            Glide.with(
                binding.ivLandlordRoomImage
            )
                .load(
                    image.imageUrl
                )
                .placeholder(
                    R.drawable.room_image_placeholder
                )
                .error(
                    R.drawable.room_image_placeholder
                )
                .centerCrop()
                .into(
                    binding.ivLandlordRoomImage
                )

            binding.tvLandlordPrimaryBadge
                .isVisible =
                image.isPrimary

            binding.tvLandlordImageOrder.text =
                "#${position + 1}"

            binding.btnLandlordSetPrimary
                .isVisible =
                !image.isPrimary

            binding.btnLandlordSetPrimary
                .setOnClickListener {

                    onSetPrimary(
                        image
                    )
                }

            binding.btnLandlordDeleteImage
                .setOnClickListener {

                    onDelete(
                        image
                    )
                }
        }
    }
}