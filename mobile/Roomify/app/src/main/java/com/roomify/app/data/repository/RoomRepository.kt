
package com.roomify.app.data.repository

import com.roomify.app.data.remote.api.RoomApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.common.safeApiCall
import com.roomify.app.data.remote.dto.common.PagedResponse
import com.roomify.app.data.remote.dto.rooms.RoomCardResponse

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

        require(page >= 1) {
            "Page phải lớn hơn hoặc bằng 1."
        }

        require(pageSize in 1..50) {
            "PageSize phải từ 1 đến 50."
        }

        return safeApiCall {
            roomApi.searchRooms(
                sortBy = "newest",
                page = page,
                pageSize = pageSize
            )
        }
    }

    // ========================================================
    // TIM KIEM PHONG
    // ========================================================

    suspend fun searchRooms(
        keyword: String? = null,
        provinceId: Int? = null,
        wardId: Int? = null,
        minPrice: Long? = null,
        maxPrice: Long? = null,
        page: Int = 1,
        pageSize: Int = 10
    ): ApiResult<PagedResponse<RoomCardResponse>> {

        return safeApiCall {
            roomApi.searchRooms(
                keyword = keyword,
                provinceId = provinceId,
                wardId = wardId,
                minPrice = minPrice,
                maxPrice = maxPrice,
                sortBy = "newest",
                page = page,
                pageSize = pageSize
            )
        }
    }
}
