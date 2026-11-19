package com.roomify.app.data.remote.api

import com.roomify.app.data.remote.dto.appointments.CreateViewingAppointmentRequest
import com.roomify.app.data.remote.dto.appointments.ViewingAppointmentResponse
import com.roomify.app.data.remote.dto.common.PagedResponse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ViewingAppointmentApi {

    @POST("api/student/rooms/{roomId}/viewing-appointments")
    suspend fun createViewingAppointment(
        @Path("roomId")
        roomId: Long,
        @Body
        request: CreateViewingAppointmentRequest
    ): Response<ViewingAppointmentResponse>

    @GET("api/student/viewing-appointments")
    suspend fun getMyAppointments(
        @Query("page")
        page: Int = 1,
        @Query("pageSize")
        pageSize: Int = 20
    ): Response<PagedResponse<ViewingAppointmentResponse>>

    @PATCH("api/student/viewing-appointments/{appointmentId}/cancel")
    suspend fun cancelAppointment(
        @Path("appointmentId")
        appointmentId: Long
    ): Response<ViewingAppointmentResponse>

    @PATCH("api/student/viewing-appointments/{appointmentId}/accept-proposal")
    suspend fun acceptProposal(
        @Path("appointmentId")
        appointmentId: Long
    ): Response<ViewingAppointmentResponse>
}