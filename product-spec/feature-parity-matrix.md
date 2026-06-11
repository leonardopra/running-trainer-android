# Feature Parity Matrix

| Area | Flutter Reference | Android Day 1 | Web Day 1 | iOS Day 1 | Notes |
|---|---|---|---|---|---|
| Onboarding | Yes | Yes | Yes | Yes | Must preserve redirect semantics |
| Local plan generation | Yes | Yes | Yes | Yes | Core parity requirement |
| Plan overview | Yes | Yes | Yes | Yes | Functional parity, not pixel parity |
| Workout detail | Yes | Yes | Yes | Yes | |
| Workout logging | Yes | Yes | Yes | Yes | |
| Progress dashboard | Yes | Yes | Yes | Yes | |
| Settings | Yes | Yes | Yes | Yes | Secure key handling differs by platform |
| Pace calculator | Yes | Yes | Yes | Yes | |
| Insights | Yes | Yes | Yes | Yes | |
| AI enrichment | Yes | Phase 1.1 | Phase 1.1 | Phase 1.1 | Optional feature, never blocks core flow |
| Notifications | Android only | Yes | Optional | Yes | Web notifications are non-blocking |
| Stretching | Yes | Phase 2 | Phase 2 | Phase 2 | |
| Privacy screen | Yes | Phase 2 | Phase 2 | Phase 2 | |

Delivery rule:
- native clients must ship the Day 1 row set before feature expansion
- Flutter remains the benchmark until all three native clients pass the shared contract suite
