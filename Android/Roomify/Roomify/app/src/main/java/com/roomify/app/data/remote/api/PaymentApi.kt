package com.roomify.app.data.remote.api

import com.roomify.app.data.remote.dto.payments.PaymentListResponse
import com.roomify.app.data.remote.dto.payments.PaymentResponse
import com.roomify.app.data.remote.dto.payments.SubmitPaymentRequest

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface PaymentApi {

    @GET("api/student/payments")
    suspend fun getMyPayments(
        @Query("status")
        status: String? = null,

        @Query("paymentType")
        paymentType: String? = null,

        @Query("page")
        page: Int = 1,

        @Query("pageSize")
        pageSize: Int = 20
    ): Response<PaymentListResponse>

    @GET("api/student/payments/{paymentId}")
    suspend fun getPaymentDetail(
        @Path("paymentId")
        paymentId: Long
    ): Response<PaymentResponse>

    @PATCH("api/student/payments/{paymentId}/submit")
    suspend fun submitPayment(
        @Path("paymentId")
        paymentId: Long,

        @Body
        request: SubmitPaymentRequest
    ): Response<PaymentResponse>

    @PATCH("api/student/payments/{paymentId}/cancel")
    suspend fun cancelPayment(
        @Path("paymentId")
        paymentId: Long
    ): Response<PaymentResponse>
}