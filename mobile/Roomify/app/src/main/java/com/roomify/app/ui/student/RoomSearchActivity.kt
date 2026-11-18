package com.roomify.app.ui.student

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager

import com.roomify.app.R
import com.roomify.app.data.remote.api.RoomApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.RoomRepository
import com.roomify.app.databinding.ActivityRoomSearchBinding

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import java.math.BigDecimal

class RoomSearchActivity :
    AppCompatActivity() {

    companion object {

        const val EXTRA_KEYWORD =
            "extra_keyword"
    }

    private lateinit var binding:
            ActivityRoomSearchBinding

    private lateinit var roomRepository:
            RoomRepository

    private val roomAdapter by lazy {

        StudentRoomAdapter(
            onRoomClick = { room ->

                openRoomDetail(
                    room.roomId
                )
            }
        )
    }

    private var searchJob: Job? = null

    private var searchVersion = 0

    // ========================================================
    // FILTER STATE
    // ========================================================

    private var filterProvinceId:
            Int? = null

    private var filterWardId:
            Int? = null

    private var filterPropertyTypeId:
            Int? = null

    private var filterMinPrice:
            Long? = null

    private var filterMaxPrice:
            Long? = null

    private var filterMinArea:
            BigDecimal? = null

    private var filterMaxArea:
            BigDecimal? = null

    private var filterAmenityIds:
            List<Int> =
        emptyList()

    // ========================================================
    // FILTER RESULT
    // ========================================================

    private val filterLauncher =
        registerForActivityResult(
            ActivityResultContracts
                .StartActivityForResult()
        ) { result ->

            if (
                result.resultCode !=
                RESULT_OK
            ) {
                return@registerForActivityResult
            }

            val data =
                result.data
                    ?: return@registerForActivityResult

            readFilterResult(data)

            updateFilterButton()

            scheduleSearch(
                immediate = true
            )
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityRoomSearchBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        applyWindowInsets()

        ViewCompat
            .setOnApplyWindowInsetsListener(
                binding.bottomNavigation
            ) { view, insets ->

                view.setPadding(
                    view.paddingLeft,
                    view.paddingTop,
                    view.paddingRight,
                    0
                )

                insets
            }

        setupRepository()
        setupRecyclerView()
        setupListeners()
        setupBottomNavigation()

        val initialKeyword =
            intent.getStringExtra(
                EXTRA_KEYWORD
            ).orEmpty()

        binding.edtKeyword.setText(
            initialKeyword
        )

        updateFilterButton()

        scheduleSearch(
            immediate = true
        )
    }

    // ========================================================
    // WINDOW INSETS
    // ========================================================

    private fun applyWindowInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.roomSearchRoot
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

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.bottomNavigation
        ) { view, insets ->

            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                0
            )

            insets
        }
    }

    // ========================================================
    // REPOSITORY
    // ========================================================

    private fun setupRepository() {

        RetrofitClient.initialize(
            applicationContext
        )

        val api =
            RetrofitClient.createService(
                RoomApi::class.java
            )

        roomRepository =
            RoomRepository(api)
    }

    // ========================================================
    // RECYCLER
    // ========================================================

    private fun setupRecyclerView() {

        binding.rvSearchRooms.apply {

            layoutManager =
                LinearLayoutManager(
                    this@RoomSearchActivity
                )

            adapter =
                roomAdapter

            setHasFixedSize(false)
        }
    }
    private fun openRoomDetail(
        roomId: Long
    ) {

        val intent =
            Intent(
                this,
                RoomDetailActivity::class.java
            )

        intent.putExtra(
            RoomDetailActivity.EXTRA_ROOM_ID,
            roomId
        )

        startActivity(
            intent
        )
    }
    // ========================================================
    // LISTENERS
    // ========================================================

    private fun setupListeners() {

        binding.edtKeyword
            .doAfterTextChanged {

                scheduleSearch(
                    immediate = false
                )
            }

        binding.btnSearch
            .setOnClickListener {

                hideKeyboard()

                scheduleSearch(
                    immediate = true
                )
            }

        binding.btnFilter
            .setOnClickListener {

                openFilter()
            }

        binding.edtKeyword
            .setOnEditorActionListener {
                    _, actionId, _ ->

                if (
                    actionId ==
                    EditorInfo
                        .IME_ACTION_SEARCH
                ) {

                    hideKeyboard()

                    scheduleSearch(
                        immediate = true
                    )

                    true

                } else {

                    false
                }
            }

        binding.btnRetry
            .setOnClickListener {

                scheduleSearch(
                    immediate = true
                )
            }
    }

    // ========================================================
    // OPEN FILTER
    // ========================================================

    private fun openFilter() {

        val intent =
            Intent(
                this,
                RoomFilterActivity::class.java
            )

        filterProvinceId?.let {

            intent.putExtra(
                RoomFilterActivity
                    .EXTRA_PROVINCE_ID,
                it
            )
        }

        filterWardId?.let {

            intent.putExtra(
                RoomFilterActivity
                    .EXTRA_WARD_ID,
                it
            )
        }

        filterPropertyTypeId?.let {

            intent.putExtra(
                RoomFilterActivity
                    .EXTRA_PROPERTY_TYPE_ID,
                it
            )
        }

        filterMinPrice?.let {

            intent.putExtra(
                RoomFilterActivity
                    .EXTRA_MIN_PRICE,
                it
            )
        }

        filterMaxPrice?.let {

            intent.putExtra(
                RoomFilterActivity
                    .EXTRA_MAX_PRICE,
                it
            )
        }

        filterMinArea?.let {

            intent.putExtra(
                RoomFilterActivity
                    .EXTRA_MIN_AREA,
                it.toPlainString()
            )
        }

        filterMaxArea?.let {

            intent.putExtra(
                RoomFilterActivity
                    .EXTRA_MAX_AREA,
                it.toPlainString()
            )
        }

        intent.putExtra(
            RoomFilterActivity
                .EXTRA_AMENITY_IDS,
            filterAmenityIds
                .toIntArray()
        )

        filterLauncher.launch(
            intent
        )
    }

    // ========================================================
    // READ FILTER RESULT
    // ========================================================

    private fun readFilterResult(
        data: Intent
    ) {

        filterProvinceId =
            data.getIntExtraOrNull(
                RoomFilterActivity
                    .EXTRA_PROVINCE_ID
            )

        filterWardId =
            data.getIntExtraOrNull(
                RoomFilterActivity
                    .EXTRA_WARD_ID
            )

        filterPropertyTypeId =
            data.getIntExtraOrNull(
                RoomFilterActivity
                    .EXTRA_PROPERTY_TYPE_ID
            )

        filterMinPrice =
            data.getLongExtraOrNull(
                RoomFilterActivity
                    .EXTRA_MIN_PRICE
            )

        filterMaxPrice =
            data.getLongExtraOrNull(
                RoomFilterActivity
                    .EXTRA_MAX_PRICE
            )

        filterMinArea =
            data.getStringExtra(
                RoomFilterActivity
                    .EXTRA_MIN_AREA
            )?.toBigDecimalOrNull()

        filterMaxArea =
            data.getStringExtra(
                RoomFilterActivity
                    .EXTRA_MAX_AREA
            )?.toBigDecimalOrNull()

        filterAmenityIds =
            data.getIntArrayExtra(
                RoomFilterActivity
                    .EXTRA_AMENITY_IDS
            )
                ?.toList()
                .orEmpty()
    }

    // ========================================================
    // FILTER BUTTON
    // ========================================================

    private fun updateFilterButton() {

        val count =
            activeFilterCount()

        binding.btnFilter.text =
            if (count == 0) {

                "Bộ lọc"

            } else {

                "Bộ lọc ($count)"
            }
    }

    private fun activeFilterCount():
            Int {

        var count = 0

        if (filterProvinceId != null)
            count++

        if (filterWardId != null)
            count++

        if (filterPropertyTypeId != null)
            count++

        if (filterMinPrice != null)
            count++

        if (filterMaxPrice != null)
            count++

        if (filterMinArea != null)
            count++

        if (filterMaxArea != null)
            count++

        if (filterAmenityIds.isNotEmpty())
            count++

        return count
    }

    // ========================================================
    // BOTTOM NAVIGATION
    // ========================================================

    private fun setupBottomNavigation() {

        StudentBottomNavHelper.setup(
            activity = this,
            bottomNavigation =
                binding.bottomNavigation,
            selectedItemId =
                R.id.navSearch
        )
    }

    // ========================================================
    // KEYBOARD
    // ========================================================

    private fun hideKeyboard() {

        ViewCompat
            .getWindowInsetsController(
                binding.edtKeyword
            )
            ?.hide(
                WindowInsetsCompat
                    .Type.ime()
            )

        binding.edtKeyword
            .clearFocus()
    }

    // ========================================================
    // SEARCH
    // ========================================================

    private fun scheduleSearch(
        immediate: Boolean = false
    ) {

        searchJob?.cancel()

        val version =
            ++searchVersion

        val keyword =
            binding.edtKeyword.text
                ?.toString()
                ?.trim()
                .orEmpty()

        searchJob =
            lifecycleScope.launch {

                if (!immediate) {
                    delay(450L)
                }

                loadRooms(
                    keyword,
                    version
                )
            }
    }

    // ========================================================
    // LOAD ROOMS
    // ========================================================

    private suspend fun loadRooms(
        keyword: String,
        version: Int
    ) {

        showLoading()

        try {

            val result =
                roomRepository.searchRooms(

                    keyword =
                        keyword.ifBlank {
                            null
                        },

                    provinceId =
                        filterProvinceId,

                    wardId =
                        filterWardId,

                    propertyTypeId =
                        filterPropertyTypeId,

                    minPrice =
                        filterMinPrice,

                    maxPrice =
                        filterMaxPrice,

                    minArea =
                        filterMinArea,

                    maxArea =
                        filterMaxArea,

                    amenityIds =
                        filterAmenityIds,

                    page = 1,

                    pageSize = 20
                )

            if (
                version !=
                searchVersion
            ) {
                return
            }

            when (result) {

                is ApiResult.Success -> {

                    val rooms =
                        result.data
                            ?.items
                            .orEmpty()

                    val totalCount =
                        result.data
                            ?.totalCount
                            ?: rooms.size

                    roomAdapter
                        .submitList(
                            rooms
                        )

                    binding.tvResultCount.text =
                        "Hiển thị " +
                                "${rooms.size}/" +
                                "$totalCount phòng"

                    if (rooms.isEmpty()) {

                        binding.rvSearchRooms
                            .isVisible = false

                        binding.stateContainer
                            .isVisible = true

                        binding.btnRetry
                            .isVisible = false

                        binding.tvStateMessage.text =
                            when {

                                activeFilterCount() >
                                        0 ->

                                    "Không tìm thấy phòng phù hợp với bộ lọc."

                                keyword.isNotBlank() ->

                                    "Không tìm thấy phòng phù hợp với \"$keyword\"."

                                else ->

                                    "Hiện chưa có phòng được đăng công khai."
                            }

                    } else {

                        binding.rvSearchRooms
                            .isVisible = true

                        binding.stateContainer
                            .isVisible = false
                    }
                }

                is ApiResult.Error -> {

                    showError(
                        "Không thể tải danh sách phòng.\n" +
                                "Vui lòng kiểm tra kết nối và thử lại."
                    )
                }

                ApiResult.Loading -> Unit
            }

        } catch (
            e: CancellationException
        ) {

            throw e

        } catch (_: Exception) {

            if (
                version ==
                searchVersion
            ) {

                showError(
                    "Đã xảy ra lỗi khi tìm phòng.\n" +
                            "Vui lòng thử lại."
                )
            }

        } finally {

            if (
                version ==
                searchVersion
            ) {

                binding.progressSearch
                    .isVisible = false
            }
        }
    }

    // ========================================================
    // UI STATE
    // ========================================================

    private fun showLoading() {

        binding.progressSearch
            .isVisible = true

        binding.rvSearchRooms
            .isVisible = false

        binding.stateContainer
            .isVisible = false

        binding.tvResultCount.text =
            "Đang tìm phòng..."
    }

    private fun showError(
        message: String
    ) {

        roomAdapter
            .submitList(
                emptyList()
            )

        binding.rvSearchRooms
            .isVisible = false

        binding.stateContainer
            .isVisible = true

        binding.btnRetry
            .isVisible = true

        binding.tvStateMessage.text =
            message

        binding.tvResultCount.text =
            "Không thể tải kết quả"
    }

    // ========================================================
    // INTENT HELPERS
    // ========================================================

    private fun Intent.getIntExtraOrNull(
        key: String
    ): Int? {

        return if (hasExtra(key)) {

            getIntExtra(
                key,
                0
            )

        } else {

            null
        }
    }

    private fun Intent.getLongExtraOrNull(
        key: String
    ): Long? {

        return if (hasExtra(key)) {

            getLongExtra(
                key,
                0L
            )

        } else {

            null
        }
    }
}