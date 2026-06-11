package com.leopra.runningtrainer.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.leopra.runningtrainer.data.repository.SettingsRepository
import com.leopra.runningtrainer.data.repository.TrainingPlanRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AlarmManager alarms do not survive a reboot (or an app update), so this receiver
 * re-schedules workout reminders from the active plan when either happens.
 */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject lateinit var trainingPlanRepository: TrainingPlanRepository
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var notificationService: NotificationService

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = settingsRepository.observePreferences().first()
                if (!prefs.notificationsEnabled) return@launch
                val plan = trainingPlanRepository.observeActivePlan().firstOrNull() ?: return@launch
                notificationService.scheduleForPlan(plan, prefs.notificationHour, prefs.notificationMinute)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
