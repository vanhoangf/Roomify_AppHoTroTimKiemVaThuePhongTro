package com.roomify.app.data.remote.dto.landlord

import com.google.gson.annotations.SerializedName

data class LandlordCreateRentalContractRequest(

    @SerializedName("terms")
    val terms: String?,

    @SerializedName("contractFileUrl")
    val contractFileUrl: String?
)