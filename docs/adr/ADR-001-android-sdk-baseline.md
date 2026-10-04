# ADR-001: Android SDK Baseline

Status: Accepted

Date: 2026-10-02

## Context

Yeyecatl needs an Android baseline before Phase 1 scaffolding. The baseline
affects Wi-Fi scan permission behavior, Android tooling, test scope, and
realistic device support.

## Decision

Use these Phase 1 values:

| Setting | Value |
|---|---:|
| `minSdk` | 29 |
| `targetSdk` | 36 |
| `compileSdk` | 36 |

These are decisions for Phase 1, not permanent requirements.

## Rationale

- API 29 gives a cleaner baseline for scan permission behavior because apps
  targeting API 29 or higher need `ACCESS_FINE_LOCATION` for scan APIs.
- API 29 avoids extra Android 8/9 compatibility paths during the first scanner.
- API 36 aligns with Google Play's 2026 target API requirement for new apps and
  updates.
- API 36 avoids adopting Android 17 target behavior before Yeyecatl needs it.

## Consequences

- Android 8 and Android 9 devices are not supported by the Phase 1 baseline.
- `java.time` should still use Android desugaring if broad runtime support is
  needed.
- Future support for older devices requires an explicit product decision and
  adapter-only compatibility work.

## Related Documents

- `docs/research/android-wifi-platform-contract.md`
- `docs/research/android-wifi-permissions-matrix.md`
