package com.roomify.app.data.remote.dto.messaging

import com.google.gson.annotations.SerializedName

data class ConversationResponse(

    @SerializedName("conversationId")
    val conversationId: Long,

    @SerializedName("roomId")
    val roomId: Long,

    @SerializedName("roomTitle")
    val roomTitle: String,

    @SerializedName("studentId")
    val studentId: Long,

    @SerializedName("studentUserId")
    val studentUserId: Long,

    @SerializedName("studentName")
    val studentName: String,

    @SerializedName("landlordId")
    val landlordId: Long,

    @SerializedName("landlordUserId")
    val landlordUserId: Long,

    @SerializedName("landlordName")
    val landlordName: String,

    @SerializedName("lastMessage")
    val lastMessage: String?,

    @SerializedName("lastMessageType")
    val lastMessageType: String?,

    @SerializedName("lastMessageAt")
    val lastMessageAt: String?,

    @SerializedName("unreadCount")
    val unreadCount: Int,

    @SerializedName("createdAt")
    val createdAt: String
)

data class ConversationListResponse(

    @SerializedName("items")
    val items: List<ConversationResponse> = emptyList(),

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

data class MessageResponse(

    @SerializedName("messageId")
    val messageId: Long,

    @SerializedName("conversationId")
    val conversationId: Long,

    @SerializedName("senderUserId")
    val senderUserId: Long,

    @SerializedName("senderName")
    val senderName: String,

    @SerializedName("senderRole")
    val senderRole: String,

    @SerializedName("content")
    val content: String?,

    @SerializedName("imageUrl")
    val imageUrl: String?,

    @SerializedName("messageType")
    val messageType: String,

    @SerializedName("isMine")
    val isMine: Boolean,

    @SerializedName("isRead")
    val isRead: Boolean,

    @SerializedName("readAt")
    val readAt: String?,

    @SerializedName("sentAt")
    val sentAt: String
)

data class SendMessageRequest(

    @SerializedName("messageType")
    val messageType: String = "TEXT",

    @SerializedName("content")
    val content: String?,

    @SerializedName("imageUrl")
    val imageUrl: String? = null
)