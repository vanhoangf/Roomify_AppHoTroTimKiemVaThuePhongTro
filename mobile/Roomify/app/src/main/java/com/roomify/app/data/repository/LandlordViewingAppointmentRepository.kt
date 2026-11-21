package com.roomify.app.data.repository

import com.roomify.app.data.remote.api.LandlordViewingAppointmentApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.common.safeApiCall
import com.roomify.app.data.remote.dto.appointments.ViewingAppointmentResponse
import com.roomify.app.data.remote.dto.common.PagedResponse
import com.roomify.app.data.remote.dto.landlord.LandlordProposeViewingTimeRequest
import com.roomify.app.data.remote.dto.landlord.LandlordViewingAppointmentHistoryResponse
import com.roomify.app.data.remote.dto.landlord.LandlordViewingAppointmentNoteRequest

class LandlordViewingAppointmentRepository(
    private val api:
    LandlordViewingAppointmentApi
) {

    suspend fun getAppointments(
        page: Int = 1,
        pageSize: Int = 20
    ): ApiResult<
            PagedResponse<ViewingAppointmentResponse>
            > {

        require(page >= 1)
        require(pageSize in 1..50)

        return safeApiCall {
            api.getAppointments(
                page,
                pageSize
            )
        }
    }

    suspend fun getDetail(
        appointmentId: Long
    ): ApiResult<ViewingAppointmentResponse> {

        require(
            appointmentId > 0L
        )

        return safeApiCall {
            api.getAppointmentDetail(
                appointmentId
            )
        }
    }

    suspend fun getHistory(
        appointmentId: Long
    ): ApiResult<
            List<LandlordViewingAppointmentHistoryResponse>
            > {

        require(
            appointmentId > 0L
        )

        return safeApiCall {
            api.getHistory(
                appointmentId
            )
        }
    }

    suspend fun confirm(
        appointmentId: Long,
        note: String? = null
    ): ApiResult<ViewingAppointmentResponse> {

        require(
            appointmentId > 0L
        )

        return safeApiCall {

            api.confirmAppointment(
                appointmentId,
                LandlordViewingAppointmentNoteRequest(
                    note =
                        normalizeNote(note)
                )
            )
        }
    }

    suspend fun reject(
        appointmentId: Long,
        reason: String
    ): ApiResult<ViewingAppointmentResponse> {

        require(
            appointmentId > 0L
        )

        val cleanReason =
            reason.trim()

        require(
            cleanReason.isNotBlank()
        ) {
            "Vui lòng nhập lý do từ chối."
        }

        require(
            cleanReason.length <= 1000
        ) {
            "Ghi chú tối đa 1000 ký tự."
        }

        return safeApiCall {

            api.rejectAppointment(
                appointmentId,
                LandlordViewingAppointmentNoteRequest(
                    note =
                        cleanReason
                )
            )
        }
    }

    suspend fun propose(
        appointmentId: Long,
        proposedAt: String,
        note: String? = null
    ): ApiResult<ViewingAppointmentResponse> {

        require(
            appointmentId > 0L
        )

        require(
            proposedAt.isNotBlank()
        ) {
            "Thiếu thời gian đề xuất."
        }

        return safeApiCall {

            api.proposeTime(
                appointmentId,
                LandlordProposeViewingTimeRequest(
                    proposedAt =
                        proposedAt,
                    note =
                        normalizeNote(note)
                )
            )
        }
    }

    suspend fun complete(
        appointmentId: Long,
        note: String? = null
    ): ApiResult<ViewingAppointmentResponse> {

        require(
            appointmentId > 0L
        )

        return safeApiCall {

            api.completeAppointment(
                appointmentId,
                LandlordViewingAppointmentNoteRequest(
                    note =
                        normalizeNote(note)
                )
            )
        }
    }

    private fun normalizeNote(
        note: String?
    ): String? {

        val clean =
            note
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        require(
            clean == null ||
                    clean.length <= 1000
        ) {
            "Ghi chú tối đa 1000 ký tự."
        }

        return clean
    }
}