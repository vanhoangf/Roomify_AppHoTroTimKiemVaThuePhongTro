package com.roomify.app.ui.landlord.room

import android.net.Uri
import android.os.Bundle
import android.widget.Toast

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

import com.roomify.app.data.remote.api.LandlordRoomImageApi
import com.roomify.app.data.remote.common.ApiResult
import com.roomify.app.data.remote.dto.landlord.LandlordRoomImageResponse
import com.roomify.app.data.remote.network.RetrofitClient
import com.roomify.app.data.repository.LandlordRoomImageRepository
import com.roomify.app.databinding.ActivityLandlordRoomImagesBinding
import com.roomify.app.ui.common.RoleSessionGuard
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

import java.io.File
import java.util.Locale

class LandlordRoomImagesActivity :
    AppCompatActivity() {

    companion object {

        const val EXTRA_ROOM_ID =
            "extra_room_id"

        const val EXTRA_ROOM_NAME =
            "extra_room_name"

        const val EXTRA_ROOM_LISTING_STATUS =
            "extra_room_listing_status"

        private const val MAX_IMAGE_BYTES =
            10L * 1024L * 1024L
    }

    private var imageOrderChanged =
        false

    private var isSavingOrder =
        false

    private fun confirmSetPrimary(
        image: LandlordRoomImageResponse
    ) {

        MaterialAlertDialogBuilder(this)
            .setTitle(
                "Đặt làm ảnh đại diện?"
            )
            .setMessage(
                "Ảnh này sẽ trở thành ảnh chính của phòng."
            )
            .setNegativeButton(
                "Hủy",
                null
            )
            .setPositiveButton(
                "Đặt đại diện"
            ) { _, _ ->

                setPrimaryImage(
                    image
                )
            }
            .show()
    }

    private fun setPrimaryImage(
        image: LandlordRoomImageResponse
    ) {

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.setPrimary(
                            roomId,
                            image.imageId
                        )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@LandlordRoomImagesActivity,
                            "Đã đổi ảnh đại diện.",
                            Toast.LENGTH_SHORT
                        ).show()

                        loadImages()
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@LandlordRoomImagesActivity,
                            result.exception.message
                                ?: "Không thể đặt ảnh đại diện.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } catch (
                e: CancellationException
            ) {

                throw e

            } catch (
                e: Exception
            ) {

                Toast.makeText(
                    this@LandlordRoomImagesActivity,
                    e.message
                        ?: "Không thể đặt ảnh đại diện.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun confirmDeleteImage(
        image: LandlordRoomImageResponse
    ) {

        MaterialAlertDialogBuilder(this)
            .setTitle(
                "Xóa ảnh?"
            )
            .setMessage(
                if (image.isPrimary) {
                    "Đây là ảnh đại diện. Nếu xóa, hệ thống sẽ tự chọn ảnh tiếp theo làm ảnh đại diện."
                } else {
                    "Ảnh sẽ bị xóa khỏi phòng và Cloudinary."
                }
            )
            .setNegativeButton(
                "Hủy",
                null
            )
            .setPositiveButton(
                "Xóa"
            ) { _, _ ->

                deleteImage(
                    image
                )
            }
            .show()
    }

    private fun deleteImage(
        image: LandlordRoomImageResponse
    ) {

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.deleteImage(
                            roomId,
                            image.imageId
                        )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@LandlordRoomImagesActivity,
                            "Đã xóa ảnh.",
                            Toast.LENGTH_SHORT
                        ).show()

                        loadImages()
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@LandlordRoomImagesActivity,
                            result.exception.message
                                ?: "Không thể xóa ảnh.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } catch (
                e: CancellationException
            ) {

                throw e

            } catch (
                e: Exception
            ) {

                Toast.makeText(
                    this@LandlordRoomImagesActivity,
                    e.message
                        ?: "Không thể xóa ảnh.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private lateinit var binding:
            ActivityLandlordRoomImagesBinding

    private lateinit var repository:
            LandlordRoomImageRepository

    private val imageAdapter by lazy {

        LandlordRoomImageAdapter(
            onSetPrimary = { image ->

                if (!canEditImages()) {

                    Toast.makeText(
                        this,
                        "Tin phòng đang chờ duyệt nên không thể chỉnh sửa ảnh.",
                        Toast.LENGTH_LONG
                    ).show()

                } else {

                    confirmSetPrimary(
                        image
                    )
                }
            },

            onDelete = { image ->

                if (!canEditImages()) {

                    Toast.makeText(
                        this,
                        "Tin phòng đang chờ duyệt nên không thể xóa ảnh.",
                        Toast.LENGTH_LONG
                    ).show()

                } else {

                    confirmDeleteImage(
                        image
                    )
                }
            }
        )
    }

    private var roomId =
        0L

    private var roomName =
        ""

    private var listingStatus =
        ""

    private var isLoading =
        false

    private var isUploading =
        false

    private data class PreparedUpload(
        val parts: List<MultipartBody.Part>,
        val tempFiles: List<File>
    )

    private val imagePicker =
        registerForActivityResult(
            ActivityResultContracts
                .GetMultipleContents()
        ) { uris ->

            if (uris.isNotEmpty()) {

                uploadSelectedImages(
                    uris
                )
            }
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityLandlordRoomImagesBinding
                .inflate(
                    layoutInflater
                )

        setContentView(
            binding.root
        )

        roomId =
            intent.getLongExtra(
                EXTRA_ROOM_ID,
                0L
            )

        roomName =
            intent.getStringExtra(
                EXTRA_ROOM_NAME
            )
                ?.trim()
                .orEmpty()

        listingStatus =
            intent.getStringExtra(
                EXTRA_ROOM_LISTING_STATUS
            )
                ?.trim()
                .orEmpty()

        if (roomId <= 0L) {

            Toast.makeText(
                this,
                "Không tìm thấy phòng.",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        setupInsets()
        setupRepository()
        setupRecyclerView()
        setupImageDragAndDrop()
        setupActions()

        binding.tvRoomName.text =
            roomName.ifBlank {
                "Phòng trọ"
            }

        loadImages()
    }

    private fun setupImageDragAndDrop() {

        val callback =
            object :
                ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP or
                            ItemTouchHelper.DOWN or
                            ItemTouchHelper.LEFT or
                            ItemTouchHelper.RIGHT,
                    0
                ) {

                override fun isLongPressDragEnabled():
                        Boolean {

                    return canEditImages() &&
                            !isUploading &&
                            !isSavingOrder
                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder:
                    RecyclerView.ViewHolder,
                    target:
                    RecyclerView.ViewHolder
                ): Boolean {

                    if (
                        !canEditImages() ||
                        isUploading ||
                        isSavingOrder
                    ) {
                        return false
                    }

                    val fromPosition =
                        viewHolder
                            .bindingAdapterPosition

                    val toPosition =
                        target
                            .bindingAdapterPosition

                    if (
                        fromPosition ==
                        RecyclerView.NO_POSITION ||
                        toPosition ==
                        RecyclerView.NO_POSITION
                    ) {
                        return false
                    }

                    val moved =
                        imageAdapter.moveImage(
                            fromPosition,
                            toPosition
                        )

                    if (moved) {

                        imageOrderChanged =
                            true
                    }

                    return moved
                }

                override fun onSwiped(
                    viewHolder:
                    RecyclerView.ViewHolder,
                    direction: Int
                ) {
                    // Không sử dụng swipe.
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder:
                    RecyclerView.ViewHolder
                ) {

                    super.clearView(
                        recyclerView,
                        viewHolder
                    )

                    if (imageOrderChanged) {

                        imageOrderChanged =
                            false

                        saveImageOrder()
                    }
                }
            }

        ItemTouchHelper(
            callback
        ).attachToRecyclerView(
            binding.rvRoomImages
        )
    }

    private fun saveImageOrder() {

        if (
            isSavingOrder ||
            !canEditImages()
        ) {
            return
        }

        val images =
            imageAdapter.getImages()

        if (images.size < 2) {
            return
        }

        isSavingOrder =
            true

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository
                            .reorderImages(
                                roomId,
                                images
                            )
                ) {

                    is ApiResult.Success -> {

                        val updatedImages =
                            sortImages(
                                result.data
                                    .orEmpty()
                            )

                        imageAdapter
                            .submitImages(
                                updatedImages
                            )

                        Toast.makeText(
                            this@LandlordRoomImagesActivity,
                            "Đã lưu thứ tự ảnh.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@LandlordRoomImagesActivity,
                            result.exception.message
                                ?: "Không thể lưu thứ tự ảnh.",
                            Toast.LENGTH_LONG
                        ).show()

                        // Lấy lại thứ tự thật từ server.
                        loadImages()
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } catch (
                e: CancellationException
            ) {

                throw e

            } catch (
                e: Exception
            ) {

                Toast.makeText(
                    this@LandlordRoomImagesActivity,
                    e.message
                        ?: "Không thể lưu thứ tự ảnh.",
                    Toast.LENGTH_LONG
                ).show()

                loadImages()

            } finally {

                isSavingOrder =
                    false
            }
        }
    }

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.landlordRoomImagesRoot
        ) { view, insets ->

            val bars =
                insets.getInsets(
                    WindowInsetsCompat.Type
                        .systemBars()
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

    private fun setupRepository() {

        RetrofitClient.initialize(
            applicationContext
        )

        repository =
            LandlordRoomImageRepository(
                RetrofitClient.createService(
                    LandlordRoomImageApi::class.java
                )
            )
    }

    private fun setupRecyclerView() {

        binding.rvRoomImages.apply {

            layoutManager =
                GridLayoutManager(
                    this@LandlordRoomImagesActivity,
                    2
                )

            adapter =
                imageAdapter

            setHasFixedSize(
                false
            )
        }
    }

    private fun setupActions() {

        binding.btnBack
            .setOnClickListener {
                finish()
            }

        binding.btnRetry
            .setOnClickListener {
                loadImages()
            }

        binding.btnAddImages
            .setOnClickListener {

                if (!canEditImages()) {

                    Toast.makeText(
                        this,
                        "Tin phòng đang chờ duyệt nên không thể thêm ảnh.",
                        Toast.LENGTH_LONG
                    ).show()

                    return@setOnClickListener
                }

                imagePicker.launch(
                    "image/*"
                )
            }
    }

    private fun canEditImages(): Boolean {

        return !listingStatus.equals(
            "PENDING",
            ignoreCase = true
        )
    }

    private fun loadImages() {

        if (isLoading) {
            return
        }

        isLoading =
            true

        showLoading()

        lifecycleScope.launch {

            try {

                when (
                    val result =
                        repository.getImages(
                            roomId
                        )
                ) {

                    is ApiResult.Success -> {

                        val images =
                            sortImages(
                                result.data
                                    .orEmpty()
                            )

                        if (images.isEmpty()) {

                            showEmpty()

                        } else {

                            showImages(
                                images
                            )
                        }
                    }

                    is ApiResult.Error -> {

                        showError(
                            result.exception.message
                                ?: "Không thể tải ảnh phòng."
                        )
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } catch (
                e: CancellationException
            ) {

                throw e

            } catch (
                e: Exception
            ) {

                showError(
                    e.message
                        ?: "Không thể tải ảnh phòng."
                )

            } finally {

                isLoading =
                    false
            }
        }
    }

    private fun uploadSelectedImages(
        uris: List<Uri>
    ) {

        if (isUploading) {
            return
        }

        if (!canEditImages()) {

            Toast.makeText(
                this,
                "Tin phòng đang chờ duyệt nên không thể thêm ảnh.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        isUploading =
            true

        updateAddButtonState()

        lifecycleScope.launch {

            var tempFiles:
                    List<File> =
                emptyList()

            try {

                val prepared =
                    prepareUpload(
                        uris
                    )

                tempFiles =
                    prepared.tempFiles

                when (
                    val result =
                        repository.uploadImages(
                            roomId =
                                roomId,

                            files =
                                prepared.parts
                        )
                ) {

                    is ApiResult.Success -> {

                        Toast.makeText(
                            this@LandlordRoomImagesActivity,
                            "Đã tải ${uris.size} ảnh lên phòng.",
                            Toast.LENGTH_SHORT
                        ).show()

                        val images =
                            sortImages(
                                result.data
                                    .orEmpty()
                            )

                        if (images.isEmpty()) {

                            showEmpty()

                        } else {

                            showImages(
                                images
                            )
                        }
                    }

                    is ApiResult.Error -> {

                        Toast.makeText(
                            this@LandlordRoomImagesActivity,
                            result.exception.message
                                ?: "Không thể tải ảnh lên.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    ApiResult.Loading ->
                        Unit
                }

            } catch (
                e: CancellationException
            ) {

                throw e

            } catch (
                e: IllegalArgumentException
            ) {

                Toast.makeText(
                    this@LandlordRoomImagesActivity,
                    e.message
                        ?: "Ảnh không hợp lệ.",
                    Toast.LENGTH_LONG
                ).show()

            } catch (
                e: Exception
            ) {

                Toast.makeText(
                    this@LandlordRoomImagesActivity,
                    e.message
                        ?: "Không thể tải ảnh lên.",
                    Toast.LENGTH_LONG
                ).show()

            } finally {

                tempFiles.forEach {
                    runCatching {
                        it.delete()
                    }
                }

                isUploading =
                    false

                updateAddButtonState()
            }
        }
    }

    private suspend fun prepareUpload(
        uris: List<Uri>
    ): PreparedUpload {

        return withContext(
            Dispatchers.IO
        ) {

            val parts =
                mutableListOf<
                        MultipartBody.Part
                        >()

            val tempFiles =
                mutableListOf<File>()

            try {

                uris.forEachIndexed {
                        index,
                        uri ->

                    val originalMime =
                        contentResolver
                            .getType(
                                uri
                            )
                            ?.lowercase(
                                Locale.ROOT
                            )

                    val mimeType =
                        normalizeMimeType(
                            originalMime
                        )

                    val extension =
                        when (mimeType) {

                            "image/jpeg" ->
                                "jpg"

                            "image/png" ->
                                "png"

                            "image/webp" ->
                                "webp"

                            else ->
                                throw IllegalArgumentException(
                                    "Định dạng ảnh không được hỗ trợ."
                                )
                        }

                    val tempFile =
                        File.createTempFile(
                            "landlord_room_${roomId}_${index}_",
                            ".$extension",
                            cacheDir
                        )

                    tempFiles.add(
                        tempFile
                    )

                    val inputStream =
                        contentResolver
                            .openInputStream(
                                uri
                            )
                            ?: throw IllegalArgumentException(
                                "Không thể đọc ảnh đã chọn."
                            )

                    inputStream.use {
                            input ->

                        tempFile
                            .outputStream()
                            .use {
                                    output ->

                                input.copyTo(
                                    output
                                )
                            }
                    }

                    if (
                        tempFile.length() <= 0L
                    ) {

                        throw IllegalArgumentException(
                            "Có ảnh không chứa dữ liệu."
                        )
                    }

                    if (
                        tempFile.length() >
                        MAX_IMAGE_BYTES
                    ) {

                        throw IllegalArgumentException(
                            "Mỗi ảnh không được vượt quá 10 MB."
                        )
                    }

                    val requestBody =
                        tempFile.asRequestBody(
                            mimeType.toMediaType()
                        )

                    val part =
                        MultipartBody.Part
                            .createFormData(
                                "files",
                                tempFile.name,
                                requestBody
                            )

                    parts.add(
                        part
                    )
                }

                PreparedUpload(
                    parts =
                        parts,

                    tempFiles =
                        tempFiles
                )

            } catch (
                e: Exception
            ) {

                tempFiles.forEach {
                    runCatching {
                        it.delete()
                    }
                }

                throw e
            }
        }
    }

    private fun normalizeMimeType(
        mimeType: String?
    ): String {

        return when (
            mimeType
        ) {

            "image/jpeg",
            "image/jpg" ->
                "image/jpeg"

            "image/png" ->
                "image/png"

            "image/webp" ->
                "image/webp"

            else ->
                throw IllegalArgumentException(
                    "Chỉ hỗ trợ ảnh JPEG, PNG hoặc WEBP."
                )
        }
    }

    private fun sortImages(
        images:
        List<LandlordRoomImageResponse>
    ): List<LandlordRoomImageResponse> {

        return images.sortedWith(
            compareByDescending<
                    LandlordRoomImageResponse
                    > {
                it.isPrimary
            }
                .thenBy {
                    it.sortOrder
                }
                .thenBy {
                    it.imageId
                }
        )
    }

    private fun updateAddButtonState() {

        binding.btnAddImages.isEnabled =
            canEditImages() &&
                    !isUploading

        binding.btnAddImages.text =
            when {

                isUploading ->
                    "Đang tải ảnh..."

                !canEditImages() ->
                    "Đang chờ duyệt"

                else ->
                    "Thêm ảnh"
            }
    }

    private fun showLoading() {

        binding.progressRoomImages
            .isVisible =
            true

        binding.rvRoomImages
            .isVisible =
            false

        binding.emptyContainer
            .isVisible =
            false

        binding.errorContainer
            .isVisible =
            false

        binding.btnAddImages
            .isVisible =
            false
    }

    private fun showImages(
        images:
        List<LandlordRoomImageResponse>
    ) {

        binding.progressRoomImages
            .isVisible =
            false

        binding.emptyContainer
            .isVisible =
            false

        binding.errorContainer
            .isVisible =
            false

        binding.rvRoomImages
            .isVisible =
            true

        binding.btnAddImages
            .isVisible =
            true

        imageAdapter.submitImages(
            images
        )

        updateAddButtonState()
    }

    private fun showEmpty() {

        binding.progressRoomImages
            .isVisible =
            false

        binding.rvRoomImages
            .isVisible =
            false

        binding.errorContainer
            .isVisible =
            false

        binding.emptyContainer
            .isVisible =
            true

        binding.btnAddImages
            .isVisible =
            true

        imageAdapter.submitImages(
            emptyList()
        )

        updateAddButtonState()
    }

    private fun showError(
        message: String
    ) {

        binding.progressRoomImages
            .isVisible =
            false

        binding.rvRoomImages
            .isVisible =
            false

        binding.emptyContainer
            .isVisible =
            false

        binding.errorContainer
            .isVisible =
            true

        binding.btnAddImages
            .isVisible =
            false

        binding.tvErrorMessage.text =
            message
    }

    override fun onStart() {
        super.onStart()

        RoleSessionGuard.verify(
            activity = this,
            expectedRole = "LANDLORD",
            onNetworkError = { message ->

                Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }
}