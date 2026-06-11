package com.leopra.runningtrainer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.leopra.runningtrainer.R
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leopra.runningtrainer.ui.navigation.AppDestination
import com.leopra.runningtrainer.ui.screens.FitnessSelectionScreen
import com.leopra.runningtrainer.ui.screens.GeneratingPlanScreen
import com.leopra.runningtrainer.ui.screens.GoalSelectionScreen
import com.leopra.runningtrainer.ui.screens.HomeScreen
import com.leopra.runningtrainer.ui.screens.HomeScreenSkeleton
import com.leopra.runningtrainer.ui.screens.PaceCalculatorScreen
import com.leopra.runningtrainer.ui.screens.ProgressScreen
import com.leopra.runningtrainer.ui.screens.ProfileScreen
import com.leopra.runningtrainer.ui.screens.RaceConfigScreen
import com.leopra.runningtrainer.ui.screens.PrivacyScreen
import com.leopra.runningtrainer.ui.screens.RunHistoryScreen
import com.leopra.runningtrainer.ui.screens.SettingsScreen
import com.leopra.runningtrainer.ui.screens.StretchingScreen
import com.leopra.runningtrainer.ui.screens.TrainingDaysScreen
import com.leopra.runningtrainer.ui.screens.WorkoutDetailScreen
import com.leopra.runningtrainer.ui.theme.SurfaceVar

private val mainNavDestinations = setOf(
    AppDestination.Home,
    AppDestination.Progress,
    AppDestination.PaceCalc,
    AppDestination.Settings
)

