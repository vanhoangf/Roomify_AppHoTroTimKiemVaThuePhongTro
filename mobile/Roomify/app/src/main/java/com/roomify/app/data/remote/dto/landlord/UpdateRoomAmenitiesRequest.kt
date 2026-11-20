package com.roomify.app.data.remote.dto.landlord

import com.google.gson.annotations.SerializedName

data class UpdateRoomAmenitiesRequest(

    @SerializedName("amenityIds")
    val amenityIds: List<Int>
)