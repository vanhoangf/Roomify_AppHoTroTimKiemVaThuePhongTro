package com.roomify.app.data.repository

import com.roomify.app.data.remote.api.RentalRequestApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.common.safeApiCall
import com.roomify.app.data.remote.dto.rentals.CreateRentalRequestRequest
import com.roomify.app.data.remote.dto.rentals.RentalRequestListResponse
import com.roomify.app.data.remote.dto.rentals.RentalRequestResponse

class RentalRequestRepository(
    private val api: RentalRequestApi
) {

    suspend fun create(
        roomId: Long,
        request: CreateRentalRequestRequest
    ): ApiResult<RentalRequestResponse> {

        require(roomId > 0)

        return safeApiCall {
            api.createRentalRequest(
                roomId,
                request
            )
        }
    }

    suspend fun getMyRentalRequests(
        page: Int = 1,
        pageSize: Int = 20
    ): ApiResult<RentalRequestListResponse> {

        require(page >= 1)
        require(pageSize in 1..50)

        return safeApiCall {
            api.getMyRentalRequests(
                page,
                pageSize
            )
        }
    }

    suspend fun cancel(
        rentalRequestId: Long
    ): ApiResult<RentalRequestResponse> {

        require(rentalRequestId > 0)

        return safeApiCall {
            api.cancelRentalRequest(
                rentalRequestId
            )
        }
    }
}