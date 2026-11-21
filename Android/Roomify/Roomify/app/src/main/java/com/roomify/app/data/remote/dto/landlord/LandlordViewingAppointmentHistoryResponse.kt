package com.roomify.app.data.remote.dto.landlord

import com.google.gson.annotations.SerializedName

data class LandlordViewingAppointmentHistoryResponse(

    @SerializedName("appointmentHistoryId")
    val appointmentHistoryId: Long,

    @SerializedName("appointmentId")
    val appointmentId: Long,

    @SerializedName("actorUserId")
    val actorUserId: Long,

    @SerializedName("action")
    val action: String,

    @SerializedName("oldStatus")
    val oldStatus: String?,

    @SerializedName("newStatus")
    val newStatus: String,

    @SerializedName("oldScheduledAt")
    val oldScheduledAt: String?,

    @SerializedName("newScheduledAt")
    val newScheduledAt: String?,

    @SerializedName("note")
    val note: String?,

    @SerializedName("createdAt")
    val createdAt: String
)