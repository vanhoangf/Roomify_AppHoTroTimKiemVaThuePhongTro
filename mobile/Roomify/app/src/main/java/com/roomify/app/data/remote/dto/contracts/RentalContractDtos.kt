package com.roomify.app.data.remote.dto.contracts

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class RentalContractResponse(

    @SerializedName("contractId")
    val contractId: Long,

    @SerializedName("rentalRequestId")
    val rentalRequestId: Long,

    @SerializedName("contractCode")
    val contractCode: String,

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

    @SerializedName("landlordId")
    val landlordId: Long,

    @SerializedName("landlordName")
    val landlordName: String,

    @SerializedName("startDate")
    val startDate: String,

    @SerializedName("endDate")
    val endDate: String?,

    @SerializedName("monthlyRent")
    val monthlyRent: BigDecimal,

    @SerializedName("depositAmount")
    val depositAmount: BigDecimal,

    @SerializedName("terms")
    val terms: String?,

    @SerializedName("contractFileUrl")
    val contractFileUrl: String?,

    @SerializedName("status")
    val status: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("activatedAt")
    val activatedAt: String?,

    @SerializedName("completedAt")
    val completedAt: String?,

    @SerializedName("cancelledAt")
    val cancelledAt: String?
)

data class RentalContractListResponse(

    @SerializedName("items")
    val items: List<RentalContractResponse> = emptyList(),

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