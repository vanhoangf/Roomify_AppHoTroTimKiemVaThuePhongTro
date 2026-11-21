package com.roomify.app.data.remote.dto.appointments

import com.google.gson.annotations.SerializedName

data class CreateViewingAppointmentRequest(

    @SerializedName("scheduledAt")
    val scheduledAt: String,

    @SerializedName("note")
    val note: String?
)

data class ViewingAppointmentResponse(

    @SerializedName("appointmentId")
    val appointmentId: Long,

    @SerializedName("roomId")
    val roomId: Long,

    @SerializedName("roomTitle")
    val roomTitle: String,

    @SerializedName("propertyName")
    val propertyName: String,

    @SerializedName("studentId")
    val studentId: Long,

    @SerializedName("studentName")
    val studentName: String,

    @SerializedName("studentPhone")
    val studentPhone: String?,

    @SerializedName("scheduledAt")
    val scheduledAt: String,

    @SerializedName("proposedAt")
    val proposedAt: String?,

    @SerializedName("status")
    val status: String,

    @SerializedName("note")
    val note: String?,

    @SerializedName("landlordNote")
    val landlordNote: String?,

    @SerializedName("respondedAt")
    val respondedAt: String?,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)