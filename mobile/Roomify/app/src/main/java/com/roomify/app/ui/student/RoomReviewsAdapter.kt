package com.roomify.app.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.roomify.app.data.remote.dto.reviews.ReviewResponse
import com.roomify.app.databinding.ItemRoomReviewBinding

class RoomReviewsAdapter :
    ListAdapter<
            ReviewResponse,
            RoomReviewsAdapter.ViewHolder
            >(DIFF) {

    inner class ViewHolder(
        private val binding:
        ItemRoomReviewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: ReviewResponse
        ) {

            binding.tvStudentName.text =
                item.studentName

            binding.ratingBar.rating =
                item.rating.toFloat()

            binding.tvComment.text =
                item.comment
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: "Không có nhận xét."

            binding.tvDate.text =
                item.createdAt
                    .take(10)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(
            ItemRoomReviewBinding.inflate(
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
                        ReviewResponse
                        >() {

                override fun areItemsTheSame(
                    oldItem: ReviewResponse,
                    newItem: ReviewResponse
                ) =
                    oldItem.reviewId ==
                            newItem.reviewId

                override fun areContentsTheSame(
                    oldItem: ReviewResponse,
                    newItem: ReviewResponse
                ) =
                    oldItem == newItem
            }
    }
}