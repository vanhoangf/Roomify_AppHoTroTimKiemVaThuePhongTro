package com.roomify.app.data.remote.dto.landlord

import com.google.gson.annotations.SerializedName

data class LandlordRoomImageResponse(

    @SerializedName("imageId")
    val imageId: Long,

    @SerializedName("roomId")
    val roomId: Long,

    @SerializedName("imageUrl")
    val imageUrl: String,

    @SerializedName("isPrimary")
    val isPrimary: Boolean,

    @SerializedName("sortOrder")
    val sortOrder: Int,

    @SerializedName("createdAt")
    val createdAt: String
)