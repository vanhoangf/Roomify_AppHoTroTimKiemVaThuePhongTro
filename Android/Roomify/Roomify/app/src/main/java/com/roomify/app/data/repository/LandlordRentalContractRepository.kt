package com.roomify.app.data.repository

import com.roomify.app.data.remote.api.LandlordRentalContractApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.common.safeApiCall
import com.roomify.app.data.remote.dto.contracts.RentalContractListResponse
import com.roomify.app.data.remote.dto.contracts.RentalContractResponse
import com.roomify.app.data.remote.dto.landlord.LandlordCreateRentalContractRequest

class LandlordRentalContractRepository(
    private val api:
    LandlordRentalContractApi
) {

    suspend fun create(
        rentalRequestId: Long,
        terms: String?,
        contractFileUrl: String?
    ): ApiResult<RentalContractResponse> {

        require(
            rentalRequestId > 0L
        )

        return safeApiCall {

            api.createContract(
                rentalRequestId =
                    rentalRequestId,
                request =
                    LandlordCreateRentalContractRequest(
                        terms =
                            normalize(
                                terms
                            ),
                        contractFileUrl =
                            normalize(
                                contractFileUrl
                            )
                    )
            )
        }
    }

    suspend fun getContracts(
        status: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): ApiResult<RentalContractListResponse> {

        require(
            page >= 1
        )

        require(
            pageSize in 1..50
        )

        return safeApiCall {

            api.getContracts(
                status =
                    normalizeStatus(
                        status
                    ),
                page =
                    page,
                pageSize =
                    pageSize
            )
        }
    }

    suspend fun getDetail(
        contractId: Long
    ): ApiResult<RentalContractResponse> {

        require(
            contractId > 0L
        )

        return safeApiCall {

            api.getContractDetail(
                contractId
            )
        }
    }

    suspend fun complete(
        contractId: Long
    ): ApiResult<RentalContractResponse> {

        require(
            contractId > 0L
        )

        return safeApiCall {

            api.completeContract(
                contractId
            )
        }
    }

    suspend fun cancel(
        contractId: Long
    ): ApiResult<RentalContractResponse> {

        require(
            contractId > 0L
        )

        return safeApiCall {

            api.cancelContract(
                contractId
            )
        }
    }

    private fun normalizeStatus(
        value: String?
    ): String? {

        return value
            ?.trim()
            ?.uppercase()
            ?.takeIf {
                it.isNotBlank()
            }
    }

    private fun normalize(
        value: String?
    ): String? {

        return value
            ?.trim()
            ?.takeIf {
                it.isNotBlank()
            }
    }
}