package com.roomify.app.data.repository

import com.roomify.app.data.remote.api.LandlordRentalRequestApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.common.safeApiCall
import com.roomify.app.data.remote.dto.landlord.LandlordRentalRequestDecisionRequest
import com.roomify.app.data.remote.dto.rentals.RentalRequestListResponse
import com.roomify.app.data.remote.dto.rentals.RentalRequestResponse

class LandlordRentalRequestRepository(
    private val api:
    LandlordRentalRequestApi
) {

    suspend fun getRentalRequests(
        status: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): ApiResult<RentalRequestListResponse> {

        require(page >= 1)
        require(pageSize in 1..50)

        return safeApiCall {

            api.getRentalRequests(
                status =
                    normalizeStatus(status),
                page =
                    page,
                pageSize =
                    pageSize
            )
        }
    }

    suspend fun getDetail(
        rentalRequestId: Long
    ): ApiResult<RentalRequestResponse> {

        require(
            rentalRequestId > 0L
        )

        return safeApiCall {

            api.getRentalRequestDetail(
                rentalRequestId
            )
        }
    }

    suspend fun approve(
        rentalRequestId: Long,
        note: String? = null
    ): ApiResult<RentalRequestResponse> {

        require(
            rentalRequestId > 0L
        )

        return safeApiCall {

            api.approveRentalRequest(
                rentalRequestId =
                    rentalRequestId,
                request =
                    LandlordRentalRequestDecisionRequest(
                        note =
                            normalizeNote(note)
                    )
            )
        }
    }

    suspend fun reject(
        rentalRequestId: Long,
        reason: String
    ): ApiResult<RentalRequestResponse> {

        require(
            rentalRequestId > 0L
        )

        val cleanReason =
            reason.trim()

        require(
            cleanReason.isNotBlank()
        ) {
            "Vui lòng nhập lý do từ chối."
        }

        require(
            cleanReason.length <= 1500
        ) {
            "Ghi chú tối đa 1500 ký tự."
        }

        return safeApiCall {

            api.rejectRentalRequest(
                rentalRequestId =
                    rentalRequestId,
                request =
                    LandlordRentalRequestDecisionRequest(
                        note =
                            cleanReason
                    )
            )
        }
    }

    private fun normalizeStatus(
        status: String?
    ): String? {

        return status
            ?.trim()
            ?.uppercase()
            ?.takeIf {
                it.isNotBlank()
            }
    }

    private fun normalizeNote(
        note: String?
    ): String? {

        val clean =
            note
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        require(
            clean == null ||
                    clean.length <= 1500
        ) {
            "Ghi chú tối đa 1500 ký tự."
        }

        return clean
    }
}