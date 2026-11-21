package com.roomify.app.data.repository

import com.roomify.app.data.remote.api.LandlordRoomImageApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.common.safeApiCall
import com.roomify.app.data.remote.dto.landlord.LandlordReorderRoomImagesRequest
import com.roomify.app.data.remote.dto.landlord.LandlordRoomImageOrderItem
import com.roomify.app.data.remote.dto.landlord.LandlordRoomImageResponse

import okhttp3.MultipartBody

class LandlordRoomImageRepository(
    private val api: LandlordRoomImageApi
) {

    suspend fun getImages(
        roomId: Long
    ): ApiResult<List<LandlordRoomImageResponse>> {

        require(
            roomId > 0L
        )

        return safeApiCall {
            api.getImages(
                roomId
            )
        }
    }

    suspend fun uploadImages(
        roomId: Long,
        files: List<MultipartBody.Part>
    ): ApiResult<List<LandlordRoomImageResponse>> {

        require(
            roomId > 0L
        )

        require(
            files.isNotEmpty()
        )

        return safeApiCall {
            api.uploadImages(
                roomId,
                files
            )
        }
    }

    suspend fun setPrimary(
        roomId: Long,
        imageId: Long
    ): ApiResult<LandlordRoomImageResponse> {

        require(
            roomId > 0L
        )

        require(
            imageId > 0L
        )

        return safeApiCall {
            api.setPrimary(
                roomId,
                imageId
            )
        }
    }

    suspend fun reorderImages(
        roomId: Long,
        images: List<LandlordRoomImageResponse>
    ): ApiResult<List<LandlordRoomImageResponse>> {

        require(
            roomId > 0L
        )

        val request =
            LandlordReorderRoomImagesRequest(
                images =
                    images.mapIndexed {
                            index,
                            image ->

                        LandlordRoomImageOrderItem(
                            imageId =
                                image.imageId,

                            sortOrder =
                                index
                        )
                    }
            )

        return safeApiCall {
            api.reorderImages(
                roomId,
                request
            )
        }
    }

    suspend fun deleteImage(
        roomId: Long,
        imageId: Long
    ): ApiResult<Unit> {

        require(
            roomId > 0L
        )

        require(
            imageId > 0L
        )

        return safeApiCall {
            api.deleteImage(
                roomId,
                imageId
            )
        }
    }
}