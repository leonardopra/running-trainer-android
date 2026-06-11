# ADR 0001 — Single native Android app; pause iOS, Web, and macOS

- **Status:** Accepted
- **Date:** 2026-06-09
- **Supersedes:** the implicit "Flutter multiplatform" product scope

## Context

Running Trainer began as a Flutter multiplatform app targeting **macOS, Android, and Web (Chrome)**, with **iOS** planned (RUN-14). A **native Android pilot** (Kotlin + Jetpack Compose, Hilt, Room) was built to evaluate migrating off Flutter. The pilot now implements the core feature set — rule-based plan generation, Claude plan enrichment and streaming post-workout coaching, the insights engine, the VDOT pace calculator, the multi-step onboarding, scheduled notifications, localization (EN/IT/DE), stretching, and privacy — but covers **Android only**.

## Decision

For the current phase, the product is a **single native Android app**. We **pause** the Web and macOS targets and **do not start** iOS. This is a deferral, not a permanent cancellation.

Stated plainly: with Android served natively and macOS/Web dropped, the Flutter codebase no longer ships to any platform. It is therefore **frozen** — kept for reference, not actively developed — rather than deleted.

## Consequences

**Positive**
- One codebase and toolchain to maintain; no Flutter/Dart upkeep.
- Native performance and platform-idiomatic UX on Android.
- Faster iteration focused on a single target.

**Negative / risks**
- Loss of **macOS** reach — a working, already-shipped platform, not just a planned one — and of **Web** reach.
- **No code reuse for a future iOS app**: iOS would require either a new native app or reviving Flutter.
- The native app becomes the **sole product**, so its gaps are no longer backstopped by Flutter. The following shift from "nice-to-have" to **gating for production**:
  - **Parity:** confirm the rule engine matches `product-spec/fixtures` (increase coverage — RUN-16).
  - **Security:** the Claude API key is stored in plaintext in DataStore — a regression from Flutter's AES-256 encrypted storage. Move it to encrypted storage (RUN-48).
  - **Release hardening:** enable R8/minification, a signing config, and proper versioning; the app is currently `0.1.0` / debug.

## Guardrails

- **Preserve `product-spec/fixtures`.** They remain the parity contract for the Android rule engine even while Flutter is frozen.
- **Archive the Flutter repo (read-only) rather than deleting it**, to keep a future revival cheap.

## Revisit when

- iOS becomes a priority, or
- Web/desktop reach is needed again, or
- The cross-platform strategy is otherwise reconsidered.
