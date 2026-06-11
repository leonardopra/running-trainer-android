# App Module — Package Layout

```
com.leopra.runningtrainer
├── MainActivity.kt             extends AppCompatActivity, @AndroidEntryPoint
├── app/
│   ├── RunningTrainerApplication.kt   @HiltAndroidApp
│   └── di/                     AppModule.kt — Hilt @Provides bindings (sole composition root)
├── data/
│   ├── local/                  AppDatabase (Room), TrainingPlanDao, TrainingPlanEntity, LocalSettingsStore (DataStore)
│   ├── repository/             TrainingPlanRepository, SettingsRepository + local impls
│   └── serialization/          @Serializable mirror models for Room JSON blob
├── domain/
│   ├── contracts/              PlanGenerationRequest, PlanGenerationResult
│   ├── model/                  TrainingPlan, Workout, PaceZone, enums, UserPreferencesDto, StretchExercise/StretchData
│   └── service/                PlanGenerator, InsightsService, PaceCalculatorService, ProgressStatsCalculator,
│                               ClaudeService, ClaudeHttpClient, ClaudePromptBuilder, ClaudeResponseParser
├── notifications/              NotificationService, WorkoutAlarmReceiver
└── ui/
    ├── navigation/             AppDestination enum
    ├── screens/                One file per screen + shared helpers in HomeScreen.kt
    ├── theme/                  Color, Type, Theme
    ├── MainViewModel.kt        navigation + global state (@HiltViewModel)
    ├── OnboardingViewModel.kt  onboarding form + plan generation
    ├── PlanViewModel.kt        selected workout, stats, insights, AI enrichment
    ├── SettingsViewModel.kt    settings + goal-time writes
    ├── WorkoutLogViewModel.kt  workout logging + streaming post-workout coaching
    └── RunningTrainerApp.kt    Scaffold, 4-tab bottom nav, when(dest) screen routing
```

## Dependency injection

Hilt is the sole composition root — there is no manual `AppContainer`. All singletons are provided by
`app/di/AppModule.kt` (`@InstallIn(SingletonComponent::class)`); ViewModels are `@HiltViewModel` with
`@Inject` constructors, obtained in `MainActivity` via `by viewModels()`.

## Shared Composable helpers (HomeScreen.kt)

These are package-level and used across multiple screens:

- `workoutTypeColor(type)` — returns the Color for a WorkoutType
- `WorkoutType.typeLabel()` — localized display name via `workout_type_*` string resources
- `WorkoutType.zoneDescription()` — localized pace zone description via `pace_zone_*_desc` string resources
- `SurfaceCard` — bordered card composable used throughout the app
