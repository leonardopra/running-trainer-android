package com.leopra.runningtrainer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leopra.runningtrainer.data.repository.SettingsRepository
import com.leopra.runningtrainer.data.repository.TrainingPlanRepository
import com.leopra.runningtrainer.domain.model.CoachingInsight
import com.leopra.runningtrainer.domain.model.PaceZone
import com.leopra.runningtrainer.domain.model.ProgressStats
import com.leopra.runningtrainer.domain.model.TrainingPlan
import com.leopra.runningtrainer.domain.model.UserPreferencesDto
import com.leopra.runningtrainer.domain.model.Workout
import com.leopra.runningtrainer.domain.model.WorkoutType
import com.leopra.runningtrainer.domain.service.ClaudeService
import com.leopra.runningtrainer.domain.service.InsightsService
import com.leopra.runningtrainer.domain.service.PaceCalculatorService
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

data class PlanUiState(
    val activePlan: TrainingPlan? = null,
    val selectedWorkout: Workout? = null,
    val progressStats: ProgressStats? = null,
    val insights: List<CoachingInsight> = emptyList(),
    val selectedWorkoutPaceZones: List<PaceZone> = emptyList(),
    val isEnrichingPlan: Boolean = false,
    val enrichmentError: String? = null
)

/**
 * Owns plan-related derived state: selected workout, insights, pace zones, enrichment.
 * Observes repositories directly — no dependency on MainViewModel.
 * Navigation intent is published via [navigationEvent]; MainActivity bridges it to MainViewModel.
 */
@HiltViewModel
class PlanViewModel @Inject constructor(
    private val trainingPlanRepository: TrainingPlanRepository,
    private val settingsRepository: SettingsRepository,
    private val insightsService: InsightsService,
    private val paceCalculatorService: PaceCalculatorService,
    private val claudeService: ClaudeService
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<AppDestination>(extraBufferCapacity = 1)
    val navigationEvent: SharedFlow<AppDestination> = _navigationEvent.asSharedFlow()

    private val selectedWorkoutId = MutableStateFlow<String?>(null)
    private val isEnriching = MutableStateFlow(false)
    private val enrichmentError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PlanUiState> = combine(
        trainingPlanRepository.observeActivePlan(),
        trainingPlanRepository.observeProgressStats(),
        settingsRepository.observePreferences(),
        selectedWorkoutId,
        combine(isEnriching, enrichmentError) { e, err -> e to err }
    ) { activePlan, progressStats, preferences, workoutId, (enriching, enrichError) ->
        val selectedWorkout = activePlan?.weeks?.flatMap { it.workouts }
            ?.firstOrNull { it.id == workoutId }
        val insights = if (activePlan != null) {
            insightsService.generate(activePlan, LocalDate.now()).take(5)
        } else emptyList()
        val paceZones = if (selectedWorkout != null &&
            selectedWorkout.type != WorkoutType.rest &&
            selectedWorkout.type != WorkoutType.crossTrain &&
            preferences.goalTimeSeconds != null
        ) {
            paceCalculatorService.calculate(activePlan!!.goalType, preferences.goalTimeSeconds)
                .filter { it.type == selectedWorkout.type }
        } else emptyList()
        PlanUiState(
            activePlan = activePlan,
            selectedWorkout = selectedWorkout,
            progressStats = progressStats,
            insights = insights,
            selectedWorkoutPaceZones = paceZones,
            isEnrichingPlan = enriching,
            enrichmentError = enrichError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlanUiState()
    )

    init {
        // When the plan is cleared, reset any selected workout.
        // When a new unenriched plan arrives (e.g. after generatePlan()), auto-start enrichment.
        viewModelScope.launch {
            trainingPlanRepository.observeActivePlan().collect { plan ->
                if (plan == null) {
                    selectedWorkoutId.value = null
                } else if (!plan.isClaudeEnriched && !isEnriching.value) {
                    val prefs = settingsRepository.observePreferences().first()
                    val apiKey = prefs.claudeApiKey
                    if (!apiKey.isNullOrBlank()) runEnrichment(apiKey, prefs)
                }
            }
        }
    }

    fun openWorkoutDetail(workoutId: String) {
        selectedWorkoutId.value = workoutId
        viewModelScope.launch { _navigationEvent.emit(AppDestination.WorkoutDetail) }
    }

    fun openProgress() {
        viewModelScope.launch { _navigationEvent.emit(AppDestination.Progress) }
    }

    fun openRunHistory() {
        viewModelScope.launch { _navigationEvent.emit(AppDestination.RunHistory) }
    }

    private suspend fun runEnrichment(apiKey: String, prefs: UserPreferencesDto) {
        val plan = trainingPlanRepository.observeActivePlan().firstOrNull() ?: return
        if (plan.isClaudeEnriched) return
        isEnriching.value = true
        enrichmentError.value = null
        try {
            val result = claudeService.enrichPlan(plan, apiKey, prefs)
            if (result.isAuthError) {
                enrichmentError.value = "Invalid API key. Check Settings."
            } else {
                trainingPlanRepository.updatePlan(
                    plan.copy(weeks = result.enrichedWeeks, isClaudeEnriched = true)
                )
            }
        } finally {
            isEnriching.value = false
        }
    }
}
