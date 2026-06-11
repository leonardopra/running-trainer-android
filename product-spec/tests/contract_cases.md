# Contract Cases

These cases define the first parity suite for native clients.

## Rule engine parity
- 5K beginner age 35 must match `fixtures/plan_generation_5k_beginner_age_35.json`
- Marathon advanced age 52 must match `fixtures/plan_generation_marathon_advanced_age_52.json`
- Every week must contain exactly 7 workouts
- Rest days must fill unused weekday slots
- Taper weeks must be the final 3 weeks for race goals

## Workout logging parity
- Logged distance uses actual distance when provided
- Logged distance falls back to planned distance when actual distance is missing
- Completed workouts must preserve notes, RPE, feeling, and completion timestamp

## AI enrichment parity
- Base plan must be persisted before enrichment
- Missing API key must skip enrichment
- 401 must surface auth failure
- 429 must retry with backoff
- other failures must keep local plan usable

## Storage parity
- plan storage is local and readable offline
- preferences storage is local and secure where supported

## Localization parity
- native clients must preserve product meaning even if copy systems differ from Flutter ARB files
