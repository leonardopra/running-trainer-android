# Graph Report - .  (2026-06-11)

## Corpus Check
- 58 files · ~40,853 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 372 nodes · 314 edges · 62 communities detected
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 7 edges (avg confidence: 0.78)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 19|Community 19]]
- [[_COMMUNITY_Community 20|Community 20]]
- [[_COMMUNITY_Community 21|Community 21]]
- [[_COMMUNITY_Community 22|Community 22]]
- [[_COMMUNITY_Community 23|Community 23]]
- [[_COMMUNITY_Community 24|Community 24]]
- [[_COMMUNITY_Community 25|Community 25]]
- [[_COMMUNITY_Community 26|Community 26]]
- [[_COMMUNITY_Community 27|Community 27]]
- [[_COMMUNITY_Community 28|Community 28]]
- [[_COMMUNITY_Community 29|Community 29]]
- [[_COMMUNITY_Community 30|Community 30]]
- [[_COMMUNITY_Community 31|Community 31]]
- [[_COMMUNITY_Community 32|Community 32]]
- [[_COMMUNITY_Community 33|Community 33]]
- [[_COMMUNITY_Community 34|Community 34]]
- [[_COMMUNITY_Community 35|Community 35]]
- [[_COMMUNITY_Community 36|Community 36]]
- [[_COMMUNITY_Community 37|Community 37]]
- [[_COMMUNITY_Community 38|Community 38]]
- [[_COMMUNITY_Community 39|Community 39]]
- [[_COMMUNITY_Community 40|Community 40]]
- [[_COMMUNITY_Community 41|Community 41]]
- [[_COMMUNITY_Community 42|Community 42]]
- [[_COMMUNITY_Community 43|Community 43]]
- [[_COMMUNITY_Community 44|Community 44]]
- [[_COMMUNITY_Community 45|Community 45]]
- [[_COMMUNITY_Community 46|Community 46]]
- [[_COMMUNITY_Community 47|Community 47]]
- [[_COMMUNITY_Community 48|Community 48]]
- [[_COMMUNITY_Community 49|Community 49]]
- [[_COMMUNITY_Community 50|Community 50]]
- [[_COMMUNITY_Community 51|Community 51]]
- [[_COMMUNITY_Community 52|Community 52]]
- [[_COMMUNITY_Community 53|Community 53]]
- [[_COMMUNITY_Community 54|Community 54]]
- [[_COMMUNITY_Community 55|Community 55]]
- [[_COMMUNITY_Community 56|Community 56]]
- [[_COMMUNITY_Community 57|Community 57]]
- [[_COMMUNITY_Community 58|Community 58]]
- [[_COMMUNITY_Community 59|Community 59]]
- [[_COMMUNITY_Community 60|Community 60]]
- [[_COMMUNITY_Community 61|Community 61]]

## God Nodes (most connected - your core abstractions)
1. `PlanGenerator` - 15 edges
2. `WorkoutDetailScreenTest` - 13 edges
3. `LocalTrainingPlanRepository` - 12 edges
4. `OnboardingViewModel` - 11 edges
5. `AppModule` - 11 edges
6. `OnboardingScreensTest` - 10 edges
7. `ClaudePromptBuilderTest` - 10 edges
8. `TrainingPlanRepository` - 10 edges
9. `MainViewModel` - 9 edges
10. `ClaudeResponseParserTest` - 7 edges

## Surprising Connections (you probably didn't know these)
- `MainViewModel` --rationale_for--> `Rationale: Room stores plan as JSON blob (not normalized rows)`  [INFERRED]
  app/src/main/java/com/leopra/runningtrainer/ui/MainViewModel.kt → CLAUDE.md
- `PlanViewModel` --governs_design_constraint_on--> `Rationale: native Android pilot — rule-engine behavior must match product-spec fixtures`  [INFERRED]
  app/src/main/java/com/leopra/runningtrainer/ui/PlanViewModel.kt → CLAUDE.md
