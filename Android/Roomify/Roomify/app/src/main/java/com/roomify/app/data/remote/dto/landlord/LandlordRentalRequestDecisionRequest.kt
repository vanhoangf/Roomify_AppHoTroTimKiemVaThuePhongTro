package com.roomify.app.data.remote.dto.landlord

import com.google.gson.annotations.SerializedName

data class LandlordRentalRequestDecisionRequest(

    @SerializedName("note")
    val note: String?
)