
package com.roomify.app.ui.student

import android.os.Bundle
import android.view.inputmethod.EditorInfo

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager

import com.roomify.app.data.remote.api.RoomApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.RoomRepository
import com.roomify.app.databinding.ActivityRoomSearchBinding

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RoomSearchActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_KEYWORD = "extra_keyword"
    }

    private lateinit var binding: ActivityRoomSearchBinding
    private lateinit var roomRepository: RoomRepository

    private val roomAdapter by lazy {
        StudentRoomAdapter()
    }

    private var searchJob: Job? = null
    private var searchVersion = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRoomSearchBinding.inflate(
            layoutInflater
        )

        setContentView(binding.root)

        applyWindowInsets()
        setupRepository()
        setupRecyclerView()
        setupListeners()

        // Có thể nhận từ khóa từ màn hình khác.
        val initialKeyword = intent
            .getStringExtra(EXTRA_KEYWORD)
            .orEmpty()

        binding.edtKeyword.setText(initialKeyword)

        // Mở màn hình là tải danh sách phòng ngay.
        scheduleSearch(immediate = true)
    }

    private fun applyWindowInsets() {

        val root = binding.roomSearchRoot

        val left = root.paddingLeft
        val top = root.paddingTop
        val right = root.paddingRight
        val bottom = root.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(
            root
        ) { view, insets ->

            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                left + bars.left,
                top + bars.top,
                right + bars.right,
                bottom + bars.bottom
            )

            insets
        }
    }

    private fun setupRepository() {

        RetrofitClient.initialize(applicationContext)

        val api = RetrofitClient.createService(
            RoomApi::class.java
        )

        roomRepository = RoomRepository(api)
    }

    private fun setupRecyclerView() {

        binding.rvSearchRooms.apply {

            layoutManager = LinearLayoutManager(
                this@RoomSearchActivity
            )

            adapter = roomAdapter

            setHasFixedSize(false)
        }
    }

    private fun setupListeners() {

        binding.btnBack.setOnClickListener {
            finish()
        }

        // Tìm tự động sau khi ngừng nhập một lúc.
        binding.edtKeyword.doAfterTextChanged {
            scheduleSearch(immediate = false)
        }

        // Nhấn nút Tìm.
        binding.btnSearch.setOnClickListener {
            hideKeyboard()
            scheduleSearch(immediate = true)
        }

        // Nhấn Search trên bàn phím.
        binding.edtKeyword.setOnEditorActionListener {
                _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                hideKeyboard()
                scheduleSearch(immediate = true)
                true

            } else {
                false
            }
        }

        binding.btnRetry.setOnClickListener {
            scheduleSearch(immediate = true)
        }
    }

    private fun hideKeyboard() {

        ViewCompat.getWindowInsetsController(
            binding.edtKeyword
        )?.hide(WindowInsetsCompat.Type.ime())

        binding.edtKeyword.clearFocus()
    }

    // ========================================================
    // SEARCH DEBOUNCE
    // ========================================================

    private fun scheduleSearch(
        immediate: Boolean = false
    ) {

        // Hủy truy vấn cũ khi người dùng thay từ khóa.
        searchJob?.cancel()

        val version = ++searchVersion

        val keyword = binding.edtKeyword.text
            ?.toString()
            ?.trim()
            .orEmpty()

        searchJob = lifecycleScope.launch {

            if (!immediate) {
                delay(450L)
            }

            loadRooms(
                keyword = keyword,
                version = version
            )
        }
    }

    // ========================================================
    // LOAD ROOMS FROM API
    // ========================================================

    private suspend fun loadRooms(
        keyword: String,
        version: Int
    ) {

        showLoading()

        try {

            val result = roomRepository.searchRooms(
                keyword = keyword.ifBlank { null },
                page = 1,
                pageSize = 20
            )

            // Bỏ kết quả của truy vấn đã lỗi thời.
            if (version != searchVersion) return

            when (result) {

                is ApiResult.Success -> {

                    val rooms = result.data
                        ?.items
                        .orEmpty()

                    val totalCount = result.data
                        ?.totalCount
                        ?: rooms.size

                    roomAdapter.submitList(rooms)

                    binding.tvResultCount.text =
                        "Hiển thị ${rooms.size}/$totalCount phòng"

                    if (rooms.isEmpty()) {

                        binding.rvSearchRooms.isVisible = false
                        binding.stateContainer.isVisible = true
                        binding.btnRetry.isVisible = false

                        binding.tvStateMessage.text =
                            if (keyword.isBlank()) {
                                "Hiện chưa có phòng được đăng công khai."
                            } else {
                                "Không tìm thấy phòng phù hợp với \"$keyword\"."
                            }

                    } else {

                        binding.rvSearchRooms.isVisible = true
                        binding.stateContainer.isVisible = false
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

        } catch (e: CancellationException) {
            throw e

        } catch (_: Exception) {

            if (version == searchVersion) {
                showError(
                    "Đã xảy ra lỗi khi tìm phòng.\n" +
                            "Vui lòng thử lại."
                )
            }

        } finally {

            if (version == searchVersion) {
                binding.progressSearch.isVisible = false
            }
        }
    }

    private fun showLoading() {

        binding.progressSearch.isVisible = true
        binding.rvSearchRooms.isVisible = false
        binding.stateContainer.isVisible = false
        binding.tvResultCount.text = "Đang tìm phòng..."
    }

    private fun showError(message: String) {

        roomAdapter.submitList(emptyList())

        binding.rvSearchRooms.isVisible = false
        binding.stateContainer.isVisible = true
        binding.btnRetry.isVisible = true

        binding.tvStateMessage.text = message
        binding.tvResultCount.text = "Không thể tải kết quả"
    }
}
