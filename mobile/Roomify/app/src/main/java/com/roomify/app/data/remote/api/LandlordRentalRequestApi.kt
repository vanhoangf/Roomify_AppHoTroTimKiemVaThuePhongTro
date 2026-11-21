package com.roomify.app.data.remote.api

import com.roomify.app.data.remote.dto.landlord.LandlordRentalRequestDecisionRequest
import com.roomify.app.data.remote.dto.rentals.RentalRequestListResponse
import com.roomify.app.data.remote.dto.rentals.RentalRequestResponse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface LandlordRentalRequestApi {

    @GET(
        "api/landlord/rental-requests"
    )
    suspend fun getRentalRequests(

        @Query("status")
        status: String? = null,

        @Query("page")
        page: Int = 1,

        @Query("pageSize")
        pageSize: Int = 20

    ): Response<RentalRequestListResponse>

    @GET(
        "api/landlord/rental-requests/{rentalRequestId}"
    )
    suspend fun getRentalRequestDetail(

        @Path("rentalRequestId")
        rentalRequestId: Long

    ): Response<RentalRequestResponse>

    @PATCH(
        "api/landlord/rental-requests/{rentalRequestId}/approve"
    )
    suspend fun approveRentalRequest(

        @Path("rentalRequestId")
        rentalRequestId: Long,

        @Body
        request:
        LandlordRentalRequestDecisionRequest

    ): Response<RentalRequestResponse>

    @PATCH(
        "api/landlord/rental-requests/{rentalRequestId}/reject"
    )
    suspend fun rejectRentalRequest(

        @Path("rentalRequestId")
        rentalRequestId: Long,

        @Body
        request:
        LandlordRentalRequestDecisionRequest

    ): Response<RentalRequestResponse>
}