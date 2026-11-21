package com.roomify.app.data.remote.api

import com.roomify.app.data.remote.dto.landlord.LandlordConfirmPaymentRequest
import com.roomify.app.data.remote.dto.landlord.LandlordCreatePaymentRequest
import com.roomify.app.data.remote.dto.landlord.LandlordRejectPaymentRequest
import com.roomify.app.data.remote.dto.payments.PaymentListResponse
import com.roomify.app.data.remote.dto.payments.PaymentResponse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LandlordPaymentApi {

    @POST(
        "api/landlord/contracts/{contractId}/payments"
    )
    suspend fun createPayment(
        @Path("contractId")
        contractId: Long,

        @Body
        request: LandlordCreatePaymentRequest

    ): Response<PaymentResponse>

    @GET(
        "api/landlord/payments"
    )
    suspend fun getPayments(

        @Query("status")
        status: String? = null,

        @Query("paymentType")
        paymentType: String? = null,

        @Query("page")
        page: Int = 1,

        @Query("pageSize")
        pageSize: Int = 20

    ): Response<PaymentListResponse>

    @GET(
        "api/landlord/payments/{paymentId}"
    )
    suspend fun getPaymentDetail(

        @Path("paymentId")
        paymentId: Long

    ): Response<PaymentResponse>

    @PATCH(
        "api/landlord/payments/{paymentId}/confirm"
    )
    suspend fun confirmPayment(

        @Path("paymentId")
        paymentId: Long,

        @Body
        request: LandlordConfirmPaymentRequest

    ): Response<PaymentResponse>

    @PATCH(
        "api/landlord/payments/{paymentId}/reject"
    )
    suspend fun rejectPayment(

        @Path("paymentId")
        paymentId: Long,

        @Body
        request: LandlordRejectPaymentRequest

    ): Response<PaymentResponse>

    @PATCH(
        "api/landlord/payments/{paymentId}/cancel"
    )
    suspend fun cancelPayment(

        @Path("paymentId")
        paymentId: Long

    ): Response<PaymentResponse>
}