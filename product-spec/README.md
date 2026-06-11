# Product Spec

This folder contains the shared product contracts for the native migration.

Goals:
- define platform-agnostic data contracts
- freeze the current Flutter behavior as the baseline
- provide canonical fixtures for Android, Web, and iOS parity tests
- document target architecture and feature rollout boundaries

Contents:
- `golden-reference.md`: behavior captured from the current Flutter app
- `feature-parity-matrix.md`: delivery scope across platforms
- `contracts/`: public DTOs and API contracts
- `fixtures/`: canonical request/response and rule-engine examples
- `tests/`: shared contract-test scenarios

Decision notes:
- offline-first remains mandatory
- local rule engine remains mandatory on every client
- backend services are optional support services, not product-critical runtime dependencies
