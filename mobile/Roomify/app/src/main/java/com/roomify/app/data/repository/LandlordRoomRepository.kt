package com.roomify.app.data.repository

import com.roomify.app.data.remote.api.LandlordRoomApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.common.safeApiCall
import com.roomify.app.data.remote.dto.landlord.CreateRoomRequest
import com.roomify.app.data.remote.dto.landlord.RoomResponse
import com.roomify.app.data.remote.dto.landlord.UpdateRoomAmenitiesRequest
import com.roomify.app.data.remote.dto.landlord.UpdateRoomAvailabilityRequest
import com.roomify.app.data.remote.dto.landlord.UpdateRoomRequest

import java.util.Locale

class LandlordRoomRepository(
    private val roomApi: LandlordRoomApi
) {

    suspend fun getRooms(
        propertyId: Long
    ): ApiResult<List<RoomResponse>> {

        require(propertyId > 0L)

        return safeApiCall {
            roomApi.getRooms(
                propertyId
            )
        }
    }

    suspend fun getRoom(
        roomId: Long
    ): ApiResult<RoomResponse> {

        require(roomId > 0L)

        return safeApiCall {
            roomApi.getRoom(
                roomId
            )
        }
    }

    suspend fun createRoom(
        propertyId: Long,
        request: CreateRoomRequest
    ): ApiResult<RoomResponse> {

        require(propertyId > 0L)

        validateCreateRequest(
            request
        )

        return safeApiCall {
            roomApi.createRoom(
                propertyId,
                request
            )
        }
    }

    suspend fun updateRoom(
        roomId: Long,
        request: UpdateRoomRequest
    ): ApiResult<RoomResponse> {

        require(roomId > 0L)

        validateUpdateRequest(
            request
        )

        return safeApiCall {
            roomApi.updateRoom(
                roomId,
                request
            )
        }
    }

    suspend fun updateAmenities(
        roomId: Long,
        amenityIds: List<Int>
    ): ApiResult<RoomResponse> {

        require(roomId > 0L)

        require(
            amenityIds.all {
                it > 0
            }
        )

        return safeApiCall {

            roomApi.updateAmenities(
                roomId,
                UpdateRoomAmenitiesRequest(
                    amenityIds =
                        amenityIds.distinct()
                )
            )
        }
    }

    suspend fun updateAvailability(
        roomId: Long,
        status: String
    ): ApiResult<RoomResponse> {

        require(roomId > 0L)

        val normalized =
            status
                .trim()
                .uppercase(
                    Locale.ROOT
                )

        require(
            normalized in setOf(
                "AVAILABLE",
                "RESERVED",
                "RENTED"
            )
        )

        return safeApiCall {

            roomApi.updateAvailability(
                roomId,
                UpdateRoomAvailabilityRequest(
                    availabilityStatus =
                        normalized
                )
            )
        }
    }

    suspend fun submitForReview(
        roomId: Long
    ): ApiResult<RoomResponse> {

        require(roomId > 0L)

        return safeApiCall {
            roomApi.submitForReview(
                roomId
            )
        }
    }

    suspend fun hideRoom(
        roomId: Long
    ): ApiResult<RoomResponse> {

        require(roomId > 0L)

        return safeApiCall {
            roomApi.hideRoom(
                roomId
            )
        }
    }

    private fun validateCreateRequest(
        request: CreateRoomRequest
    ) {

        require(
            request.title.isNotBlank()
        )

        require(
            request.title.length <= 255
        )

        require(
            request.roomNumber
                ?.length
                    ?: 0 <= 50
        )

        require(
            request.areaM2 >
                    java.math.BigDecimal.ZERO
        )

        require(
            request.rentPrice >=
                    java.math.BigDecimal.ZERO
        )

        require(
            request.maxOccupants >= 1
        )

        validateGenderPreference(
            request.genderPreference
        )
    }

    private fun validateUpdateRequest(
        request: UpdateRoomRequest
    ) {

        require(
            request.title.isNotBlank()
        )

        require(
            request.title.length <= 255
        )

        require(
            request.roomNumber
                ?.length
                    ?: 0 <= 50
        )

        require(
            request.areaM2 >
                    java.math.BigDecimal.ZERO
        )

        require(
            request.rentPrice >=
                    java.math.BigDecimal.ZERO
        )

        require(
            request.maxOccupants >= 1
        )

        validateGenderPreference(
            request.genderPreference
        )
    }

    private fun validateGenderPreference(
        value: String
    ) {

        val normalized =
            value
                .trim()
                .uppercase(
                    Locale.ROOT
                )

        require(
            normalized in setOf(
                "ALL",
                "MALE",
                "FEMALE"
            )
        )
    }
}