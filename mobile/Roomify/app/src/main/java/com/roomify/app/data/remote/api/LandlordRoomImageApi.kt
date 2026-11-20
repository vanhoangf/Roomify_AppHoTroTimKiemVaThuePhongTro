package com.roomify.app.data.remote.api

import com.roomify.app.data.remote.dto.landlord.LandlordReorderRoomImagesRequest
import com.roomify.app.data.remote.dto.landlord.LandlordRoomImageResponse

import okhttp3.MultipartBody

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface LandlordRoomImageApi {

    @GET(
        "api/landlord/rooms/{roomId}/images"
    )
    suspend fun getImages(
        @Path("roomId")
        roomId: Long
    ): Response<List<LandlordRoomImageResponse>>

    @Multipart
    @POST(
        "api/landlord/rooms/{roomId}/images"
    )
    suspend fun uploadImages(
        @Path("roomId")
        roomId: Long,

        @Part
        files: List<MultipartBody.Part>
    ): Response<List<LandlordRoomImageResponse>>

    @PATCH(
        "api/landlord/rooms/{roomId}/images/{imageId}/primary"
    )
    suspend fun setPrimary(
        @Path("roomId")
        roomId: Long,

        @Path("imageId")
        imageId: Long
    ): Response<LandlordRoomImageResponse>

    @PUT(
        "api/landlord/rooms/{roomId}/images/order"
    )
    suspend fun reorderImages(
        @Path("roomId")
        roomId: Long,

        @Body
        request: LandlordReorderRoomImagesRequest
    ): Response<List<LandlordRoomImageResponse>>

    @DELETE(
        "api/landlord/rooms/{roomId}/images/{imageId}"
    )
    suspend fun deleteImage(
        @Path("roomId")
        roomId: Long,

        @Path("imageId")
        imageId: Long
    ): Response<Unit>
}