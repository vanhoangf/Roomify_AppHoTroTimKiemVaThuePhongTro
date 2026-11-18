package com.roomify.app.data.repository

import com.roomify.app.data.remote.api.SavedRoomApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.common.safeApiCall
import com.roomify.app.data.remote.dto.savedrooms.SavedRoomResponse

class SavedRoomRepository(
    private val api: SavedRoomApi
) {

    suspend fun getSavedRooms():
            ApiResult<List<SavedRoomResponse>> {

        return safeApiCall {
            api.getSavedRooms()
        }
    }

    suspend fun saveRoom(
        roomId: Long
    ): ApiResult<SavedRoomResponse> {

        require(roomId > 0)

        return safeApiCall {
            api.saveRoom(roomId)
        }
    }

    suspend fun deleteSavedRoom(
        roomId: Long
    ): ApiResult<Unit> {

        require(roomId > 0)

        return safeApiCall {
            api.deleteSavedRoom(roomId)
        }
    }
}