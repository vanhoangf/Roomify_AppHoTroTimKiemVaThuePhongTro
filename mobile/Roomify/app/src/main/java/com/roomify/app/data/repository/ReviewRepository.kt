package com.roomify.app.data.repository

import com.roomify.app.data.remote.api.ReviewApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.common.safeApiCall
import com.roomify.app.data.remote.dto.reviews.CreateReviewRequest
import com.roomify.app.data.remote.dto.reviews.ReviewListResponse
import com.roomify.app.data.remote.dto.reviews.ReviewResponse

class ReviewRepository(
    private val api: ReviewApi
) {

    suspend fun createReview(
        contractId: Long,
        rating: Int,
        comment: String?
    ): ApiResult<ReviewResponse> {

        require(contractId > 0)
        require(rating in 1..5)

        return safeApiCall {
            api.createReview(
                contractId,
                CreateReviewRequest(
                    rating = rating,
                    comment = comment
                )
            )
        }
    }

    suspend fun getMyReview(
        contractId: Long
    ): ApiResult<ReviewResponse> {

        return safeApiCall {
            api.getMyReview(contractId)
        }
    }

    suspend fun getRoomReviews(
        roomId: Long
    ): ApiResult<ReviewListResponse> {

        return safeApiCall {
            api.getRoomReviews(
                roomId = roomId,
                page = 1,
                pageSize = 20
            )
        }
    }
}