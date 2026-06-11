package com.leopra.runningtrainer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leopra.runningtrainer.data.repository.SettingsRepository
import com.leopra.runningtrainer.data.repository.TrainingPlanRepository
import com.leopra.runningtrainer.domain.model.TrainingPlan
import com.leopra.runningtrainer.domain.model.UserPreferencesDto
import com.leopra.runningtrainer.ui.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MainUiState(
    val isBootstrapping: Boolean = true,
    val currentDestination: AppDestination = AppDestination.Goal,
    val isPreRunStretching: Boolean = true,
    // Onboarding fields kept here so existing composables (RaceConfigScreen, ProfileScreen)
    // can receive MainUiState unchanged. Values are sourced from OnboardingViewModel.uiState.
    val onboarding: OnboardingFormState = OnboardingFormState(),
    val isGeneratingPlan: Boolean = false,
    val generationError: String? = null,
    val preferences: UserPreferencesDto = UserPreferencesDto(),
    val activePlan: TrainingPlan? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val trainingPlanRepository: TrainingPlanRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val currentDestination = MutableStateFlow(AppDestination.Goal)
    private val isPreRunStretching = MutableStateFlow(true)
    private val navState = combine(currentDestination, isPreRunStretching) { dest, pre -> dest to pre }

    // Kept as internal MutableStateFlow; MainActivity bridges OnboardingViewModel.uiState here.
    private val _onboardingState = MutableStateFlow(OnboardingUiState())

    fun updateOnboardingState(state: OnboardingUiState) {
        _onboardingState.value = state
    }

    val uiState: StateFlow<MainUiState> = combine(
        settingsRepository.observePreferences(),
        trainingPlanRepository.observeActivePlan(),
        navState,
        _onboardingState
    ) { preferences, activePlan, (destination, preRunStretching), onboarding ->
        MainUiState(
            isBootstrapping = false,
            currentDestination = if (preferences.hasCompletedOnboarding && activePlan != null &&
                destination == AppDestination.Goal
            ) {
                AppDestination.Home
            } else {
                destination
            },
            isPreRunStretching = preRunStretching,
            onboarding = onboarding.form,
            isGeneratingPlan = onboarding.isGeneratingPlan,
            generationError = onboarding.generationError,
            preferences = preferences,
            activePlan = activePlan
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState()
    )

    // ── Navigation ────────────────────────────────────────────────────────────

    fun goHome() {
        currentDestination.value = AppDestination.Home
    }

    /** Used by MainActivity to forward navigation events from other ViewModels. */
    fun navigateTo(destination: AppDestination) {
        currentDestination.value = destination
    }

    fun openSettings() {
        currentDestination.value = AppDestination.Settings
    }

    fun openPaceCalc() {
        currentDestination.value = AppDestination.PaceCalc
    }

    fun openStretching(isPreRun: Boolean) {
        isPreRunStretching.value = isPreRun
        currentDestination.value = AppDestination.Stretching
    }

    fun openPrivacy() {
        currentDestination.value = AppDestination.Privacy
    }
}
