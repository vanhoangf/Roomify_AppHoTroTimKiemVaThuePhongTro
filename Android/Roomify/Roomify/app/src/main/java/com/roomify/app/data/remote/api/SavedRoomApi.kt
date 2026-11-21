package com.roomify.app.data.remote.api

import com.roomify.app.data.remote.dto.savedrooms.SavedRoomResponse

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SavedRoomApi {

    @GET("api/student/saved-rooms")
    suspend fun getSavedRooms():
            Response<List<SavedRoomResponse>>

    @POST("api/student/saved-rooms/{roomId}")
    suspend fun saveRoom(
        @Path("roomId")
        roomId: Long
    ): Response<SavedRoomResponse>

    @DELETE("api/student/saved-rooms/{roomId}")
    suspend fun deleteSavedRoom(
        @Path("roomId")
        roomId: Long
    ): Response<Unit>
}