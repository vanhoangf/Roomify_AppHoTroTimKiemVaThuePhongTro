package com.roomify.app.data.remote.api

import com.roomify.app.data.remote.dto.contracts.RentalContractListResponse
import com.roomify.app.data.remote.dto.contracts.RentalContractResponse

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface RentalContractApi {

    @GET("api/student/contracts")
    suspend fun getMyContracts(
        @Query("page")
        page: Int = 1,

        @Query("pageSize")
        pageSize: Int = 20
    ): Response<RentalContractListResponse>

    @GET("api/student/contracts/{contractId}")
    suspend fun getContractDetail(
        @Path("contractId")
        contractId: Long
    ): Response<RentalContractResponse>

    @PATCH("api/student/contracts/{contractId}/activate")
    suspend fun activateContract(
        @Path("contractId")
        contractId: Long
    ): Response<RentalContractResponse>

    @PATCH("api/student/contracts/{contractId}/cancel")
    suspend fun cancelContract(
        @Path("contractId")
        contractId: Long
    ): Response<RentalContractResponse>
}