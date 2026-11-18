package com.roomify.app.data.remote.api

import com.roomify.app.data.remote.dto.catalog.AmenityResponse
import com.roomify.app.data.remote.dto.catalog.PropertyTypeResponse
import com.roomify.app.data.remote.dto.catalog.ProvinceResponse
import com.roomify.app.data.remote.dto.catalog.WardResponse

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CatalogApi {

    @GET("api/catalog/provinces")
    suspend fun getProvinces():
            Response<List<ProvinceResponse>>

    @GET("api/catalog/provinces/{provinceId}/wards")
    suspend fun getWards(
        @Path("provinceId")
        provinceId: Int
    ): Response<List<WardResponse>>

    @GET("api/catalog/property-types")
    suspend fun getPropertyTypes():
            Response<List<PropertyTypeResponse>>

    @GET("api/catalog/amenities")
    suspend fun getAmenities():
            Response<List<AmenityResponse>>
}