package com.roomify.app.data.repository

import com.roomify.app.data.remote.api.MessagingApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.common.safeApiCall
import com.roomify.app.data.remote.dto.messaging.ConversationListResponse
import com.roomify.app.data.remote.dto.messaging.ConversationResponse
import com.roomify.app.data.remote.dto.messaging.MessageResponse
import com.roomify.app.data.remote.dto.messaging.SendMessageRequest

class MessagingRepository(
    private val api: MessagingApi
) {

    suspend fun getOrCreateConversation(
        roomId: Long
    ): ApiResult<ConversationResponse> {

        require(roomId > 0)

        return safeApiCall {
            api.getOrCreateConversation(roomId)
        }
    }

    suspend fun getConversations():
            ApiResult<ConversationListResponse> {

        return safeApiCall {
            api.getConversations(
                page = 1,
                pageSize = 20
            )
        }
    }

    suspend fun getConversation(
        conversationId: Long
    ): ApiResult<ConversationResponse> {

        return safeApiCall {
            api.getConversation(conversationId)
        }
    }

    suspend fun getMessages(
        conversationId: Long
    ): ApiResult<List<MessageResponse>> {

        return safeApiCall {
            api.getMessages(conversationId)
        }
    }

    suspend fun sendMessage(
        conversationId: Long,
        content: String
    ): ApiResult<MessageResponse> {

        return safeApiCall {

            api.sendMessage(
                conversationId,
                SendMessageRequest(
                    messageType = "TEXT",
                    content = content,
                    imageUrl = null
                )
            )
        }
    }

    suspend fun markAsRead(
        conversationId: Long
    ): ApiResult<Unit> {

        return safeApiCall {
            api.markAsRead(conversationId)
        }
    }
}