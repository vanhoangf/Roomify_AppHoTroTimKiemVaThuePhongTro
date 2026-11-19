package com.roomify.app.ui.student

import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope

import com.roomify.app.data.remote.api.RoomApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.rooms.RoomDetailResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.RoomRepository
import com.roomify.app.databinding.ActivityRoomMapBinding

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

import java.text.NumberFormat
import java.util.Locale

class RoomMapActivity :
    AppCompatActivity() {

    companion object {

        private const val MAP_STYLE =
            "https://demotiles.maplibre.org/style.json"
    }

    private lateinit var binding:
            ActivityRoomMapBinding

    private lateinit var repository:
            RoomRepository

    private var maplibreMap:
            MapLibreMap? = null

    private var isLoading =
        false

    /*
     * Marker của MapLibre không có tag như Google Marker,
     * nên map Marker.id -> RoomId.
     */
    private val markerRoomIds =
        mutableMapOf<Long, Long>()

    private val money =
        NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("vi-VN")
        )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        /*
         * Phải init MapLibre trước khi inflate MapView.
         * Không cần API key.
         */
        MapLibre.getInstance(this)

        binding =
            ActivityRoomMapBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        binding.mapView.onCreate(
            savedInstanceState
        )

        setupInsets()
        setupRepository()
        setupListeners()
        setupMap()
    }

    // =========================================================
    // INSETS
    // =========================================================

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.mapRoot
        ) { view, insets ->

            val bars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            view.setPadding(
                bars.left,
                bars.top,
                bars.right,
                bars.bottom
            )

            insets
        }
    }

    // =========================================================
    // REPOSITORY
    // =========================================================

    private fun setupRepository() {

        RetrofitClient.initialize(
            applicationContext
        )

        repository =
            RoomRepository(
                RetrofitClient.createService(
                    RoomApi::class.java
                )
            )
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    private fun setupListeners() {

        binding.btnBack
            .setOnClickListener {
                finish()
            }

        binding.btnRetry
            .setOnClickListener {

                loadRooms()
            }
    }

    // =========================================================
    // MAP
    // =========================================================

    private fun setupMap() {

        binding.mapView
            .getMapAsync { map ->

                maplibreMap =
                    map

                configureMap(
                    map
                )

                /*
                 * Đợi style tải xong rồi mới add marker.
                 */
                map.setStyle(
                    MAP_STYLE
                ) {

                    loadRooms()
                }
            }
    }

    private fun configureMap(
        map: MapLibreMap
    ) {

        map.uiSettings.apply {

            isCompassEnabled =
                true

            isZoomGesturesEnabled =
                true

            isScrollGesturesEnabled =
                true

            isRotateGesturesEnabled =
                true

            isTiltGesturesEnabled =
                true

            isLogoEnabled =
                true

            isAttributionEnabled =
                true
        }

        map.setOnInfoWindowClickListener { marker ->

            val roomId =
                markerRoomIds[
                    marker.id
                ]

            if (roomId != null) {

                openRoomDetail(
                    roomId
                )

                true

            } else {

                false
            }
        }
    }

    // =========================================================
    // LOAD ROOMS
    // =========================================================

    private fun loadRooms() {

        if (isLoading) {
            return
        }

        val map =
            maplibreMap
                ?: return

        isLoading =
            true

        binding.progressMap.isVisible =
            true

        binding.stateCard.isVisible =
            false

        binding.tvRoomCount.text =
            "Đang tải phòng..."

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.getNewestRooms(
                            page = 1,
                            pageSize = 20
                        )
                ) {

                    is ApiResult.Success -> {

                        val cards =
                            result.data
                                ?.items
                                .orEmpty()

                        /*
                         * RoomCard chưa có latitude/longitude.
                         * Lấy detail song song để lấy tọa độ.
                         */
                        val details =
                            cards.map { roomCard ->

                                async {

                                    when (
                                        val detailResult =
                                            repository
                                                .getRoomDetail(
                                                    roomCard.roomId
                                                )
                                    ) {

                                        is ApiResult.Success ->
                                            detailResult.data

                                        else ->
                                            null
                                    }
                                }

                            }.awaitAll()
                                .filterNotNull()

                        showRooms(
                            map,
                            details
                        )
                    }

                    is ApiResult.Error -> {

                        showError(
                            result.exception.message
                        )
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } finally {

                isLoading =
                    false

                binding.progressMap.isVisible =
                    false
            }
        }
    }

    // =========================================================
    // SHOW MARKERS
    // =========================================================

    @Suppress("DEPRECATION")
    private fun showRooms(
        map: MapLibreMap,
        rooms: List<RoomDetailResponse>
    ) {

        map.clear()

        markerRoomIds.clear()

        val validRooms =
            rooms.filter {

                it.latitude != null &&
                        it.longitude != null
            }

        binding.tvRoomCount.text =
            "${validRooms.size} phòng trên bản đồ"

        if (validRooms.isEmpty()) {

            showEmptyMap(
                map
            )

            return
        }

        binding.stateCard.isVisible =
            false

        validRooms.forEach { room ->

            val position =
                LatLng(
                    room.latitude!!.toDouble(),
                    room.longitude!!.toDouble()
                )

            val marker =
                map.addMarker(
                    MarkerOptions()
                        .position(
                            position
                        )
                        .title(
                            room.title
                        )
                        .snippet(
                            buildString {

                                append(
                                    money.format(
                                        room.rentPrice
                                    )
                                )

                                append(
                                    "/tháng"
                                )

                                if (
                                    room.wardName
                                        .isNotBlank()
                                ) {

                                    append(" • ")
                                    append(
                                        room.wardName
                                    )
                                }

                                append(
                                    "\nChạm để xem phòng"
                                )
                            }
                        )
                )

            markerRoomIds[
                marker.id
            ] =
                room.roomId
        }

        moveCameraToRooms(
            map,
            validRooms
        )
    }

    // =========================================================
    // CAMERA
    // =========================================================

    private fun moveCameraToRooms(
        map: MapLibreMap,
        rooms: List<RoomDetailResponse>
    ) {

        if (rooms.isEmpty()) {
            return
        }

        /*
         * Tính tâm trung bình của các phòng.
         * Với dữ liệu Roomify chủ yếu cùng khu vực,
         * cách này ổn định và đơn giản hơn bounds.
         */
        val averageLatitude =
            rooms.mapNotNull {
                it.latitude?.toDouble()
            }.average()

        val averageLongitude =
            rooms.mapNotNull {
                it.longitude?.toDouble()
            }.average()

        map.animateCamera(
            CameraUpdateFactory
                .newLatLngZoom(
                    LatLng(
                        averageLatitude,
                        averageLongitude
                    ),
                    13.0
                ),
            700
        )
    }

    // =========================================================
    // EMPTY
    // =========================================================

    private fun showEmptyMap(
        map: MapLibreMap
    ) {

        /*
         * Camera mặc định về Đà Nẵng.
         * Đây chỉ là camera fallback,
         * không phải vị trí hiện tại của người dùng.
         */
        map.animateCamera(
            CameraUpdateFactory
                .newLatLngZoom(
                    LatLng(
                        16.0544,
                        108.2022
                    ),
                    11.5
                )
        )

        binding.stateCard.isVisible =
            true

        binding.btnRetry.isVisible =
            false

        binding.tvState.text =
            "Hiện chưa có phòng nào có tọa độ bản đồ."
    }

    // =========================================================
    // ERROR
    // =========================================================

    private fun showError(
        message: String
    ) {

        binding.stateCard.isVisible =
            true

        binding.btnRetry.isVisible =
            true

        binding.tvState.text =
            message

        binding.tvRoomCount.text =
            "Không thể tải dữ liệu phòng"
    }

    // =========================================================
    // DETAIL
    // =========================================================

    private fun openRoomDetail(
        roomId: Long
    ) {

        startActivity(
            Intent(
                this,
                RoomDetailActivity::class.java
            ).apply {

                putExtra(
                    RoomDetailActivity.EXTRA_ROOM_ID,
                    roomId
                )
            }
        )
    }

    // =========================================================
    // MAPVIEW LIFECYCLE
    // =========================================================

    override fun onStart() {
        super.onStart()

        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()

        binding.mapView.onResume()
    }

    override fun onPause() {

        binding.mapView.onPause()

        super.onPause()
    }

    override fun onStop() {

        binding.mapView.onStop()

        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()

        binding.mapView.onLowMemory()
    }

    override fun onDestroy() {

        binding.mapView.onDestroy()

        super.onDestroy()
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {

        super.onSaveInstanceState(
            outState
        )

        binding.mapView.onSaveInstanceState(
            outState
        )
    }
}