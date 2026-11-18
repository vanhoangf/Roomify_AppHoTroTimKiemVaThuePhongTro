package com.roomify.app.ui.student

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView

import com.roomify.app.databinding.ActivityRoomGalleryBinding

class RoomGalleryActivity :
    AppCompatActivity() {

    companion object {

        const val EXTRA_IMAGE_URLS =
            "extra_image_urls"

        const val EXTRA_START_POSITION =
            "extra_start_position"
    }

    private lateinit var binding:
            ActivityRoomGalleryBinding

    private lateinit var layoutManager:
            LinearLayoutManager

    private val snapHelper =
        PagerSnapHelper()

    private var imageUrls:
            List<String> =
        emptyList()

    private var startPosition =
        0

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        binding =
            ActivityRoomGalleryBinding
                .inflate(
                    layoutInflater
                )

        setContentView(
            binding.root
        )

        readIntent()
        setupWindowInsets()
        setupGallery()
        setupListeners()
    }

    // ========================================================
    // INTENT
    // ========================================================

    private fun readIntent() {

        imageUrls =
            intent.getStringArrayListExtra(
                EXTRA_IMAGE_URLS
            ) ?: arrayListOf()

        startPosition =
            intent.getIntExtra(
                EXTRA_START_POSITION,
                0
            )

        if (imageUrls.isEmpty()) {

            finish()

            return
        }

        startPosition =
            startPosition.coerceIn(
                imageUrls.indices
            )
    }

    // ========================================================
    // WINDOW INSETS
    // ========================================================

    private fun setupWindowInsets() {

        ViewCompat
            .setOnApplyWindowInsetsListener(
                binding.galleryRoot
            ) { view, insets ->

                val bars =
                    insets.getInsets(
                        WindowInsetsCompat
                            .Type.systemBars()
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

    // ========================================================
    // GALLERY
    // ========================================================

    private fun setupGallery() {

        if (imageUrls.isEmpty()) {
            return
        }

        layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )

        binding.rvGallery.layoutManager =
            layoutManager

        binding.rvGallery.adapter =
            RoomGalleryAdapter(
                imageUrls
            )

        snapHelper.attachToRecyclerView(
            binding.rvGallery
        )

        binding.rvGallery.scrollToPosition(
            startPosition
        )

        updateCounter(
            startPosition
        )

        binding.rvGallery
            .addOnScrollListener(
                object :
                    RecyclerView.OnScrollListener() {

                    override fun onScrollStateChanged(
                        recyclerView:
                        RecyclerView,
                        newState: Int
                    ) {

                        super
                            .onScrollStateChanged(
                                recyclerView,
                                newState
                            )

                        if (
                            newState ==
                            RecyclerView
                                .SCROLL_STATE_IDLE
                        ) {

                            updateCurrentPosition()
                        }
                    }
                }
            )
    }

    private fun updateCurrentPosition() {

        val snappedView =
            snapHelper.findSnapView(
                layoutManager
            ) ?: return

        val position =
            layoutManager
                .getPosition(
                    snappedView
                )

        if (
            position !=
            RecyclerView.NO_POSITION
        ) {

            updateCounter(
                position
            )
        }
    }

    private fun updateCounter(
        position: Int
    ) {

        binding.tvImageCounter.text =
            "${position + 1} / ${imageUrls.size}"
    }

    // ========================================================
    // LISTENER
    // ========================================================

    private fun setupListeners() {

        binding.btnBack
            .setOnClickListener {

                finish()
            }
    }
}