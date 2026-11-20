package com.roomify.app.data.remote.dto.landlord

import com.google.gson.annotations.SerializedName

data class LandlordViewingAppointmentNoteRequest(

    @SerializedName("note")
    val note: String?
)

data class LandlordProposeViewingTimeRequest(

    @SerializedName("proposedAt")
    val proposedAt: String,

    @SerializedName("note")
    val note: String?
)