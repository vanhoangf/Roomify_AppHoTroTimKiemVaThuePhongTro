package com.roomify.app.data.remote.dto.landlord

import com.google.gson.annotations.SerializedName

data class UpdateRoomAvailabilityRequest(

    @SerializedName("availabilityStatus")
    val availabilityStatus: String
)