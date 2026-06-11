# Android App

Native Android implementation of Running Trainer, built with Kotlin + Jetpack Compose. This is the product and the sole supported platform; the former Flutter multiplatform app (macOS/Android/Web) is frozen — see ADR 0001.

## Stack

- Kotlin 2.0.21, Java 17, AGP 8.5.2
- Jetpack Compose (BOM 2024.09.03) + Material3
- Hilt 2.52 (DI) + KSP 2.0.21-1.0.27
- Room 2.6.1 (plan storage as JSON blob) — Room compiler via KSP
- DataStore Preferences (user settings)
- kotlinx-serialization-json 1.7.3
- kotlinx-datetime 0.6.1
- AppCompat 1.7.0 (locale switching)
- navigation-compose 2.8.0 (on the classpath; routing is ViewModel-driven, NavHost not used)
- Tests: JUnit 4, MockK 1.13.10, kotlinx-coroutines-test, Compose UI test
- Min SDK 26, target/compile SDK 35
- App ID / namespace `com.leopra.runningtrainer`, version `0.1.0` (versionCode 1)

## Status

Core feature set implemented (rule-based plan generation, Claude enrichment + streaming
post-workout coaching, insights engine, VDOT pace calculator, onboarding, scheduled
notifications, EN/IT/DE localization, stretching, privacy). Android-only — Web/macOS/iOS
are paused (see `docs/adr/0001-single-native-android-app.md`).

Not yet production-ready:
- Rule-engine parity with `product-spec/fixtures` still being broadened (RUN-16).
- ~~Claude API key is stored in plaintext in DataStore — no encryption (RUN-48)~~ Fixed: key is encrypted via Android Keystore (AES-256-GCM) and the DataStore dir is excluded from backups.
- No R8/minification or signing config; current build is debug `0.1.0`.

## Screens

| Screen | Route | Notes |
|---|---|---|
| Onboarding | Goal → RaceConfig → Fitness → Days → Profile → Generating | Race-date or duration input |
| Home | `Home` | Greeting, insight strip, full multi-week plan with current week highlighted |
| Workout Detail | `WorkoutDetail` | Pace zones, AI coach note, coaching tip, log form, post-workout coaching |
| Progress | `Progress` | Stat grid, weekly bars, feeling/type breakdown, recent activity |
| Run History | `RunHistory` | Full list of completed workouts |
| Pace Calculator | `PaceCalc` | Goal distance selector, HH:MM:SS input, expandable pace zone cards, auto-saves goal time |
| Settings | `Settings` | Profile, AI key, language (EN/IT/DE), units, notifications, new plan, reset |
| Stretching | `Stretching` | Pre/post run, expandable exercise list, YouTube links |
| Privacy | `Privacy` | Data storage, AI, notifications, deletion policy |

## Running

```bash
./gradlew assembleDebug                                          # Build debug APK
./gradlew test                                                   # Run all JVM unit tests
./gradlew :app:testDebugUnitTest --tests "*.PlanGeneratorFixtureTest"
./gradlew connectedAndroidTest                                   # Instrumented tests (device/emulator required)
./gradlew lint
```

## Architecture

See `CLAUDE.md` for full architecture notes, key constraints, and gotchas.
