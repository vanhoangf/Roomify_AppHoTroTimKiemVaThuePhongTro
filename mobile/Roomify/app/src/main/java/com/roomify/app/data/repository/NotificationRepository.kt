package com.roomify.app.data.repository

import com.roomify.app.data.remote.api.NotificationApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.common.safeApiCall
import com.roomify.app.data.remote.dto.notifications.NotificationListResponse
import com.roomify.app.data.remote.dto.notifications.NotificationResponse
import com.roomify.app.data.remote.dto.notifications.UnreadCountResponse
import com.roomify.app.data.remote.dto.notifications.UpdatedCountResponse

class NotificationRepository(
    private val api: NotificationApi
) {

    suspend fun getNotifications():
            ApiResult<NotificationListResponse> {

        return safeApiCall {
            api.getNotifications(
                page = 1,
                pageSize = 30
            )
        }
    }

    suspend fun getUnreadCount():
            ApiResult<UnreadCountResponse> {

        return safeApiCall {
            api.getUnreadCount()
        }
    }

    suspend fun markAsRead(
        notificationId: Long
    ): ApiResult<NotificationResponse> {

        require(notificationId > 0)

        return safeApiCall {
            api.markAsRead(
                notificationId
            )
        }
    }

    suspend fun markAllAsRead():
            ApiResult<UpdatedCountResponse> {

        return safeApiCall {
            api.markAllAsRead()
        }
    }
}