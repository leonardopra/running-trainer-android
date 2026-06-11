# Golden Reference

This document freezes the current Flutter behavior that native clients must match unless intentionally versioned.

## Routing and Onboarding
- Initial route `/` redirects to `/home` when `hasCompletedOnboarding == true`.
- Initial route `/` redirects to `/onboarding/goal` when `hasCompletedOnboarding == false`.
- Onboarding sequence:
  - `/onboarding/goal`
  - `/onboarding/race-date`
  - `/onboarding/fitness`
  - `/onboarding/days`
  - `/onboarding/profile`
  - `/onboarding/generating`
- `hasCompletedOnboarding` is set only after a plan is successfully saved.
- Main routes after onboarding:
  - `/home`
  - `/plan`
  - `/plan/workout/:id`
  - `/progress`
  - `/pace`
  - `/stretching`
  - `/settings`
  - `/privacy`

## Plan Generation
- Rule engine is deterministic and local-first.
- Base weekly mileage:
  - beginner: `20.0 km`
  - intermediate: `35.0 km`
  - advanced: `55.0 km`
- Under age 50:
  - progression: `+9%`
  - recovery every 4th week
- Age 50 and above:
  - progression: `+7%`
  - recovery every 3rd week
- Recovery week target: `-20%` from current volume.
- Race goals apply a 3-week taper using `70% / 50% / 30%` of peak weekly volume.
- Each week always contains exactly 7 `Workout` entries.
- Supported training days per week: `3..6`.
- Day assignment:
  - 3 days: `1, 3, 7`
  - 4 days: `1, 3, 5, 7`
  - 5 days: `1, 2, 4, 5, 7`
  - 6 days: `1, 2, 3, 5, 6, 7`

## AI Enrichment
- Base plan is saved before any remote enrichment call.
- Enrichment runs one request per week, not per workout.
- If API key is missing, plan remains usable and no enrichment is attempted.
- `401` is treated as auth error.
- `429` retries with backoff up to 3 attempts.
- Other failures are skipped silently and the plan remains usable.
- Enrichment updates:
  - `Workout.description`
  - `Workout.coachingTip`

## Workout Logging
- A workout can be marked complete with:
  - actual distance
  - actual duration
  - notes
  - RPE
  - feeling
  - completion timestamp
  - post-workout coaching
- Logged distance falls back to planned distance when actual distance is missing.

## Progress and Insights
- Progress uses only the active plan.
- Streak counts consecutive completed scheduled run days and ignores rest days.
- Insights are priority-ordered and generated from plan state plus recent workout behavior.

## Storage
- Training plans are stored locally and unencrypted.
- User preferences are stored locally and encrypted when the platform supports secure key storage.
- Offline usage is a hard requirement for plan generation, plan browsing, and workout logging.
