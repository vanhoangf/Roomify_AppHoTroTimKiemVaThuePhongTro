package com.roomify.app.data.remote.api

import com.roomify.app.data.remote.dto.contracts.RentalContractListResponse
import com.roomify.app.data.remote.dto.contracts.RentalContractResponse
import com.roomify.app.data.remote.dto.landlord.LandlordCreateRentalContractRequest

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LandlordRentalContractApi {

    @POST(
        "api/landlord/rental-requests/{rentalRequestId}/contract"
    )
    suspend fun createContract(
        @Path("rentalRequestId")
        rentalRequestId: Long,

        @Body
        request: LandlordCreateRentalContractRequest
    ): Response<RentalContractResponse>

    @GET(
        "api/landlord/contracts"
    )
    suspend fun getContracts(
        @Query("status")
        status: String? = null,

        @Query("page")
        page: Int = 1,

        @Query("pageSize")
        pageSize: Int = 20
    ): Response<RentalContractListResponse>

    @GET(
        "api/landlord/contracts/{contractId}"
    )
    suspend fun getContractDetail(
        @Path("contractId")
        contractId: Long
    ): Response<RentalContractResponse>

    @PATCH(
        "api/landlord/contracts/{contractId}/complete"
    )
    suspend fun completeContract(
        @Path("contractId")
        contractId: Long
    ): Response<RentalContractResponse>

    @PATCH(
        "api/landlord/contracts/{contractId}/cancel"
    )
    suspend fun cancelContract(
        @Path("contractId")
        contractId: Long
    ): Response<RentalContractResponse>
}