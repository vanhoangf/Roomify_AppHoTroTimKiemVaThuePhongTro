package com.roomify.app.ui.student

import android.os.Bundle
import android.view.View
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope

import com.roomify.app.data.remote.api.ReviewApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.reviews.ReviewResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.ReviewRepository
import com.roomify.app.databinding.ActivityCreateReviewBinding

import kotlinx.coroutines.launch

class CreateReviewActivity :
    AppCompatActivity() {

    companion object {

        const val EXTRA_CONTRACT_ID =
            "extra_contract_id"

        const val EXTRA_ROOM_TITLE =
            "extra_room_title"
    }

    private lateinit var binding:
            ActivityCreateReviewBinding

    private lateinit var repository:
            ReviewRepository

    private var contractId =
        -1L

    private var isSubmitting =
        false

    private fun setupRatingBar() {

        binding.ratingBar.numStars = 5

        binding.ratingBar.stepSize = 1f

        binding.ratingBar.rating = 0f
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityCreateReviewBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        setupRatingBar()

        contractId =
            intent.getLongExtra(
                EXTRA_CONTRACT_ID,
                -1L
            )

        binding.tvRoomTitle.text =
            intent.getStringExtra(
                EXTRA_ROOM_TITLE
            ) ?: "Phòng Roomify"

        setupInsets()
        setupRepository()
        setupListeners()

        checkExistingReview()
    }

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.reviewRoot
        ) { view, insets ->

            val bars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            view.setPadding(
                bars.left,
                bars.top,
                bars.right,
                bars.bottom
            )

            insets
        }
    }

    private fun setupRepository() {

        RetrofitClient.initialize(
            applicationContext
        )

        repository =
            ReviewRepository(
                RetrofitClient.createService(
                    ReviewApi::class.java
                )
            )
    }

    private fun setupListeners() {

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.ratingBar
            .setOnRatingBarChangeListener {
                    _,
                    rating,
                    _ ->

                binding.tvRatingLabel.text =
                    when (rating.toInt()) {

                        1 -> "Rất không hài lòng"
                        2 -> "Chưa hài lòng"
                        3 -> "Bình thường"
                        4 -> "Hài lòng"
                        5 -> "Rất hài lòng"

                        else ->
                            "Chọn từ 1 đến 5 sao"
                    }
            }

        binding.btnSubmit.setOnClickListener {
            submitReview()
        }
    }

    private fun checkExistingReview() {

        if (contractId <= 0L) {
            finish()
            return
        }

        lifecycleScope.launch {

            when (
                val result =
                    repository.getMyReview(
                        contractId
                    )
            ) {

                is ApiResult.Success -> {

                    result.data?.let {
                        showExistingReview(it)
                    }
                }

                /*
                 * Nếu chưa có review, backend trả NOT_FOUND.
                 * Khi đó giữ form để Student tạo mới.
                 */
                is ApiResult.Error ->
                    Unit

                ApiResult.Loading ->
                    Unit
            }
        }
    }

    private fun showExistingReview(
        review: ReviewResponse
    ) {

        binding.ratingBar.rating =
            review.rating.toFloat()

        binding.ratingBar.isEnabled =
            false

        binding.edtComment.setText(
            review.comment.orEmpty()
        )

        binding.edtComment.isEnabled =
            false

        binding.tvExistingReview.visibility =
            View.VISIBLE

        binding.tvExistingReview.text =
            "Bạn đã đánh giá hợp đồng này. " +
                    "Roomify hiện không hỗ trợ sửa đánh giá."

        binding.actionContainer.visibility =
            View.GONE
    }

    private fun submitReview() {

        if (isSubmitting) {
            return
        }

        val rating =
            binding.ratingBar.rating
                .toInt()

        if (rating !in 1..5) {

            Toast.makeText(
                this,
                "Vui lòng chọn số sao đánh giá.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val comment =
            binding.edtComment.text
                ?.toString()
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        isSubmitting =
            true

        binding.btnSubmit.isEnabled =
            false

        binding.btnSubmit.text =
            "Đang gửi..."

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.createReview(
                            contractId =
                                contractId,
                            rating =
                                rating,
                            comment =
                                comment
                        )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@CreateReviewActivity,
                            "Cảm ơn bạn đã đánh giá.",
                            Toast.LENGTH_LONG
                        ).show()

                        result.data?.let {
                            showExistingReview(it)
                        }
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@CreateReviewActivity,
                            result.exception.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } finally {

                isSubmitting =
                    false

                binding.btnSubmit.isEnabled =
                    true

                binding.btnSubmit.text =
                    "Gửi đánh giá"
            }
        }
    }
}