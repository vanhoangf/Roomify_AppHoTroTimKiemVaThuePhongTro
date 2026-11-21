package com.roomify.app.data.remote.api

import com.roomify.app.data.remote.dto.reviews.CreateReviewRequest
import com.roomify.app.data.remote.dto.reviews.ReviewListResponse
import com.roomify.app.data.remote.dto.reviews.ReviewResponse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ReviewApi {

    @POST("api/student/contracts/{contractId}/review")
    suspend fun createReview(
        @Path("contractId")
        contractId: Long,

        @Body
        request: CreateReviewRequest
    ): Response<ReviewResponse>

    @GET("api/student/contracts/{contractId}/review")
    suspend fun getMyReview(
        @Path("contractId")
        contractId: Long
    ): Response<ReviewResponse>

    @GET("api/rooms/{roomId}/reviews")
    suspend fun getRoomReviews(
        @Path("roomId")
        roomId: Long,

        @Query("page")
        page: Int = 1,

        @Query("pageSize")
        pageSize: Int = 20
    ): Response<ReviewListResponse>
}