package com.roomify.app.data.repository

import com.roomify.app.data.remote.api.ViewingAppointmentApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.common.safeApiCall
import com.roomify.app.data.remote.dto.appointments.CreateViewingAppointmentRequest
import com.roomify.app.data.remote.dto.appointments.ViewingAppointmentResponse
import com.roomify.app.data.remote.dto.common.PagedResponse

class ViewingAppointmentRepository(
    private val api: ViewingAppointmentApi
) {

    suspend fun create(
        roomId: Long,
        request: CreateViewingAppointmentRequest
    ): ApiResult<ViewingAppointmentResponse> {

        require(roomId > 0)

        return safeApiCall {
            api.createViewingAppointment(
                roomId,
                request
            )
        }
    }

    suspend fun getMyAppointments(
        page: Int = 1,
        pageSize: Int = 20
    ): ApiResult<PagedResponse<ViewingAppointmentResponse>> {

        require(page >= 1)
        require(pageSize in 1..50)

        return safeApiCall {
            api.getMyAppointments(
                page,
                pageSize
            )
        }
    }

    suspend fun cancel(
        appointmentId: Long
    ): ApiResult<ViewingAppointmentResponse> {

        require(appointmentId > 0)

        return safeApiCall {
            api.cancelAppointment(
                appointmentId
            )
        }
    }

    suspend fun acceptProposal(
        appointmentId: Long
    ): ApiResult<ViewingAppointmentResponse> {

        require(appointmentId > 0)

        return safeApiCall {
            api.acceptProposal(
                appointmentId
            )
        }
    }
}