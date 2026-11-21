package com.roomify.app.data.remote.dto.landlord

import com.google.gson.annotations.SerializedName

data class LandlordReorderRoomImagesRequest(

    @SerializedName("images")
    val images: List<LandlordRoomImageOrderItem>
)

data class LandlordRoomImageOrderItem(

    @SerializedName("imageId")
    val imageId: Long,

    @SerializedName("sortOrder")
    val sortOrder: Int
)