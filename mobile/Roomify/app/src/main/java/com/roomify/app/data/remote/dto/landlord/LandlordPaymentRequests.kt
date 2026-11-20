package com.roomify.app.data.remote.dto.landlord

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class LandlordCreatePaymentRequest(

    @SerializedName("paymentType")
    val paymentType: String,

    @SerializedName("amount")
    val amount: BigDecimal,

    @SerializedName("dueDate")
    val dueDate: String?,

    @SerializedName("note")
    val note: String?
)

data class LandlordConfirmPaymentRequest(

    @SerializedName("note")
    val note: String?
)

data class LandlordRejectPaymentRequest(

    @SerializedName("note")
    val note: String
)