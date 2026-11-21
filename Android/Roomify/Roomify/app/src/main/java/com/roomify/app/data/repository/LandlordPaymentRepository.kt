package com.roomify.app.data.repository

import com.roomify.app.data.remote.api.LandlordPaymentApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.common.safeApiCall
import com.roomify.app.data.remote.dto.landlord.LandlordConfirmPaymentRequest
import com.roomify.app.data.remote.dto.landlord.LandlordCreatePaymentRequest
import com.roomify.app.data.remote.dto.landlord.LandlordRejectPaymentRequest
import com.roomify.app.data.remote.dto.payments.PaymentListResponse
import com.roomify.app.data.remote.dto.payments.PaymentResponse

import java.math.BigDecimal
import java.util.Locale

class LandlordPaymentRepository(
    private val api:
    LandlordPaymentApi
) {

    suspend fun create(
        contractId: Long,
        paymentType: String,
        amount: BigDecimal,
        dueDate: String?,
        note: String?
    ): ApiResult<PaymentResponse> {

        require(contractId > 0L)

        require(
            amount > BigDecimal.ZERO
        ) {
            "Số tiền phải lớn hơn 0."
        }

        return safeApiCall {

            api.createPayment(
                contractId =
                    contractId,
                request =
                    LandlordCreatePaymentRequest(
                        paymentType =
                            paymentType
                                .trim()
                                .uppercase(
                                    Locale.ROOT
                                ),
                        amount =
                            amount,
                        dueDate =
                            normalize(
                                dueDate
                            ),
                        note =
                            normalize(
                                note
                            )
                    )
            )
        }
    }

    suspend fun getPayments(
        status: String? = null,
        paymentType: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): ApiResult<PaymentListResponse> {

        require(page >= 1)
        require(pageSize in 1..50)

        return safeApiCall {

            api.getPayments(
                status =
                    normalizeUpper(
                        status
                    ),
                paymentType =
                    normalizeUpper(
                        paymentType
                    ),
                page =
                    page,
                pageSize =
                    pageSize
            )
        }
    }

    suspend fun getDetail(
        paymentId: Long
    ): ApiResult<PaymentResponse> {

        require(paymentId > 0L)

        return safeApiCall {

            api.getPaymentDetail(
                paymentId
            )
        }
    }

    suspend fun confirm(
        paymentId: Long,
        note: String?
    ): ApiResult<PaymentResponse> {

        require(paymentId > 0L)

        val clean =
            normalize(
                note
            )

        require(
            clean == null ||
                    clean.length <= 1000
        )

        return safeApiCall {

            api.confirmPayment(
                paymentId =
                    paymentId,
                request =
                    LandlordConfirmPaymentRequest(
                        note =
                            clean
                    )
            )
        }
    }

    suspend fun reject(
        paymentId: Long,
        reason: String
    ): ApiResult<PaymentResponse> {

        require(paymentId > 0L)

        val clean =
            reason.trim()

        require(
            clean.isNotBlank()
        ) {
            "Vui lòng nhập lý do từ chối."
        }

        require(
            clean.length <= 1000
        ) {
            "Lý do tối đa 1000 ký tự."
        }

        return safeApiCall {

            api.rejectPayment(
                paymentId =
                    paymentId,
                request =
                    LandlordRejectPaymentRequest(
                        note =
                            clean
                    )
            )
        }
    }

    suspend fun cancel(
        paymentId: Long
    ): ApiResult<PaymentResponse> {

        require(paymentId > 0L)

        return safeApiCall {

            api.cancelPayment(
                paymentId
            )
        }
    }

    private fun normalize(
        value: String?
    ): String? {

        return value
            ?.trim()
            ?.takeIf {
                it.isNotBlank()
            }
    }

    private fun normalizeUpper(
        value: String?
    ): String? {

        return normalize(
            value
        )?.uppercase(
            Locale.ROOT
        )
    }
}