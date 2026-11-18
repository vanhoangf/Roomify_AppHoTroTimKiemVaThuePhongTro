package com.roomify.app.data.remote.api

import com.roomify.app.data.remote.dto.rentals.CreateRentalRequestRequest
import com.roomify.app.data.remote.dto.rentals.RentalRequestListResponse
import com.roomify.app.data.remote.dto.rentals.RentalRequestResponse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RentalRequestApi {

    @POST("api/student/rooms/{roomId}/rental-requests")
    suspend fun createRentalRequest(
        @Path("roomId")
        roomId: Long,

        @Body
        request: CreateRentalRequestRequest
    ): Response<RentalRequestResponse>

    @GET("api/student/rental-requests")
    suspend fun getMyRentalRequests(
        @Query("page")
        page: Int = 1,

        @Query("pageSize")
        pageSize: Int = 20
    ): Response<RentalRequestListResponse>

    @PATCH("api/student/rental-requests/{rentalRequestId}/cancel")
    suspend fun cancelRentalRequest(
        @Path("rentalRequestId")
        rentalRequestId: Long
    ): Response<RentalRequestResponse>
}