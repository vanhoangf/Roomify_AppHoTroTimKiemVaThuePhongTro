package com.roomify.app.data.repository

import com.roomify.app.data.remote.api.RoomApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.common.safeApiCall
import com.roomify.app.data.remote.dto.common.PagedResponse
import com.roomify.app.data.remote.dto.rooms.RoomCardResponse

import java.math.BigDecimal
import com.roomify.app.data.remote.dto.rooms.RoomDetailResponse
class RoomRepository(
    private val roomApi: RoomApi
) {

    // ========================================================
    // PHONG MOI DANG - STUDENT HOME
    // ========================================================

    suspend fun getNewestRooms(
        page: Int = 1,
        pageSize: Int = 6
    ): ApiResult<PagedResponse<RoomCardResponse>> {

        require(page >= 1)

        require(pageSize in 1..50)

        return safeApiCall {

            roomApi.searchRooms(
                sortBy = "newest",
                page = page,
                pageSize = pageSize
            )
        }
    }

    // ========================================================
    // TIM KIEM + BO LOC
    // ========================================================

    suspend fun searchRooms(
        keyword: String? = null,

        provinceId: Int? = null,
        wardId: Int? = null,

        propertyTypeId: Int? = null,

        minPrice: Long? = null,
        maxPrice: Long? = null,

        minArea: BigDecimal? = null,
        maxArea: BigDecimal? = null,

        amenityIds: List<Int>? = null,

        page: Int = 1,
        pageSize: Int = 20
    ): ApiResult<PagedResponse<RoomCardResponse>> {

        require(page >= 1)

        require(pageSize in 1..50)

        if (
            minPrice != null &&
            maxPrice != null
        ) {
            require(minPrice <= maxPrice)
        }

        if (
            minArea != null &&
            maxArea != null
        ) {
            require(minArea <= maxArea)
        }

        return safeApiCall {

            roomApi.searchRooms(

                keyword = keyword,

                provinceId = provinceId,
                wardId = wardId,

                propertyTypeId =
                    propertyTypeId,

                minPrice = minPrice,
                maxPrice = maxPrice,

                minArea = minArea,
                maxArea = maxArea,

                amenityIds =
                    amenityIds
                        ?.takeIf {
                            it.isNotEmpty()
                        },

                sortBy = "newest",

                page = page,
                pageSize = pageSize
            )
        }
    }

    // ========================================================
    // CHI TIET PHONG
    // ========================================================

    suspend fun getRoomDetail(
        roomId: Long
    ): ApiResult<RoomDetailResponse> {

        require(roomId > 0) {
            "RoomId không hợp lệ."
        }

        return safeApiCall {
            roomApi.getRoomDetail(
                roomId
            )
        }
    }
}