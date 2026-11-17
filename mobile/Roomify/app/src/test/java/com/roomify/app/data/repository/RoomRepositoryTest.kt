
package com.roomify.app.data.repository

import com.roomify.app.data.remote.api.RoomApi
import com.roomify.app.data.remote.common.ApiResult

import kotlinx.coroutines.runBlocking

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import java.math.BigDecimal

class RoomRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: RoomRepository

    @Before
    fun setUp() {

        server = MockWebServer()
        server.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()

        val api = retrofit.create(
            RoomApi::class.java
        )

        repository = RoomRepository(api)
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun getNewestRooms_success() = runBlocking {

        val json = """
            {
              "items": [
                {
                  "roomId": 101,
                  "propertyId": 10,
                  "propertyName": "Khu trọ A",
                  "propertyTypeName": "Nhà trọ",
                  "title": "Phòng trọ gần trường",
                  "areaM2": 25.5,
                  "rentPrice": 2500000,
                  "depositAmount": 1000000,
                  "maxOccupants": 2,
                  "genderPreference": "ALL",
                  "listingStatus": "PUBLISHED",
                  "availabilityStatus": "AVAILABLE",
                  "availableFrom": "2026-11-20",
                  "provinceId": 1,
                  "provinceName": "Đà Nẵng",
                  "wardId": 2,
                  "wardName": "Hòa Xuân",
                  "addressLine": "Đường A",
                  "thumbnailUrl": null,
                  "distanceKm": null,
                  "publishedAt": "2026-11-17T08:00:00"
                }
              ],
              "totalCount": 1,
              "page": 1,
              "pageSize": 6,
              "totalPages": 1,
              "hasNextPage": false,
              "hasPreviousPage": false
            }
        """.trimIndent()


        server.enqueue(
            MockResponse.Builder()
                .code(200)
                .addHeader("Content-Type", "application/json")
                .body(json)
                .build()
        )


        val result = repository.getNewestRooms()

        assertTrue(result is ApiResult.Success)

        val data = (result as ApiResult.Success).data

        assertNotNull(data)
        assertEquals(1, data!!.items.size)
        assertEquals(1, data.totalCount)

        val room = data.items.first()

        assertEquals(101L, room.roomId)

        assertEquals(
            "Phòng trọ gần trường",
            room.title
        )

        assertEquals(
            0,
            BigDecimal("2500000")
                .compareTo(room.rentPrice)
        )

        val request = server.takeRequest()

        assertEquals(
            "/api/rooms",
            request.url?.encodedPath
        )

        assertEquals(
            "newest",
            request.url
                ?.queryParameter("SortBy")
        )

        assertEquals(
            "1",
            request.url
                ?.queryParameter("Page")
        )

        assertEquals(
            "6",
            request.url
                ?.queryParameter("PageSize")
        )
    }

    @Test
    fun getNewestRooms_serverError() = runBlocking {


        server.enqueue(
            MockResponse.Builder()
                .code(500)
                .addHeader("Content-Type", "application/json")
                .body(
                    """{"errorCode":"SERVER_ERROR","message":"Có lỗi máy chủ."}"""
                )
                .build()
        )


        val result = repository.getNewestRooms()

        assertTrue(result is ApiResult.Error)

        val error = result as ApiResult.Error

        assertEquals(
            500,
            error.exception.httpStatus
        )
    }
}
