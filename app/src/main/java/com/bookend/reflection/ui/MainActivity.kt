package com.bookend.reflection.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bookend.reflection.data.DayPart
import com.bookend.reflection.reminder.Notifications
import com.bookend.reflection.ui.theme.BookendTheme

class MainActivity : ComponentActivity() {

    /** Set when the activity is opened by tapping a reminder. */
    private val launchPart = mutableStateOf<DayPart?>(null)

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* handled below */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        launchPart.value = partFrom(intent)

        setContent {
            BookendTheme {
                val viewModel: AppViewModel = viewModel(factory = AppViewModel.Factory)
                BookendRoot(
                    viewModel = viewModel,
                    launchPart = launchPart.value,
                    onLaunchPartHandled = { launchPart.value = null },
                    onRequestNotificationPermission = ::askForNotificationPermission,
                )
            }
        }

        askForNotificationPermission()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        launchPart.value = partFrom(intent)
    }

    private fun askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && !Notifications.canPostNotifications(this)) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun partFrom(intent: Intent?): DayPart? =
        DayPart.fromNameOrNull(intent?.getStringExtra(Notifications.EXTRA_PART))
}
