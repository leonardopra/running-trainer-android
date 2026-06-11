# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
./gradlew assembleDebug                                          # Build debug APK
./gradlew test                                                   # Run all JVM unit tests
./gradlew :app:testDebugUnitTest --tests "*.PlanGeneratorSmokeTest" # Run a single test class
./gradlew connectedAndroidTest                                   # Run instrumented tests (requires device/emulator)
./gradlew lint                                                   # Run lint checks
```

> Min SDK 26, target/compile SDK 35, Java 17, Kotlin 2.0.21, Compose BOM 2024.09.03. App ID / namespace `com.leopra.runningtrainer`.

## CI & Release

- `.github/workflows/ci.yml` runs `./gradlew test` and `./gradlew lint` on every push to `main` and on PRs; test/lint reports are uploaded as artifacts on failure.
- `.github/workflows/release.yml` (on `v*` tags or manual dispatch) builds an APK and attaches it to a GitHub Release. With the `KEYSTORE_BASE64`/`KEY_ALIAS`/`KEY_PASSWORD`/`STORE_PASSWORD` secrets set it builds a signed release APK (same keystore as the former monorepo releases, so existing installs upgrade in place); without them — the current, intentional setup — it ships the debug APK, which installs fine but requires uninstall between releases (CI regenerates the debug keystore each run).
- `app/build.gradle.kts` reads root `key.properties` for the release signing config (see `key.properties.example`); absent → unsigned release build. Bump `versionCode`/`versionName` before tagging.

## Architecture

**Single-activity, multiple ViewModels.** App state is split across five `@HiltViewModel` classes, all obtained in `MainActivity` via `by viewModels()`:

- `MainViewModel` — `MainUiState` (bootstrap, `currentDestination`, `isPreRunStretching`, onboarding mirror, plan generation flags, preferences, active plan). Owns navigation.
- `OnboardingViewModel` — onboarding form state + plan generation; schedules notifications after generating a plan.
- `PlanViewModel` — selected workout, progress stats, insights, pace zones, and AI plan enrichment (`isEnrichingPlan`, `enrichmentError`).
- `SettingsViewModel` — saves settings and goal time; re-schedules notifications on save.
- `WorkoutLogViewModel` — streaming post-workout coaching (`streamingCoaching`, `isStreaming`, `coachingAuthError`).

Sub-ViewModels publish navigation intents on a `navigationEvent: Channel<AppDestination>`; `MainActivity` collects them (inside `repeatOnLifecycle`) and forwards to `MainViewModel.navigateTo`, and likewise mirrors `OnboardingViewModel.uiState` into `MainViewModel`.

**Navigation** is ViewModel-driven via `currentDestination: MutableStateFlow<AppDestination>` — there is no Jetpack Navigation back stack. `RunningTrainerApp.kt` renders the matching screen with a `when(dest)` inside a `Scaffold` with a 4-tab bottom nav (Home / Progress / PaceCalc / Settings). System back is handled by a `BackHandler` in `RunningTrainerApp.kt` that maps each destination to its logical parent (tabs → Home, onboarding steps in reverse); Home/Goal/Generating exit the app. `navigation-compose` is on the classpath but is **not** used.

**`MainActivity` extends `AppCompatActivity`** (not `ComponentActivity`) — required so `AppCompatDelegate.setApplicationLocales()` applies the stored locale (`en`/`it`/`de`, see `res/xml/locales_config.xml`) correctly, including on Android < 13. The stored locale is applied in `onCreate`.

**Dependency injection: Hilt is the sole composition root.**
- `RunningTrainerApplication` is annotated `@HiltAndroidApp`; `MainActivity` is `@AndroidEntryPoint` (with an `@Inject` `SettingsRepository`).
- A single module, `app/di/AppModule.kt` (`@InstallIn(SingletonComponent::class)`), provides `AppDatabase`, `Json`, `TrainingPlanRepository`, `SettingsRepository`, `PaceCalculatorService`, `InsightsService`, `ClaudeService`, and `NotificationService`.
- ViewModels are `@HiltViewModel` with `@Inject` constructors. There is no manual factory and no `AppContainer`.

**Package layout (`com.leopra.runningtrainer`):**
- `MainActivity.kt` — at the package root.
- `app/` — `RunningTrainerApplication`, `di/AppModule.kt`.
- `domain/model/` — pure Kotlin data models (`TrainingPlan`, `Workout`, enums, `UserPreferencesDto`, `StretchData`/`StretchExercise`).
- `domain/contracts/` — request/result types (`PlanGenerationRequest`, `PlanGenerationResult`).
- `domain/service/` — `PlanGenerator`, `InsightsService`, `PaceCalculatorService`, `ProgressStatsCalculator`, and the Claude stack (`ClaudeService`, `ClaudeHttpClient`, `ClaudePromptBuilder`, `ClaudeResponseParser`).
- `data/local/` — Room (`AppDatabase`, `TrainingPlanEntity`, `TrainingPlanDao`) + DataStore (`LocalSettingsStore`).
- `data/repository/` — repository interfaces + `LocalTrainingPlanRepository`, `LocalSettingsRepository`.
- `data/serialization/` — `@Serializable` mirror models used for JSON storage in Room (`payloadJson` column).
- `notifications/` — `NotificationService`, `WorkoutAlarmReceiver`, `BootCompletedReceiver`.
- `ui/screens/` — one Composable file per screen. `HomeScreen.kt` exports shared helpers: `workoutTypeColor`, `WorkoutType.typeLabel()`, `WorkoutType.zoneDescription()`, `SurfaceCard`.
- `ui/navigation/` — `AppDestination` enum (Goal, RaceConfig, Fitness, Days, Profile, Generating, Home, WorkoutDetail, Progress, RunHistory, PaceCalc, Settings, Stretching, Privacy).
- `ui/` root — the five ViewModels + `RunningTrainerApp.kt`.

**Persistence:**
- `TrainingPlan` is stored as a single JSON blob (`payloadJson`) in a Room `training_plans` table. Only the active plan is used; `LocalTrainingPlanRepository` queries the most recent row.
- User preferences are stored via DataStore (`preferences_pb`). The Claude API key is encrypted at rest with an AES-256-GCM key in the Android Keystore (`ApiKeyCipher`, RUN-48); a legacy plaintext slot is read as fallback and migrated to the encrypted slot on the next save. The `datastore/` directory is excluded from auto backup and device-to-device transfer (`res/xml/backup_rules.xml`, `res/xml/data_extraction_rules.xml`).

## AI coaching

The Claude integration lives in `domain/service/`, split across `ClaudeService` (orchestration), `ClaudeHttpClient` (transport), `ClaudePromptBuilder`, and `ClaudeResponseParser`:
- `enrichPlan(...)` — enriches plan weeks; on an auth error it stops and returns the remaining weeks un-enriched, other errors skip silently keeping the original week.
- `generatePostWorkoutCoaching(...)` — blocking, returns `String?`.
- `streamPostWorkoutCoaching(...)` — SSE streaming `Flow<String>` (emits an auth-error sentinel on auth failure); used by `WorkoutLogViewModel` for live coaching.
- Transport: `https://api.anthropic.com/v1/messages` via `HttpURLConnection` with the `x-api-key` header. Model constant lives in `ClaudeHttpClient.kt` (`claude-sonnet-4-6` — aligned with the frozen Flutter reference; see RUN-45). Sonnet is chosen over Opus because post-workout coaching runs on every logged workout, where Opus's higher cost/latency isn't justified for a short 2–3 sentence output.

