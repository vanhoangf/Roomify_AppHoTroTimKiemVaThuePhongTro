package com.roomify.app.data.repository

import com.roomify.app.data.remote.api.RentalContractApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.common.safeApiCall
import com.roomify.app.data.remote.dto.contracts.RentalContractListResponse
import com.roomify.app.data.remote.dto.contracts.RentalContractResponse

class RentalContractRepository(
    private val api: RentalContractApi
) {

    suspend fun getMyContracts(
        page: Int = 1,
        pageSize: Int = 20
    ): ApiResult<RentalContractListResponse> {

        require(page >= 1)
        require(pageSize in 1..50)

        return safeApiCall {
            api.getMyContracts(
                page,
                pageSize
            )
        }
    }

    suspend fun getDetail(
        contractId: Long
    ): ApiResult<RentalContractResponse> {

        require(contractId > 0)

        return safeApiCall {
            api.getContractDetail(
                contractId
            )
        }
    }

    suspend fun activate(
        contractId: Long
    ): ApiResult<RentalContractResponse> {

        require(contractId > 0)

        return safeApiCall {
            api.activateContract(
                contractId
            )
        }
    }

    suspend fun cancel(
        contractId: Long
    ): ApiResult<RentalContractResponse> {

        require(contractId > 0)

        return safeApiCall {
            api.cancelContract(
                contractId
            )
        }
    }
}