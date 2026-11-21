package com.roomify.app.data.remote.api

import com.roomify.app.data.remote.dto.notifications.NotificationListResponse
import com.roomify.app.data.remote.dto.notifications.NotificationResponse
import com.roomify.app.data.remote.dto.notifications.UnreadCountResponse
import com.roomify.app.data.remote.dto.notifications.UpdatedCountResponse

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApi {

    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("isRead")
        isRead: Boolean? = null,

        @Query("page")
        page: Int = 1,

        @Query("pageSize")
        pageSize: Int = 30
    ): Response<NotificationListResponse>

    @GET("api/notifications/unread-count")
    suspend fun getUnreadCount():
            Response<UnreadCountResponse>

    @PATCH("api/notifications/{notificationId}/read")
    suspend fun markAsRead(
        @Path("notificationId")
        notificationId: Long
    ): Response<NotificationResponse>

    @PATCH("api/notifications/read-all")
    suspend fun markAllAsRead():
            Response<UpdatedCountResponse>
}