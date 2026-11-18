package com.roomify.app.data.remote.dto.reviews

import com.google.gson.annotations.SerializedName

data class CreateReviewRequest(
    @SerializedName("rating")
    val rating: Int,

    @SerializedName("comment")
    val comment: String?
)

data class ReviewResponse(
    @SerializedName("reviewId")
    val reviewId: Long,

    @SerializedName("contractId")
    val contractId: Long,

    @SerializedName("roomId")
    val roomId: Long,

    @SerializedName("roomTitle")
    val roomTitle: String,

    @SerializedName("studentId")
    val studentId: Long,

    @SerializedName("studentName")
    val studentName: String,

    @SerializedName("rating")
    val rating: Int,

    @SerializedName("comment")
    val comment: String?,

    @SerializedName("status")
    val status: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)

data class ReviewListResponse(
    @SerializedName("items")
    val items: List<ReviewResponse> = emptyList(),

    @SerializedName("totalCount")
    val totalCount: Int,

    @SerializedName("averageRating")
    val averageRating: Double,

    @SerializedName("page")
    val page: Int,

    @SerializedName("pageSize")
    val pageSize: Int,

    @SerializedName("totalPages")
    val totalPages: Int,

    @SerializedName("hasNextPage")
    val hasNextPage: Boolean,

    @SerializedName("hasPreviousPage")
    val hasPreviousPage: Boolean
)