## Notifications

`NotificationService.scheduleForPlan(plan, hour, minute)` cancels existing alarms then schedules an exact `AlarmManager` alarm for each future, non-rest, non-completed workout (alarm id `weekIndex * 7 + dayOfWeek`, matching Flutter). It uses `setExactAndAllowWhileIdle`, falling back to `setAndAllowWhileIdle` when `canScheduleExactAlarms()` is false on Android 12+. `WorkoutAlarmReceiver` posts the reminder on the `workout_reminders` channel. `BootCompletedReceiver` (Hilt `@AndroidEntryPoint`) re-schedules all alarms from the active plan on `BOOT_COMPLETED` and `MY_PACKAGE_REPLACED`, since `AlarmManager` alarms don't survive reboots or app updates. Required permissions: `INTERNET`, `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`. The `POST_NOTIFICATIONS` runtime permission is requested in `SettingsScreen` when the user enables the notifications toggle (Android 13+). Scheduling is invoked from `OnboardingViewModel` (after generation) and `SettingsViewModel` (on save).

## Testing

- **JVM unit tests** (`app/src/test`): `PlanGeneratorSmokeTest`, `PlanGeneratorFixtureTest` (the parity contract against `product-spec/fixtures`), `ProgressStatsCalculatorTest`, `ClaudePromptBuilderTest`, `ClaudeResponseParserTest`, `SSEParserTest`, and `ClaudeServiceTest` (MockK + `kotlinx-coroutines-test`).
- **Instrumented tests** (`app/src/androidTest`): `OnboardingScreensTest`, `WorkoutDetailScreenTest` (Compose UI).

Add fixture files alongside `PlanGeneratorFixtureTest` when adding new plan-generation test cases.

## Key constraints

- This repo is **public** on GitHub — never commit secrets, keystores, or API keys. It is the sole shipping product (Android-only); the former Flutter app is frozen and its monorepo (`leonardopra/Running-trainer`) archived (see docs/adr/0001-single-native-android-app.md). Rule-engine behavior must still remain in parity with product-spec/fixtures — the fixture tests remain the contract.
- **Hilt is the composition root.** Add new dependencies through `app/di/AppModule.kt` (or appropriate Hilt modules); do not reintroduce a manual `AppContainer`.
- Room stores the plan as a JSON blob (not normalized rows) — intentional to keep schema migrations simple during the pilot phase.
- Do not re-add `hive_generator` or add Robolectric — see root-level memory for dependency constraints.
- `PaceZone.label` and `PaceZone.description` are domain-level English strings kept for serialization/testing. Always resolve display labels through `WorkoutType.typeLabel()` / `WorkoutType.zoneDescription()` in the UI layer, never directly.
- Plan progression is age-aware (50+ uses ~7% / 3-week recovery vs <50 ~9% / 4-week) — keep in parity with the fixtures.

## graphify

This project has a graphify knowledge graph at `graphify-out/`.

Rules:
- Before answering architecture or codebase questions, read `graphify-out/GRAPH_REPORT.md` for god nodes and community structure.
- If `graphify-out/wiki/index.md` exists, navigate it instead of reading raw files.
- After modifying code files in this session, run `graphify update .` to keep the graph current (AST-only, no API cost).
