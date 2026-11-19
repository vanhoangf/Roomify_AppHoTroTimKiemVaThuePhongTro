package com.roomify.app.data.repository

import com.roomify.app.data.remote.api.PaymentApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.common.safeApiCall
import com.roomify.app.data.remote.dto.payments.PaymentListResponse
import com.roomify.app.data.remote.dto.payments.PaymentResponse
import com.roomify.app.data.remote.dto.payments.SubmitPaymentRequest

class PaymentRepository(
    private val api: PaymentApi
) {

    suspend fun getMyPayments(
        page: Int = 1,
        pageSize: Int = 20
    ): ApiResult<PaymentListResponse> {

        return safeApiCall {
            api.getMyPayments(
                page = page,
                pageSize = pageSize
            )
        }
    }

    suspend fun getDetail(
        paymentId: Long
    ): ApiResult<PaymentResponse> {

        require(paymentId > 0)

        return safeApiCall {
            api.getPaymentDetail(
                paymentId
            )
        }
    }

    suspend fun submit(
        paymentId: Long,
        request: SubmitPaymentRequest
    ): ApiResult<PaymentResponse> {

        require(paymentId > 0)

        return safeApiCall {
            api.submitPayment(
                paymentId,
                request
            )
        }
    }

    suspend fun cancel(
        paymentId: Long
    ): ApiResult<PaymentResponse> {

        require(paymentId > 0)

        return safeApiCall {
            api.cancelPayment(
                paymentId
            )
        }
    }
}