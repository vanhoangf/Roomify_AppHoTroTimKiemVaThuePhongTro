package com.roomify.app.data.remote.api

import com.roomify.app.data.remote.dto.landlord.CreateRoomRequest
import com.roomify.app.data.remote.dto.landlord.RoomResponse
import com.roomify.app.data.remote.dto.landlord.UpdateRoomAmenitiesRequest
import com.roomify.app.data.remote.dto.landlord.UpdateRoomAvailabilityRequest
import com.roomify.app.data.remote.dto.landlord.UpdateRoomRequest

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface LandlordRoomApi {

    @GET("api/landlord/properties/{propertyId}/rooms")
    suspend fun getRooms(
        @Path("propertyId")
        propertyId: Long
    ): Response<List<RoomResponse>>

    @POST("api/landlord/properties/{propertyId}/rooms")
    suspend fun createRoom(
        @Path("propertyId")
        propertyId: Long,

        @Body
        request: CreateRoomRequest
    ): Response<RoomResponse>

    @GET("api/landlord/rooms/{roomId}")
    suspend fun getRoom(
        @Path("roomId")
        roomId: Long
    ): Response<RoomResponse>

    @PUT("api/landlord/rooms/{roomId}")
    suspend fun updateRoom(
        @Path("roomId")
        roomId: Long,

        @Body
        request: UpdateRoomRequest
    ): Response<RoomResponse>

    @PUT("api/landlord/rooms/{roomId}/amenities")
    suspend fun updateAmenities(
        @Path("roomId")
        roomId: Long,

        @Body
        request: UpdateRoomAmenitiesRequest
    ): Response<RoomResponse>

    @PATCH("api/landlord/rooms/{roomId}/availability")
    suspend fun updateAvailability(
        @Path("roomId")
        roomId: Long,

        @Body
        request: UpdateRoomAvailabilityRequest
    ): Response<RoomResponse>

    @POST("api/landlord/rooms/{roomId}/submit-review")
    suspend fun submitForReview(
        @Path("roomId")
        roomId: Long
    ): Response<RoomResponse>

    @POST("api/landlord/rooms/{roomId}/hide")
    suspend fun hideRoom(
        @Path("roomId")
        roomId: Long
    ): Response<RoomResponse>
}