- `PlanGenerator` --semantically_similar_to--> `InsightsService`  [INFERRED] [semantically similar]
  app/src/main/java/com/leopra/runningtrainer/domain/service/PlanGenerator.kt → app/src/main/java/com/leopra/runningtrainer/domain/service/InsightsService.kt
- `PrivacyScreen()` --conceptually_related_to--> `LocalSettingsStore`  [INFERRED]
  app/src/main/java/com/leopra/runningtrainer/ui/screens/PrivacyScreen.kt → app/src/main/java/com/leopra/runningtrainer/data/local/LocalSettingsStore.kt
- `TrainingPlanRepository` --shares_data_with--> `SerializableTrainingPlan`  [INFERRED]
  app/src/main/java/com/leopra/runningtrainer/data/repository/TrainingPlanRepository.kt → app/src/main/java/com/leopra/runningtrainer/data/serialization/SerializableModels.kt

## Hyperedges (group relationships)
- **Fixture Contract Testing: PlanGeneratorFixtureTest + product-spec fixtures + PlanGenerator** — plangeneratorfixture_plangeneratorfixture, product_spec_fixtures, domain_plangenerator, domain_plangenerationrequest [EXTRACTED 0.95]
- **Room JSON Blob Serialization Pipeline** — trainingplandao_trainingplandao, localrepositories_localtrainingplanrepository, serializablemodels_serializabletrainingplan, serializablemodels_toserializable [EXTRACTED 0.97]
- **Core Domain Model (TrainingPlan hierarchy)** — trainingmodels_trainingplan, trainingmodels_trainingweek, trainingmodels_workout, enums_workouttype [EXTRACTED 1.00]
- **Sub-ViewModel Navigation Bridge Pattern** — mainactivity_mainactivity, onboardingviewmodel_onboardingviewmodel, planviewmodel_planviewmodel, settingsviewmodel_settingsviewmodel, workoutlogviewmodel_workoutlogviewmodel, mainviewmodel_mainviewmodel [EXTRACTED 1.00]
- **Streaming Post-Workout Coaching Flow** — workoutlogviewmodel_workoutlogviewmodel, workoutlogviewmodel_workoutloguistate, workoutlogviewmodel_streamingjob, screens_workoutdetailscreen [EXTRACTED 1.00]
- **Shared Composable Utilities exported from HomeScreen** — screens_homescreen, screens_surfacecard, screens_workouttypelabel, screens_workouttypecolor, screens_zonedescription, screens_workoutdetailscreen, screens_pacecalculatorscreen, screens_progressscreen [EXTRACTED 1.00]

## Communities

### Community 0 - "Community 0"
Cohesion: 0.12
Nodes (2): LocalSettingsRepository, LocalTrainingPlanRepository

### Community 1 - "Community 1"
Cohesion: 0.12
Nodes (0): 

### Community 2 - "Community 2"
Cohesion: 0.12
Nodes (14): CoachingInsight, InsightKind, InsightType, PaceDataPoint, PaceZone, ProgressStats, RpeDataPoint, TrainingPlan (+6 more)

### Community 3 - "Community 3"
Cohesion: 0.13
Nodes (4): SerializableTrainingPlan, SerializableTrainingWeek, SerializableWorkout, TrainingPlanRepository

### Community 4 - "Community 4"
Cohesion: 0.13
Nodes (1): PlanGenerator

### Community 5 - "Community 5"
Cohesion: 0.14
Nodes (1): WorkoutDetailScreenTest

### Community 6 - "Community 6"
Cohesion: 0.14
Nodes (3): OnboardingFormState, OnboardingUiState, OnboardingViewModel

### Community 7 - "Community 7"
Cohesion: 0.14
Nodes (0): 

### Community 8 - "Community 8"
Cohesion: 0.14
Nodes (4): Keys, LocalSettingsStore, PrivacyScreen(), SettingsRepository

### Community 9 - "Community 9"
Cohesion: 0.17
Nodes (1): AppModule

### Community 10 - "Community 10"
Cohesion: 0.18
Nodes (1): OnboardingScreensTest

### Community 11 - "Community 11"
Cohesion: 0.18
Nodes (1): ClaudePromptBuilderTest

