package com.roomify.app.data.repository

import com.roomify.app.data.remote.api.UserApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.common.safeApiCall
import com.roomify.app.data.remote.dto.users.StudentProfileResponse
import com.roomify.app.data.remote.dto.users.UpdateStudentProfileRequest
import com.roomify.app.data.remote.dto.users.UpdateUserRequest
import com.roomify.app.data.remote.dto.users.UserMeResponse

class UserRepository(
    private val api: UserApi
) {

    suspend fun getMe():
            ApiResult<UserMeResponse> {

        return safeApiCall {
            api.getMe()
        }
    }

    suspend fun updateMe(
        request: UpdateUserRequest
    ): ApiResult<UserMeResponse> {

        return safeApiCall {
            api.updateMe(request)
        }
    }

    suspend fun updateStudentProfile(
        request: UpdateStudentProfileRequest
    ): ApiResult<StudentProfileResponse> {

        return safeApiCall {
            api.updateStudentProfile(request)
        }
    }
}