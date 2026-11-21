package com.roomify.app.data.remote.api

import com.roomify.app.data.remote.dto.messaging.ConversationListResponse
import com.roomify.app.data.remote.dto.messaging.ConversationResponse
import com.roomify.app.data.remote.dto.messaging.MessageResponse
import com.roomify.app.data.remote.dto.messaging.SendMessageRequest

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MessagingApi {

    @POST("api/student/rooms/{roomId}/conversation")
    suspend fun getOrCreateConversation(
        @Path("roomId")
        roomId: Long
    ): Response<ConversationResponse>

    @GET("api/student/conversations")
    suspend fun getConversations(
        @Query("page")
        page: Int = 1,

        @Query("pageSize")
        pageSize: Int = 20
    ): Response<ConversationListResponse>

    @GET("api/student/conversations/{conversationId}")
    suspend fun getConversation(
        @Path("conversationId")
        conversationId: Long
    ): Response<ConversationResponse>

    @GET("api/student/conversations/{conversationId}/messages")
    suspend fun getMessages(
        @Path("conversationId")
        conversationId: Long
    ): Response<List<MessageResponse>>

    @POST("api/student/conversations/{conversationId}/messages")
    suspend fun sendMessage(
        @Path("conversationId")
        conversationId: Long,

        @Body
        request: SendMessageRequest
    ): Response<MessageResponse>

    @PATCH("api/student/conversations/{conversationId}/messages/read")
    suspend fun markAsRead(
        @Path("conversationId")
        conversationId: Long
    ): Response<Unit>
}