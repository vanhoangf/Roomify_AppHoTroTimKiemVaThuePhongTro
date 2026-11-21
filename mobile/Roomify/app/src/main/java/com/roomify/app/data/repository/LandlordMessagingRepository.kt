package com.roomify.app.data.repository

import com.roomify.app.data.remote.api.LandlordMessagingApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.common.safeApiCall
import com.roomify.app.data.remote.dto.messaging.ConversationListResponse
import com.roomify.app.data.remote.dto.messaging.ConversationResponse
import com.roomify.app.data.remote.dto.messaging.MessageResponse
import com.roomify.app.data.remote.dto.messaging.SendMessageRequest

class LandlordMessagingRepository(
    private val api: LandlordMessagingApi
) {

    suspend fun getConversations(
        page: Int = 1,
        pageSize: Int = 20
    ): ApiResult<ConversationListResponse> {

        require(page >= 1)
        require(pageSize in 1..50)

        return safeApiCall {

            api.getConversations(
                page = page,
                pageSize = pageSize
            )
        }
    }

    suspend fun getConversation(
        conversationId: Long
    ): ApiResult<ConversationResponse> {

        require(conversationId > 0L)

        return safeApiCall {

            api.getConversation(
                conversationId
            )
        }
    }

    suspend fun getMessages(
        conversationId: Long
    ): ApiResult<List<MessageResponse>> {

        require(conversationId > 0L)

        return safeApiCall {

            api.getMessages(
                conversationId
            )
        }
    }

    suspend fun sendMessage(
        conversationId: Long,
        content: String
    ): ApiResult<MessageResponse> {

        require(conversationId > 0L)

        val cleanContent =
            content.trim()

        require(
            cleanContent.isNotBlank()
        ) {
            "Tin nhắn không được để trống."
        }

        require(
            cleanContent.length <= 2000
        ) {
            "Tin nhắn tối đa 2000 ký tự."
        }

        return safeApiCall {

            api.sendMessage(
                conversationId =
                    conversationId,
                request =
                    SendMessageRequest(
                        messageType = "TEXT",
                        content = cleanContent,
                        imageUrl = null
                    )
            )
        }
    }

    suspend fun markAsRead(
        conversationId: Long
    ): ApiResult<Unit> {

        require(conversationId > 0L)

        return safeApiCall {

            api.markAsRead(
                conversationId
            )
        }
    }
}