private val onboardingDestinations = setOf(
    AppDestination.Goal,
    AppDestination.RaceConfig,
    AppDestination.Fitness,
    AppDestination.Days,
    AppDestination.Profile,
    AppDestination.Generating
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunningTrainerApp(
    viewModel: MainViewModel,
    onboardingViewModel: OnboardingViewModel,
    planViewModel: PlanViewModel,
    settingsViewModel: SettingsViewModel,
    workoutLogViewModel: WorkoutLogViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val planUiState by planViewModel.uiState.collectAsStateWithLifecycle()
    val workoutLogUiState by workoutLogViewModel.uiState.collectAsStateWithLifecycle()
    val dest = uiState.currentDestination
    val isOnboarding = dest in onboardingDestinations
    val isMainNav = dest in mainNavDestinations

    // There is no Jetpack Navigation back stack: system back maps each destination
    // to its logical parent. Home, Goal and Generating fall through and exit the app.
    val backTarget = when (dest) {
        AppDestination.WorkoutDetail,
        AppDestination.Stretching,
        AppDestination.Progress,
        AppDestination.PaceCalc,
        AppDestination.Settings -> AppDestination.Home
        AppDestination.RunHistory -> AppDestination.Progress
        AppDestination.Privacy -> AppDestination.Settings
        AppDestination.RaceConfig -> AppDestination.Goal
        AppDestination.Fitness -> AppDestination.RaceConfig
        AppDestination.Days -> AppDestination.Fitness
        AppDestination.Profile -> AppDestination.Days
        AppDestination.Home,
        AppDestination.Goal,
        AppDestination.Generating -> null
    }
    BackHandler(enabled = backTarget != null) {
        backTarget?.let(viewModel::navigateTo)
    }

    Scaffold(
        topBar = {
            if (!isOnboarding) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (dest) {
                                AppDestination.Home          -> stringResource(R.string.app_name)
                                AppDestination.Progress      -> stringResource(R.string.nav_progress)
                                AppDestination.PaceCalc      -> stringResource(R.string.nav_pace)
                                AppDestination.Settings      -> stringResource(R.string.nav_settings)
                                AppDestination.WorkoutDetail -> stringResource(R.string.nav_workout)
                                AppDestination.RunHistory    -> stringResource(R.string.nav_run_history)
                                AppDestination.Stretching    -> stringResource(
                                    if (uiState.isPreRunStretching) R.string.stretch_pre_run_title
                                    else R.string.stretch_post_run_title
                                )
                                AppDestination.Privacy       -> stringResource(R.string.privacy_title)
                                else -> ""
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        if (!isMainNav) {
                            IconButton(onClick = {
                                when (dest) {
                                    AppDestination.WorkoutDetail -> viewModel.goHome()
                                    AppDestination.RunHistory    -> viewModel.navigateTo(AppDestination.Progress)
                                    AppDestination.Stretching    -> viewModel.goHome()
                                    AppDestination.Privacy       -> viewModel.openSettings()
                                    else -> {}
                                }
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.cd_back)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        bottomBar = {
            if (isMainNav) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = androidx.compose.ui.unit.Dp.Unspecified
                ) {
                    NavigationBarItem(
                        selected = dest == AppDestination.Home,
                        onClick = viewModel::goHome,
                        icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.cd_home)) },
                        label = { Text(stringResource(R.string.nav_home)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = MaterialTheme.colorScheme.primary,
                            selectedTextColor   = MaterialTheme.colorScheme.primary,
                            indicatorColor      = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = dest == AppDestination.Progress,
                        onClick = { viewModel.navigateTo(AppDestination.Progress) },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.nav_progress)) },
                        label = { Text(stringResource(R.string.nav_progress)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = MaterialTheme.colorScheme.primary,
                            selectedTextColor   = MaterialTheme.colorScheme.primary,
                            indicatorColor      = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = dest == AppDestination.PaceCalc,
                        onClick = viewModel::openPaceCalc,
                        icon = { Icon(Icons.Default.Speed, contentDescription = stringResource(R.string.nav_pace)) },
                        label = { Text(stringResource(R.string.nav_pace)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = MaterialTheme.colorScheme.primary,
                            selectedTextColor   = MaterialTheme.colorScheme.primary,
                            indicatorColor      = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = dest == AppDestination.Settings,
                        onClick = viewModel::openSettings,
                        icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings)) },
                        label = { Text(stringResource(R.string.nav_settings)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = MaterialTheme.colorScheme.primary,
                            selectedTextColor   = MaterialTheme.colorScheme.primary,
                            indicatorColor      = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        if (uiState.isBootstrapping) {
            HomeScreenSkeleton(innerPadding = innerPadding)
            return@Scaffold
        }

        when (dest) {
            AppDestination.Goal -> GoalSelectionScreen(
                innerPadding = innerPadding,
                onGoalSelected = onboardingViewModel::selectGoal
            )
            AppDestination.RaceConfig -> RaceConfigScreen(
                innerPadding = innerPadding,
                uiState = uiState,
                onConfigChanged = onboardingViewModel::updateRaceConfig,
                onContinue = onboardingViewModel::continueFromRaceConfig
            )
            AppDestination.Fitness -> FitnessSelectionScreen(
                innerPadding = innerPadding,
                onFitnessSelected = onboardingViewModel::selectFitnessLevel
            )
            AppDestination.Days -> TrainingDaysScreen(
                innerPadding = innerPadding,
                selectedDays = uiState.onboarding.trainingDaysPerWeek,
                onDaysChanged = onboardingViewModel::updateTrainingDays,
                onContinue = onboardingViewModel::continueFromDays
            )
            AppDestination.Profile -> ProfileScreen(
                innerPadding = innerPadding,
                uiState = uiState,
                onProfileChanged = onboardingViewModel::updateProfile,
                onGeneratePlan = onboardingViewModel::generatePlan
            )
            AppDestination.Generating -> GeneratingPlanScreen(innerPadding)
            AppDestination.Home -> HomeScreen(
                innerPadding = innerPadding,
                activePlan = planUiState.activePlan,
                runnerName = uiState.preferences.name,
                insights = planUiState.insights,
                onStartSetup = onboardingViewModel::startNewPlan,
                onOpenWorkout = planViewModel::openWorkoutDetail,
                onOpenProgress = planViewModel::openProgress,
                onOpenSettings = viewModel::openSettings
            )
            AppDestination.WorkoutDetail -> WorkoutDetailScreen(
                innerPadding = innerPadding,
                workout = planUiState.selectedWorkout,
                paceZones = planUiState.selectedWorkoutPaceZones,
                workoutLogUiState = workoutLogUiState,
                onSave = workoutLogViewModel::saveWorkoutLog,
                onClear = workoutLogViewModel::clearWorkoutLog,
                onCoachingDismissed = workoutLogViewModel::onCoachingDismissed,
                onOpenStretching = viewModel::openStretching,
                onBack = viewModel::goHome
            )
            AppDestination.Progress -> ProgressScreen(
                innerPadding = innerPadding,
                progressStats = planUiState.progressStats,
                onBack = viewModel::goHome,
                onViewAllHistory = planViewModel::openRunHistory
            )
            AppDestination.RunHistory -> RunHistoryScreen(
                innerPadding = innerPadding,
                activePlan = planUiState.activePlan,
                onBack = { viewModel.navigateTo(AppDestination.Progress) }
            )
            AppDestination.PaceCalc -> PaceCalculatorScreen(
                innerPadding = innerPadding,
                activePlan = planUiState.activePlan,
                savedGoalTimeSeconds = uiState.preferences.goalTimeSeconds,
                onSaveGoalTime = settingsViewModel::saveGoalTime
            )
            AppDestination.Settings -> SettingsScreen(
                innerPadding = innerPadding,
                preferences = uiState.preferences,
                onSave = { name, age, weightKg, heightCm, useKm, apiKey, notifEnabled, notifHour, notifMinute, localeCode ->
                    settingsViewModel.saveSettings(
                        name, age, weightKg, heightCm, useKm, apiKey,
                        notifEnabled, notifHour, notifMinute, localeCode,
                        activePlan = planUiState.activePlan
                    )
                },
                onStartNewPlan = onboardingViewModel::startNewPlan,
                onResetAll = onboardingViewModel::resetLocalData,
                onOpenPrivacy = viewModel::openPrivacy,
                onBack = viewModel::goHome
            )
            AppDestination.Stretching -> StretchingScreen(
                innerPadding = innerPadding,
                isPreRun = uiState.isPreRunStretching,
                onBack = viewModel::goHome
            )
            AppDestination.Privacy -> PrivacyScreen(
                innerPadding = innerPadding,
                onBack = viewModel::openSettings
            )
        }
    }
}
