# ADR-008: Android Wi-Fi Scan Acquisition Lifecycle

Status: Accepted

Date: 2026-10-02

## Context

Phase 1C introduces actual Wi-Fi scan acquisition while preserving the Phase 0
and Phase 1B platform contract. Android scan APIs are permission-gated,
throttled, and broadcast-based, and modern Android can report scan results that
were not directly requested by this app.

## Decision

Implement a single `WifiScanRepository` contract with an Android implementation
under `platform/wifi`.

The Android implementation:

- checks the existing `WifiPlatformReadinessProvider` before scan requests;
- registers a dynamic `SCAN_RESULTS_AVAILABLE_ACTION` receiver with the
  application context;
- calls `WifiManager.startScan()` only after an explicit UI action;
- reads `WifiManager.EXTRA_RESULTS_UPDATED` to mark result freshness;
- maps `WifiManager.getScanResults()` into Android-independent observations;
- preserves previous observations when a request is rejected or when Android
  reports cached empty results.

## Rationale

- A repository contract keeps Android scan APIs out of UI and domain consumers.
- Dynamic receiver registration matches the foreground-only Phase 1C scope.
- Separating request source from result availability handles passive broadcasts
  on Android 10/API 29 and later.
- Preserving old snapshots avoids hiding useful observations when Android
  rejects a new request or returns stale data.

## Consequences

- `MainActivity` owns the lifecycle hook that starts and stops the repository.
- Phase 1C has no background acquisition and no manifest receiver.
- UI can show acquisition state without performing RF/channel interpretation.
- Later phases can add analyzer logic without changing Android API ownership.

## Related Documents

- `docs/architecture/android-wifi-scan-acquisition.md`
- `docs/architecture/android-wifi-platform-readiness.md`
- `docs/architecture/scan-state-model.md`
- `docs/adr/ADR-004-scan-freshness-and-state-semantics.md`
- `docs/adr/ADR-007-android-wifi-platform-readiness-boundary.md`
