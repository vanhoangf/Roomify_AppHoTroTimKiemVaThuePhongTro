
package com.roomify.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log

import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope

import com.roomify.app.data.remote.api.HealthApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.common.safeApiCall
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.databinding.ActivityMainBinding

import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val TAG = "RoomifyNetwork"
    }

    // ============================================================
    // 1. XỬ LÝ KẾT QUẢ YÊU CẦU QUYỀN MẠNG CỤC BỘ
    // ============================================================

    private val localNetworkPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->

            if (isGranted) {

                Log.d(
                    TAG,
                    "ACCESS_LOCAL_NETWORK granted"
                )

                testHealthApi()

            } else {

                Log.e(
                    TAG,
                    "ACCESS_LOCAL_NETWORK denied"
                )
            }
        }

    // ============================================================
    // 2. KHỞI TẠO ACTIVITY
    // ============================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Giữ nguyên xử lý WindowInsets
        ViewCompat.setOnApplyWindowInsetsListener(
            binding.root
        ) { view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        // Chỉ kiểm tra API trong bản Debug
        if (BuildConfig.DEBUG) {

            checkLocalNetworkPermissionThenTest()

        }
    }

    // ============================================================
    // 3. KIỂM TRA QUYỀN TRUY CẬP MẠNG CỤC BỘ
    // ============================================================

    private fun checkLocalNetworkPermissionThenTest() {

        // Android 17 - API 37 trở lên
        if (Build.VERSION.SDK_INT >= 37) {

            val permissionStatus =
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_LOCAL_NETWORK
                )

            if (
                permissionStatus !=
                PackageManager.PERMISSION_GRANTED
            ) {

                Log.d(
                    TAG,
                    "Requesting ACCESS_LOCAL_NETWORK"
                )

                localNetworkPermissionLauncher.launch(
                    Manifest.permission.ACCESS_LOCAL_NETWORK
                )

                return
            }

            Log.d(
                TAG,
                "ACCESS_LOCAL_NETWORK already granted"
            )
        }

        // Có quyền hoặc chạy phiên bản Android cũ hơn
        testHealthApi()
    }

    // ============================================================
    // 4. TEST RETROFIT VỚI ROOMIFY.API
    // ============================================================

    private fun testHealthApi() {

        Log.d(
            TAG,
            "BASE_URL = ${BuildConfig.BASE_URL}"
        )

        // Khởi tạo RetrofitClient
        RetrofitClient.initialize(applicationContext)

        // Tạo HealthApi
        val healthApi = RetrofitClient.createService(
            HealthApi::class.java
        )

        // Gọi API bằng Coroutine
        lifecycleScope.launch {

            Log.d(
                TAG,
                "Calling GET /api/health"
            )

            val result = safeApiCall {
                healthApi.getHealth()
            }

            when (result) {

                is ApiResult.Success -> {

                    Log.d(
                        TAG,
                        "HTTP Status: ${result.statusCode}"
                    )

                    Log.d(
                        TAG,
                        "API Status: ${result.data?.status}"
                    )

                    Log.d(
                        TAG,
                        "Database: ${result.data?.database}"
                    )
                }

                is ApiResult.Error -> {

                    Log.e(
                        TAG,
                        "HTTP: ${result.exception.httpStatus}, " +
                                "Error: ${result.exception.errorCode}, " +
                                "Message: ${result.exception.message}",
                        result.exception
                    )
                }

                ApiResult.Loading -> Unit
            }
        }
    }
}
