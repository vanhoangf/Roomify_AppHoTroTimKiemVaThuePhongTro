
package com.roomify.app.data.remote.api

import com.roomify.app.data.remote.dto.common.PagedResponse
import com.roomify.app.data.remote.dto.rooms.RoomCardResponse

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RoomApi {

    @GET("api/rooms")
    suspend fun searchRooms(

        @Query("Keyword")
        keyword: String? = null,

        @Query("ProvinceId")
        provinceId: Int? = null,

        @Query("WardId")
        wardId: Int? = null,

        @Query("PropertyTypeId")
        propertyTypeId: Int? = null,

        @Query("MinPrice")
        minPrice: Long? = null,

        @Query("MaxPrice")
        maxPrice: Long? = null,

        @Query("AvailabilityStatus")
        availabilityStatus: String? = null,

        @Query("SortBy")
        sortBy: String = "newest",

        @Query("Page")
        page: Int = 1,

        @Query("PageSize")
        pageSize: Int = 6

    ): Response<PagedResponse<RoomCardResponse>>
}