### Community 12 - "Community 12"
Cohesion: 0.18
Nodes (5): FixedIdProvider, FixtureExpected, FixtureRequest, PlanFixture, PlanGeneratorFixtureTest

### Community 13 - "Community 13"
Cohesion: 0.18
Nodes (3): MainUiState, MainViewModel, Rationale: Room stores plan as JSON blob (not normalized rows)

### Community 14 - "Community 14"
Cohesion: 0.22
Nodes (2): InsightsService, ProgressStatsCalculator

### Community 15 - "Community 15"
Cohesion: 0.25
Nodes (1): ClaudeResponseParserTest

### Community 16 - "Community 16"
Cohesion: 0.25
Nodes (0): 

### Community 17 - "Community 17"
Cohesion: 0.25
Nodes (3): PlanUiState, PlanViewModel, Rationale: native Android pilot — rule-engine behavior must match product-spec fixtures

### Community 18 - "Community 18"
Cohesion: 0.29
Nodes (1): SSEParserTest

### Community 19 - "Community 19"
Cohesion: 0.29
Nodes (0): 

### Community 20 - "Community 20"
Cohesion: 0.29
Nodes (1): TrainingPlanDao

### Community 21 - "Community 21"
Cohesion: 0.29
Nodes (2): ClaudeHttpClient, ClaudeRequest

### Community 22 - "Community 22"
Cohesion: 0.33
Nodes (0): 

### Community 23 - "Community 23"
Cohesion: 0.33
Nodes (5): EffortLevel, FitnessLevel, GoalType, WorkoutFeeling, WorkoutType

### Community 24 - "Community 24"
Cohesion: 0.33
Nodes (2): PaceCalculatorService, ZoneMult

### Community 25 - "Community 25"
Cohesion: 0.33
Nodes (1): NotificationService

### Community 26 - "Community 26"
Cohesion: 0.4
Nodes (1): PlanGeneratorSmokeTest

### Community 27 - "Community 27"
Cohesion: 0.4
Nodes (2): ProgressStatsCalculatorTest, ProgressTestIdProvider

### Community 28 - "Community 28"
Cohesion: 0.4
Nodes (0): 

### Community 29 - "Community 29"
Cohesion: 0.4
Nodes (1): ApiKeyCipher

### Community 30 - "Community 30"
Cohesion: 0.4
Nodes (1): ClaudePromptBuilder

### Community 31 - "Community 31"
Cohesion: 0.4
Nodes (1): EnrichmentResult

### Community 32 - "Community 32"
Cohesion: 0.5
Nodes (1): SettingsViewModel

### Community 33 - "Community 33"
Cohesion: 0.5
Nodes (0): 

### Community 34 - "Community 34"
Cohesion: 0.5
Nodes (0): 

### Community 35 - "Community 35"
Cohesion: 0.5
Nodes (3): PlanGenerationMetadata, PlanGenerationRequest, PlanGenerationResult

### Community 36 - "Community 36"
Cohesion: 0.5
Nodes (1): ClaudeResponseParser

### Community 37 - "Community 37"
Cohesion: 0.5
Nodes (1): WorkoutAlarmReceiver

### Community 38 - "Community 38"
Cohesion: 0.83
Nodes (4): Launcher Background Layer — solid deep navy / dark blue fill, App Launcher Icon (composite adaptive), Launcher Foreground Layer — running figure silhouette with bar-chart progress bars (navy + coral/red), symbolising training progress, App Launcher Icon Round (composite adaptive)

### Community 39 - "Community 39"
Cohesion: 0.67
Nodes (1): MainActivity

### Community 40 - "Community 40"
Cohesion: 0.67
Nodes (0): 

### Community 41 - "Community 41"
Cohesion: 0.67
Nodes (1): AppDatabase

### Community 42 - "Community 42"
Cohesion: 0.67
Nodes (1): BootCompletedReceiver

### Community 43 - "Community 43"
Cohesion: 1.0
Nodes (0): 

