package com.roomify.app.data.remote.api

import com.roomify.app.data.remote.dto.appointments.ViewingAppointmentResponse
import com.roomify.app.data.remote.dto.common.PagedResponse
import com.roomify.app.data.remote.dto.landlord.LandlordProposeViewingTimeRequest
import com.roomify.app.data.remote.dto.landlord.LandlordViewingAppointmentHistoryResponse
import com.roomify.app.data.remote.dto.landlord.LandlordViewingAppointmentNoteRequest

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface LandlordViewingAppointmentApi {

    @GET(
        "api/landlord/viewing-appointments"
    )
    suspend fun getAppointments(
        @Query("page")
        page: Int = 1,

        @Query("pageSize")
        pageSize: Int = 20
    ): Response<
            PagedResponse<ViewingAppointmentResponse>
            >

    @GET(
        "api/landlord/viewing-appointments/{appointmentId}"
    )
    suspend fun getAppointmentDetail(
        @Path("appointmentId")
        appointmentId: Long
    ): Response<ViewingAppointmentResponse>

    @GET(
        "api/landlord/viewing-appointments/{appointmentId}/history"
    )
    suspend fun getHistory(
        @Path("appointmentId")
        appointmentId: Long
    ): Response<
            List<LandlordViewingAppointmentHistoryResponse>
            >

    @PATCH(
        "api/landlord/viewing-appointments/{appointmentId}/confirm"
    )
    suspend fun confirmAppointment(
        @Path("appointmentId")
        appointmentId: Long,

        @Body
        request:
        LandlordViewingAppointmentNoteRequest
    ): Response<ViewingAppointmentResponse>

    @PATCH(
        "api/landlord/viewing-appointments/{appointmentId}/reject"
    )
    suspend fun rejectAppointment(
        @Path("appointmentId")
        appointmentId: Long,

        @Body
        request:
        LandlordViewingAppointmentNoteRequest
    ): Response<ViewingAppointmentResponse>

    @PATCH(
        "api/landlord/viewing-appointments/{appointmentId}/propose"
    )
    suspend fun proposeTime(
        @Path("appointmentId")
        appointmentId: Long,

        @Body
        request:
        LandlordProposeViewingTimeRequest
    ): Response<ViewingAppointmentResponse>

    @PATCH(
        "api/landlord/viewing-appointments/{appointmentId}/complete"
    )
    suspend fun completeAppointment(
        @Path("appointmentId")
        appointmentId: Long,

        @Body
        request:
        LandlordViewingAppointmentNoteRequest
    ): Response<ViewingAppointmentResponse>
}