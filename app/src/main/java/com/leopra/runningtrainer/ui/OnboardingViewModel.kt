package com.leopra.runningtrainer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leopra.runningtrainer.data.repository.SettingsRepository
import com.leopra.runningtrainer.data.repository.TrainingPlanRepository
import com.leopra.runningtrainer.domain.contracts.PlanGenerationRequest
import com.leopra.runningtrainer.domain.model.FitnessLevel
import com.leopra.runningtrainer.domain.model.GoalType
import com.leopra.runningtrainer.notifications.NotificationService
import com.leopra.runningtrainer.ui.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class OnboardingFormState(
    val goalType: GoalType? = null,
    val raceDateInput: String = "",
    val durationWeeks: Int? = null,
    val fitnessLevel: FitnessLevel? = null,
    val trainingDaysPerWeek: Int = 3,
    val name: String = "",
    val age: String = "",
    val weightKg: String = "",
    val heightCm: String = ""
)

data class OnboardingUiState(
    val form: OnboardingFormState = OnboardingFormState(),
    val isGeneratingPlan: Boolean = false,
    val generationError: String? = null
)

/**
 * Owns onboarding form state, plan generation, and data-reset operations.
 * Navigation intent is published via [navigationEvent]; MainActivity bridges it to MainViewModel.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val trainingPlanRepository: TrainingPlanRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationService: NotificationService
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<AppDestination>(extraBufferCapacity = 1)
    val navigationEvent: SharedFlow<AppDestination> = _navigationEvent.asSharedFlow()

    private val form = MutableStateFlow(OnboardingFormState())
    private val isGenerating = MutableStateFlow(false)
    private val generationError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<OnboardingUiState> = combine(
        form, isGenerating, generationError
    ) { f, generating, error ->
        OnboardingUiState(form = f, isGeneratingPlan = generating, generationError = error)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = OnboardingUiState()
    )

    // ── Form updates ──────────────────────────────────────────────────────────

    fun selectGoal(goalType: GoalType) {
        form.value = form.value.copy(goalType = goalType, raceDateInput = "", durationWeeks = null)
        viewModelScope.launch { _navigationEvent.emit(AppDestination.RaceConfig) }
    }

    fun updateRaceConfig(raceDateInput: String, durationWeeks: Int?) {
        form.value = form.value.copy(raceDateInput = raceDateInput, durationWeeks = durationWeeks)
    }

    fun continueFromRaceConfig() {
        viewModelScope.launch { _navigationEvent.emit(AppDestination.Fitness) }
    }

    fun selectFitnessLevel(level: FitnessLevel) {
        form.value = form.value.copy(fitnessLevel = level)
        viewModelScope.launch { _navigationEvent.emit(AppDestination.Days) }
    }

    fun updateTrainingDays(daysPerWeek: Int) {
        form.value = form.value.copy(trainingDaysPerWeek = daysPerWeek.coerceIn(3, 6))
    }

    fun continueFromDays() {
        viewModelScope.launch { _navigationEvent.emit(AppDestination.Profile) }
    }

    fun updateProfile(name: String, age: String, weightKg: String, heightCm: String) {
        form.value = form.value.copy(name = name, age = age, weightKg = weightKg, heightCm = heightCm)
    }

    // ── Plan generation ───────────────────────────────────────────────────────

    fun generatePlan() {
        val f = form.value
        val goal = f.goalType ?: return
        val fitness = f.fitnessLevel ?: return

        isGenerating.value = true
        generationError.value = null

        viewModelScope.launch {
            _navigationEvent.emit(AppDestination.Generating)

            runCatching {
                val currentPrefs = settingsRepository.observePreferences().first()
                settingsRepository.savePreferences(
                    currentPrefs.copy(
                        hasCompletedOnboarding = true,
                        name = f.name.trim().ifBlank { null },
                        age = f.age.toIntOrNull(),
                        weightKg = f.weightKg.toDoubleOrNull(),
                        heightCm = f.heightCm.toDoubleOrNull()
                    )
                )
                trainingPlanRepository.generateAndSavePlan(
                    PlanGenerationRequest(
                        goalType = goal,
                        fitnessLevel = fitness,
                        trainingDaysPerWeek = f.trainingDaysPerWeek,
                        raceDate = f.raceDateInput.takeIf { it.isNotBlank() }
                            ?.let { LocalDate.parse(it) },
                        durationWeeks = f.durationWeeks,
                        age = f.age.toIntOrNull()
                    )
                )
            }.onSuccess {
                val prefs = settingsRepository.observePreferences().first()
                if (prefs.notificationsEnabled) {
                    val plan = trainingPlanRepository.observeActivePlan().firstOrNull()
                    if (plan != null) {
                        notificationService.scheduleForPlan(plan, prefs.notificationHour, prefs.notificationMinute)
                    }
                }
                // PlanViewModel auto-triggers enrichment on observing the new plan.
                _navigationEvent.emit(AppDestination.Home)
            }.onFailure { throwable ->
                generationError.value = throwable.message ?: "Unable to generate plan."
                _navigationEvent.emit(AppDestination.Profile)
            }

            isGenerating.value = false
        }
    }

    // ── Data reset ────────────────────────────────────────────────────────────

    fun resetLocalData() {
        viewModelScope.launch {
            val plan = trainingPlanRepository.observeActivePlan().firstOrNull()
            if (plan != null) notificationService.cancelAll(plan.weeks.size)
            trainingPlanRepository.clearAllPlans()
            settingsRepository.clear()
            form.value = OnboardingFormState()
            generationError.value = null
            isGenerating.value = false
            _navigationEvent.emit(AppDestination.Goal)
        }
    }

    fun startNewPlan() {
        viewModelScope.launch {
            val plan = trainingPlanRepository.observeActivePlan().firstOrNull()
            if (plan != null) notificationService.cancelAll(plan.weeks.size)
            trainingPlanRepository.clearAllPlans()
            val currentPrefs = settingsRepository.observePreferences().first()
            settingsRepository.savePreferences(currentPrefs.copy(hasCompletedOnboarding = false))
            form.value = OnboardingFormState()
            generationError.value = null
            isGenerating.value = false
            _navigationEvent.emit(AppDestination.Goal)
        }
    }
}
