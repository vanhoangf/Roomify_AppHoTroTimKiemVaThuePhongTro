package com.roomify.app.data.remote.api

import com.roomify.app.data.remote.dto.landlord.CreatePropertyRequest
import com.roomify.app.data.remote.dto.landlord.PropertyResponse
import com.roomify.app.data.remote.dto.landlord.UpdatePropertyRequest
import com.roomify.app.data.remote.dto.landlord.UpdatePropertyStatusRequest

import retrofit2.Response

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface LandlordPropertyApi {

    // ========================================================
    // GET MY PROPERTIES
    // ========================================================

    @GET("api/landlord/properties")
    suspend fun getProperties():
            Response<List<PropertyResponse>>


    // ========================================================
    // GET PROPERTY DETAIL
    // ========================================================

    @GET(
        "api/landlord/properties/{propertyId}"
    )
    suspend fun getProperty(
        @Path("propertyId")
        propertyId: Long
    ): Response<PropertyResponse>


    // ========================================================
    // CREATE PROPERTY
    // ========================================================

    @POST("api/landlord/properties")
    suspend fun createProperty(
        @Body
        request: CreatePropertyRequest
    ): Response<PropertyResponse>


    // ========================================================
    // UPDATE PROPERTY
    // ========================================================

    @PUT(
        "api/landlord/properties/{propertyId}"
    )
    suspend fun updateProperty(
        @Path("propertyId")
        propertyId: Long,

        @Body
        request: UpdatePropertyRequest
    ): Response<PropertyResponse>


    // ========================================================
    // ACTIVE / INACTIVE
    // ========================================================

    @PATCH(
        "api/landlord/properties/{propertyId}/status"
    )
    suspend fun updatePropertyStatus(
        @Path("propertyId")
        propertyId: Long,

        @Body
        request: UpdatePropertyStatusRequest
    ): Response<PropertyResponse>
}