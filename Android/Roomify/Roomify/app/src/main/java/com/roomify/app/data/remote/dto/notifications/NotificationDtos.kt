package com.roomify.app.data.remote.dto.notifications

import com.google.gson.annotations.SerializedName

data class NotificationResponse(

    @SerializedName("notificationId")
    val notificationId: Long,

    @SerializedName("userId")
    val userId: Long,

    @SerializedName("notificationType")
    val notificationType: String?,

    @SerializedName("title")
    val title: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("relatedEntityType")
    val relatedEntityType: String?,

    @SerializedName("relatedEntityId")
    val relatedEntityId: Long?,

    @SerializedName("isRead")
    val isRead: Boolean,

    @SerializedName("readAt")
    val readAt: String?,

    @SerializedName("createdAt")
    val createdAt: String
)

data class NotificationListResponse(

    @SerializedName("items")
    val items: List<NotificationResponse> = emptyList(),

    @SerializedName("totalCount")
    val totalCount: Int,

    @SerializedName("unreadCount")
    val unreadCount: Int,

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

data class UnreadCountResponse(

    @SerializedName("unreadCount")
    val unreadCount: Int
)

data class UpdatedCountResponse(

    @SerializedName("updatedCount")
    val updatedCount: Int
)