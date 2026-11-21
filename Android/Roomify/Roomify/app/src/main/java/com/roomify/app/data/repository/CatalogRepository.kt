package com.roomify.app.data.repository

import com.roomify.app.data.remote.api.CatalogApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.common.safeApiCall
import com.roomify.app.data.remote.dto.catalog.AmenityResponse
import com.roomify.app.data.remote.dto.catalog.PropertyTypeResponse
import com.roomify.app.data.remote.dto.catalog.ProvinceResponse
import com.roomify.app.data.remote.dto.catalog.UniversityResponse
import com.roomify.app.data.remote.dto.catalog.WardResponse

class CatalogRepository(
    private val catalogApi: CatalogApi
) {

    suspend fun getProvinces():
            ApiResult<List<ProvinceResponse>> {

        return safeApiCall {
            catalogApi.getProvinces()
        }
    }

    suspend fun getWards(
        provinceId: Int
    ): ApiResult<List<WardResponse>> {

        require(provinceId > 0)

        return safeApiCall {
            catalogApi.getWards(
                provinceId
            )
        }
    }

    suspend fun getUniversities():
            ApiResult<List<UniversityResponse>> {

        return safeApiCall {
            catalogApi.getUniversities()
        }
    }

    suspend fun getPropertyTypes():
            ApiResult<List<PropertyTypeResponse>> {

        return safeApiCall {
            catalogApi.getPropertyTypes()
        }
    }

    suspend fun getAmenities():
            ApiResult<List<AmenityResponse>> {

        return safeApiCall {
            catalogApi.getAmenities()
        }
    }
}