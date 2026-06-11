package com.leopra.runningtrainer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leopra.runningtrainer.data.repository.SettingsRepository
import com.leopra.runningtrainer.domain.model.TrainingPlan
import com.leopra.runningtrainer.notifications.NotificationService
import com.leopra.runningtrainer.ui.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Owns all write operations on user preferences/settings.
 * Navigation intent after save is published via [navigationEvent]; MainActivity bridges it to MainViewModel.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val notificationService: NotificationService
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<AppDestination>(extraBufferCapacity = 1)
    val navigationEvent: SharedFlow<AppDestination> = _navigationEvent.asSharedFlow()

    fun saveSettings(
        name: String,
        age: String,
        weightKg: String,
        heightCm: String,
        useKilometers: Boolean,
        claudeApiKey: String,
        notificationsEnabled: Boolean,
        notificationHour: Int,
        notificationMinute: Int,
        localeCode: String = "en",
        activePlan: TrainingPlan? = null
    ) {
        viewModelScope.launch {
            val current = settingsRepository.observePreferences().first()
            val updated = current.copy(
                name = name.trim().ifBlank { null },
                age = age.toIntOrNull(),
                weightKg = weightKg.toDoubleOrNull(),
                heightCm = heightCm.toDoubleOrNull(),
                useKilometers = useKilometers,
                claudeApiKey = claudeApiKey.trim().ifBlank { null },
                notificationsEnabled = notificationsEnabled,
                notificationHour = notificationHour,
                notificationMinute = notificationMinute,
                localeCode = localeCode
            )
            settingsRepository.savePreferences(updated)

            if (activePlan != null) {
                if (notificationsEnabled) {
                    notificationService.scheduleForPlan(activePlan, notificationHour, notificationMinute)
                } else {
                    notificationService.cancelAll(activePlan.weeks.size)
                }
            }

            _navigationEvent.emit(AppDestination.Home)
        }
    }

    fun saveGoalTime(goalTimeSeconds: Int) {
        viewModelScope.launch {
            val current = settingsRepository.observePreferences().first()
            settingsRepository.savePreferences(current.copy(goalTimeSeconds = goalTimeSeconds))
        }
    }
}
