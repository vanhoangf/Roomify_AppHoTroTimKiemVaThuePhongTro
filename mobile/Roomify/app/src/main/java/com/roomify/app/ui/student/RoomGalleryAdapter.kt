package com.roomify.app.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView

import com.bumptech.glide.Glide
import com.roomify.app.R
import com.roomify.app.databinding.ItemRoomGalleryImageBinding

class RoomGalleryAdapter(
    private val imageUrls: List<String>
) : RecyclerView.Adapter<
        RoomGalleryAdapter.GalleryViewHolder
        >() {

    inner class GalleryViewHolder(
        private val binding:
        ItemRoomGalleryImageBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            imageUrl: String,
            position: Int
        ) {

            binding.ivGalleryImage
                .contentDescription =
                "Ảnh phòng ${position + 1}"

            Glide.with(
                binding.ivGalleryImage
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
                .fitCenter()
                .into(
                    binding.ivGalleryImage
                )
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GalleryViewHolder {

        val binding =
            ItemRoomGalleryImageBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        binding.root.layoutParams =
            RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

        return GalleryViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(
        holder: GalleryViewHolder,
        position: Int
    ) {

        holder.bind(
            imageUrls[position],
            position
        )
    }

    override fun getItemCount():
            Int {

        return imageUrls.size
    }
}