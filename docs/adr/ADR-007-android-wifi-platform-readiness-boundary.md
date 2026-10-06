# ADR-007: Android Wi-Fi Platform Readiness Boundary

Status: Accepted

Date: 2026-10-02

## Context

Phase 1B needs to prepare Android Wi-Fi platform access without implementing
actual scanning. The project must keep Android framework types out of future
domain logic and avoid leaking Android permission constants into UI or domain
code.

## Decision

Create a small `platform/wifi` boundary containing:

- framework-independent readiness models;
- a pure API-level permission policy;
- an Android implementation that reads Wi-Fi hardware availability, Wi-Fi power
  state, Location Services state, and fine-location permission grant state.

Use Activity Result APIs for user-triggered permission requests, but do not
request permission on startup.

## Rationale

- The platform boundary gives the UI enough state to explain readiness before
  scanning exists.
- Pure policy code can be tested with JVM tests.
- Android framework details stay out of the domain package.
- The app can distinguish "not ready" causes without pretending to scan.

## Consequences

- `MainActivity` owns the Android permission launcher because it is lifecycle
  and activity-result API integration.
- `AndroidWifiPlatformReadinessProvider` is the only Phase 1B class that touches
  `WifiManager`, `LocationManager`, `Build.VERSION`, and Android permission
  constants.
- Future Phase 1 scanning must reuse this boundary instead of adding duplicate
  permission or Wi-Fi state checks.

## Related Documents

- `docs/architecture/android-wifi-platform-readiness.md`
- `docs/research/android-wifi-permissions-matrix.md`
- `docs/adr/ADR-002-wifi-scan-permission-contract.md`
