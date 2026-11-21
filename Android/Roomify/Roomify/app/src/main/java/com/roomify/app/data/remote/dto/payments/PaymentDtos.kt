package com.roomify.app.data.remote.dto.payments

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class PaymentResponse(

    @SerializedName("paymentId")
    val paymentId: Long,

    @SerializedName("contractId")
    val contractId: Long,

    @SerializedName("contractCode")
    val contractCode: String,

    @SerializedName("roomId")
    val roomId: Long,

    @SerializedName("roomTitle")
    val roomTitle: String,

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

    @SerializedName("paymentType")
    val paymentType: String,

    @SerializedName("amount")
    val amount: BigDecimal,

    @SerializedName("dueDate")
    val dueDate: String?,

    @SerializedName("paymentMethod")
    val paymentMethod: String?,

    @SerializedName("status")
    val status: String,

    @SerializedName("transactionReference")
    val transactionReference: String?,

    @SerializedName("proofImageUrl")
    val proofImageUrl: String?,

    @SerializedName("note")
    val note: String?,

    @SerializedName("confirmedByUserId")
    val confirmedByUserId: Long?,

    @SerializedName("confirmedByUserName")
    val confirmedByUserName: String?,

    @SerializedName("confirmedAt")
    val confirmedAt: String?,

    @SerializedName("paidAt")
    val paidAt: String?,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)

data class PaymentListResponse(

    @SerializedName("items")
    val items: List<PaymentResponse> = emptyList(),

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

data class SubmitPaymentRequest(

    @SerializedName("paymentMethod")
    val paymentMethod: String,

    @SerializedName("transactionReference")
    val transactionReference: String?,

    @SerializedName("proofImageUrl")
    val proofImageUrl: String?,

    @SerializedName("note")
    val note: String?
)