# Running Trainer

[![CI](https://github.com/leonardopra/running-trainer-android/actions/workflows/ci.yml/badge.svg)](https://github.com/leonardopra/running-trainer-android/actions/workflows/ci.yml)
[![Latest release](https://img.shields.io/github/v/release/leonardopra/running-trainer-android)](https://github.com/leonardopra/running-trainer-android/releases/latest)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
![Android](https://img.shields.io/badge/Android-8.0%2B%20(API%2026)-3DDC84?logo=android&logoColor=white)

A native Android running coach that generates personalised multi-week training plans with a rule-based engine, then enriches each workout with descriptions, tips, and post-run coaching from Claude AI.

**All data stays on-device** — no account, no backend, no subscription. The optional AI features use your own Anthropic API key, encrypted at rest via the Android Keystore.

## Download

Grab the APK from the [latest release](https://github.com/leonardopra/running-trainer-android/releases/latest).

> Current releases ship a debug-signed APK: it installs normally, but you must uninstall the previous version before installing a new release.

## Features

- **Personalised plans** — rule-based generator tuned by goal (5K → marathon), race date, fitness level, training days, and age (50+ gets gentler progression and shorter recovery cycles)
- **AI coaching (optional)** — Claude enriches each workout with descriptions and tips, and streams live post-workout feedback after you log a run
- **VDOT pace calculator** — pace zones from your goal time, with expandable zone explanations
- **Progress tracking** — weekly volume, feeling/type breakdowns, insights engine, full run history
- **Workout reminders** — exact alarms that survive reboots and app updates
- **Stretching guides** — pre/post-run routines with video links
- **3 languages** — English, Italiano, Deutsch, switchable in-app
- **Private by design** — everything in local storage ([privacy policy](https://leonardopra.github.io/running-trainer-privacy/))

## Screens

| Screen | Notes |
|---|---|
| Onboarding | Goal → race config → fitness → days → profile → plan generation |
| Home | Greeting, insight strip, full multi-week plan with current week highlighted |
| Workout detail | Pace zones, AI coach note, log form, streaming post-workout coaching |
| Progress / Run history | Stat grid, weekly bars, feeling/type breakdown, completed runs |
| Pace calculator | Goal distance + HH:MM:SS input, expandable pace zone cards |
| Settings | Profile, AI key, language, units, notifications, new plan, reset |
| Stretching | Pre/post run, expandable exercise list, YouTube links |
| Privacy | Data storage, AI, notifications, deletion policy |

## Tech stack

Kotlin 2.0.21 · Jetpack Compose (Material 3) · Hilt · Room · DataStore · kotlinx-serialization · min SDK 26, target SDK 35.

Architecture notes, key constraints, and gotchas live in [CLAUDE.md](CLAUDE.md); platform decisions in [docs/adr](docs/adr).

## Build from source

```bash
./gradlew assembleDebug        # Build debug APK
./gradlew test                 # JVM unit tests (incl. plan-generator fixture parity)
./gradlew connectedAndroidTest # Instrumented tests (device/emulator required)
./gradlew lint
```

Requires JDK 17. For AI features, add your Anthropic API key in Settings inside the app.

## Project history

Extracted from the [Running-trainer monorepo](https://github.com/leonardopra/Running-trainer) (now archived) in June 2026; the former Flutter multiplatform app is frozen — see [ADR 0001](docs/adr/0001-single-native-android-app.md). The rule engine stays in behavioural parity with `product-spec/fixtures`, enforced by fixture tests.

## License

[MIT](LICENSE)
