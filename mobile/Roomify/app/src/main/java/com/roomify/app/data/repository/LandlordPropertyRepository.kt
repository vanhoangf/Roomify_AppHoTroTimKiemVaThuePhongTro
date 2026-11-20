package com.roomify.app.data.repository

import com.roomify.app.data.remote.api.LandlordPropertyApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.common.safeApiCall
import com.roomify.app.data.remote.dto.landlord.CreatePropertyRequest
import com.roomify.app.data.remote.dto.landlord.PropertyResponse
import com.roomify.app.data.remote.dto.landlord.UpdatePropertyRequest
import com.roomify.app.data.remote.dto.landlord.UpdatePropertyStatusRequest

import java.util.Locale

class LandlordPropertyRepository(
    private val propertyApi: LandlordPropertyApi
) {

    // ========================================================
    // LIST PROPERTY
    // ========================================================

    suspend fun getProperties():
            ApiResult<List<PropertyResponse>> {

        return safeApiCall {
            propertyApi.getProperties()
        }
    }


    // ========================================================
    // PROPERTY DETAIL
    // ========================================================

    suspend fun getProperty(
        propertyId: Long
    ): ApiResult<PropertyResponse> {

        require(propertyId > 0) {
            "PropertyId không hợp lệ."
        }

        return safeApiCall {

            propertyApi.getProperty(
                propertyId
            )
        }
    }


    // ========================================================
    // CREATE PROPERTY
    // ========================================================

    suspend fun createProperty(
        request: CreatePropertyRequest
    ): ApiResult<PropertyResponse> {

        require(
            request.propertyTypeId > 0
        ) {
            "PropertyTypeId không hợp lệ."
        }

        require(
            request.propertyName.isNotBlank()
        ) {
            "Tên khu trọ không được để trống."
        }

        require(
            request.provinceId > 0
        ) {
            "ProvinceId không hợp lệ."
        }

        require(
            request.wardId > 0
        ) {
            "WardId không hợp lệ."
        }

        require(
            request.addressLine.isNotBlank()
        ) {
            "Địa chỉ không được để trống."
        }

        return safeApiCall {

            propertyApi.createProperty(
                request
            )
        }
    }


    // ========================================================
    // UPDATE PROPERTY
    // ========================================================

    suspend fun updateProperty(
        propertyId: Long,
        request: UpdatePropertyRequest
    ): ApiResult<PropertyResponse> {

        require(propertyId > 0) {
            "PropertyId không hợp lệ."
        }

        require(
            request.propertyTypeId > 0
        ) {
            "PropertyTypeId không hợp lệ."
        }

        require(
            request.propertyName.isNotBlank()
        ) {
            "Tên khu trọ không được để trống."
        }

        require(
            request.provinceId > 0
        ) {
            "ProvinceId không hợp lệ."
        }

        require(
            request.wardId > 0
        ) {
            "WardId không hợp lệ."
        }

        require(
            request.addressLine.isNotBlank()
        ) {
            "Địa chỉ không được để trống."
        }

        return safeApiCall {

            propertyApi.updateProperty(
                propertyId = propertyId,
                request = request
            )
        }
    }


    // ========================================================
    // ACTIVE / INACTIVE
    // ========================================================

    suspend fun updatePropertyStatus(
        propertyId: Long,
        status: String
    ): ApiResult<PropertyResponse> {

        require(propertyId > 0) {
            "PropertyId không hợp lệ."
        }

        val normalizedStatus =
            status
                .trim()
                .uppercase(Locale.ROOT)

        require(
            normalizedStatus == "ACTIVE" ||
                    normalizedStatus == "INACTIVE"
        ) {
            "Trạng thái khu trọ không hợp lệ."
        }

        return safeApiCall {

            propertyApi.updatePropertyStatus(
                propertyId = propertyId,

                request =
                    UpdatePropertyStatusRequest(
                        status = normalizedStatus
                    )
            )
        }
    }
}