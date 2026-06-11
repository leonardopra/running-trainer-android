package com.leopra.runningtrainer

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.leopra.runningtrainer.ui.MainViewModel
import com.leopra.runningtrainer.ui.OnboardingViewModel
import com.leopra.runningtrainer.ui.PlanViewModel
import com.leopra.runningtrainer.ui.RunningTrainerApp
import com.leopra.runningtrainer.ui.SettingsViewModel
import com.leopra.runningtrainer.ui.WorkoutLogViewModel
import com.leopra.runningtrainer.ui.theme.RunningTrainerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.leopra.runningtrainer.data.repository.SettingsRepository

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository

    private val viewModel: MainViewModel by viewModels()
    private val onboardingViewModel: OnboardingViewModel by viewModels()
    private val planViewModel: PlanViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val workoutLogViewModel: WorkoutLogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply stored locale if AppCompatDelegate doesn't already have one set.
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            lifecycleScope.launch {
                val prefs = settingsRepository.observePreferences().first()
                if (prefs.localeCode != "en") {
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(prefs.localeCode)
                    )
                }
            }
        }

        // Bridge navigation events and onboarding state from sub-ViewModels → MainViewModel.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { onboardingViewModel.navigationEvent.collect(viewModel::navigateTo) }
                launch { planViewModel.navigationEvent.collect(viewModel::navigateTo) }
                launch { settingsViewModel.navigationEvent.collect(viewModel::navigateTo) }
                launch { workoutLogViewModel.navigationEvent.collect(viewModel::navigateTo) }
                launch { onboardingViewModel.uiState.collect(viewModel::updateOnboardingState) }
            }
        }

        setContent {
            RunningTrainerTheme {
                RunningTrainerApp(viewModel, onboardingViewModel, planViewModel, settingsViewModel, workoutLogViewModel)
            }
        }
    }
}
