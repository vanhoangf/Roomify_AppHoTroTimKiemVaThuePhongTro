
package com.roomify.app.data.remote.api

import com.roomify.app.data.remote.dto.common.HealthResponse

import retrofit2.Response
import retrofit2.http.GET

interface HealthApi {

    @GET("api/health")
    suspend fun getHealth(): Response<HealthResponse>
}
