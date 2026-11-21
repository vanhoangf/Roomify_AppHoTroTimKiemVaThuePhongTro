
package com.roomify.app.data.remote.dto.common

import com.google.gson.annotations.SerializedName

data class PagedResponse<T>(

    @SerializedName("items")
    val items: List<T>,

    @SerializedName("totalCount")
    val totalCount: Int,

    @SerializedName("page")
    val page: Int,

    @SerializedName("pageSize")
    val pageSize: Int,

    @SerializedName("totalPages")
    val totalPages: Int,

    @SerializedName("hasNextPage")
    val hasNextPage: Boolean,

    @SerializedName("hasPreviousPage")
    val hasPreviousPage: Boolean
)
