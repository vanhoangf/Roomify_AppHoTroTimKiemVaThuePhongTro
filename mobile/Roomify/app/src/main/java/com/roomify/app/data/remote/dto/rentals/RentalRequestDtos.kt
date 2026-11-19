package com.roomify.app.data.remote.dto.rentals

import com.google.gson.annotations.SerializedName

data class CreateRentalRequestRequest(
    @SerializedName("requestedMoveInDate")
    val requestedMoveInDate: String,

    @SerializedName("durationMonths")
    val durationMonths: Int?,

    @SerializedName("note")
    val note: String?
)

data class RentalRequestResponse(
    @SerializedName("rentalRequestId")
    val rentalRequestId: Long,

    @SerializedName("roomId")
    val roomId: Long,

    @SerializedName("roomTitle")
    val roomTitle: String,

    @SerializedName("propertyId")
    val propertyId: Long,

    @SerializedName("propertyName")
    val propertyName: String,

    @SerializedName("studentId")
    val studentId: Long,

    @SerializedName("studentName")
    val studentName: String,

    @SerializedName("studentPhone")
    val studentPhone: String?,

    @SerializedName("requestedMoveInDate")
    val requestedMoveInDate: String,

    @SerializedName("durationMonths")
    val durationMonths: Int?,

    @SerializedName("note")
    val note: String?,

    @SerializedName("status")
    val status: String,

    @SerializedName("landlordNote")
    val landlordNote: String?,

    @SerializedName("respondedAt")
    val respondedAt: String?,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)

data class RentalRequestListResponse(
    @SerializedName("items")
    val items: List<RentalRequestResponse> = emptyList(),

    @SerializedName("totalCount")
    val totalCount: Int,

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