package com.roomify.app.ui.student

import android.app.Activity
import android.content.Intent

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.roomify.app.R

object StudentBottomNavHelper {

    fun setup(
        activity: Activity,
        bottomNavigation: BottomNavigationView,
        selectedItemId: Int
    ) {

        // Bật toàn bộ 5 tab Student
        bottomNavigation.menu
            .findItem(R.id.navHome)
            ?.isEnabled = true

        bottomNavigation.menu
            .findItem(R.id.navSearch)
            ?.isEnabled = true

        bottomNavigation.menu
            .findItem(R.id.navSaved)
            ?.isEnabled = true

        bottomNavigation.menu
            .findItem(R.id.navMessages)
            ?.isEnabled = true

        bottomNavigation.menu
            .findItem(R.id.navAccount)
            ?.isEnabled = true

        // Đánh dấu tab hiện tại
        bottomNavigation.selectedItemId =
            selectedItemId

        bottomNavigation
            .setOnItemSelectedListener { item ->

                // Nhấn lại tab hiện tại
                if (item.itemId == selectedItemId) {
                    return@setOnItemSelectedListener true
                }

                val intent =
                    when (item.itemId) {

                        R.id.navHome ->
                            Intent(
                                activity,
                                StudentHomeActivity::class.java
                            )

                        R.id.navSearch ->
                            Intent(
                                activity,
                                RoomSearchActivity::class.java
                            )

                        R.id.navSaved ->
                            Intent(
                                activity,
                                SavedRoomsActivity::class.java
                            )

                        R.id.navMessages ->
                            Intent(
                                activity,
                                ConversationsActivity::class.java
                            )

                        R.id.navAccount ->
                            Intent(
                                activity,
                                StudentAccountActivity::class.java
                            )

                        else ->
                            null
                    }

                if (intent == null) {
                    return@setOnItemSelectedListener false
                }

                /*
                 * Nếu Activity đã tồn tại trong back stack
                 * thì đưa nó lên trước thay vì tạo vô hạn Activity mới.
                 */
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                )

                activity.startActivity(
                    intent
                )

                // Chuyển tab không cần animation Activity
                activity.overridePendingTransition(
                    0,
                    0
                )

                false
            }
    }
}