### Community 44 - "Community 44"
Cohesion: 1.0
Nodes (1): WorkoutLogUiState

### Community 45 - "Community 45"
Cohesion: 1.0
Nodes (1): AppDestination

### Community 46 - "Community 46"
Cohesion: 1.0
Nodes (0): 

### Community 47 - "Community 47"
Cohesion: 1.0
Nodes (1): RunningTrainerApplication

### Community 48 - "Community 48"
Cohesion: 1.0
Nodes (1): TrainingPlanEntity

### Community 49 - "Community 49"
Cohesion: 1.0
Nodes (1): StretchExercise

### Community 50 - "Community 50"
Cohesion: 1.0
Nodes (0): 

### Community 51 - "Community 51"
Cohesion: 1.0
Nodes (0): 

### Community 52 - "Community 52"
Cohesion: 1.0
Nodes (0): 

### Community 53 - "Community 53"
Cohesion: 1.0
Nodes (0): 

### Community 54 - "Community 54"
Cohesion: 1.0
Nodes (0): 

### Community 55 - "Community 55"
Cohesion: 1.0
Nodes (0): 

### Community 56 - "Community 56"
Cohesion: 1.0
Nodes (1): product-spec/fixtures JSON files

### Community 57 - "Community 57"
Cohesion: 1.0
Nodes (1): Android App README

### Community 58 - "Community 58"
Cohesion: 1.0
Nodes (1): app/README.md — Package Layout

### Community 59 - "Community 59"
Cohesion: 1.0
Nodes (1): Rationale: MainActivity extends AppCompatActivity for locale switching on Android < 13

### Community 60 - "Community 60"
Cohesion: 1.0
Nodes (1): Rationale: no DI framework, manual AppContainer

### Community 61 - "Community 61"
Cohesion: 1.0
Nodes (1): Rationale: PaceZone.label kept as domain English strings for serialization/testing only

## Knowledge Gaps
- **46 isolated node(s):** `PlanFixture`, `FixtureExpected`, `MainUiState`, `PlanUiState`, `OnboardingFormState` (+41 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Community 43`** (2 nodes): `RunningTrainerApp.kt`, `RunningTrainerApp()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 44`** (2 nodes): `WorkoutLogViewModel.kt`, `WorkoutLogUiState`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 45`** (2 nodes): `AppDestination.kt`, `AppDestination`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 46`** (2 nodes): `Theme.kt`, `RunningTrainerTheme()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 47`** (2 nodes): `RunningTrainerApplication.kt`, `RunningTrainerApplication`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 48`** (2 nodes): `TrainingPlanEntity.kt`, `TrainingPlanEntity`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 49`** (2 nodes): `StretchExercise.kt`, `StretchExercise`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 50`** (1 nodes): `build.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 51`** (1 nodes): `settings.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 52`** (1 nodes): `build.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 53`** (1 nodes): `Color.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 54`** (1 nodes): `Type.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 55`** (1 nodes): `StretchData.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 56`** (1 nodes): `product-spec/fixtures JSON files`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 57`** (1 nodes): `Android App README`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 58`** (1 nodes): `app/README.md — Package Layout`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 59`** (1 nodes): `Rationale: MainActivity extends AppCompatActivity for locale switching on Android < 13`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 60`** (1 nodes): `Rationale: no DI framework, manual AppContainer`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 61`** (1 nodes): `Rationale: PaceZone.label kept as domain English strings for serialization/testing only`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `TrainingPlanRepository` connect `Community 3` to `Community 8`?**
  _High betweenness centrality (0.004) - this node is a cross-community bridge._
- **Why does `PlanGenerator` connect `Community 4` to `Community 14`?**
  _High betweenness centrality (0.003) - this node is a cross-community bridge._
- **What connects `PlanFixture`, `FixtureExpected`, `MainUiState` to the rest of the system?**
  _46 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.12 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.12 - nodes in this community are weakly interconnected._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.12 - nodes in this community are weakly interconnected._
- **Should `Community 3` be split into smaller, more focused modules?**
  _Cohesion score 0.13 - nodes in this community are weakly interconnected._