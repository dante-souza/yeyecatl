# Android Wi-Fi Scan Acquisition

Status: Accepted for Phase 1C

Phase 1C adds the first real Wi-Fi acquisition path. It requests one foreground
scan only after a user action, listens for Android scan-result availability, and
maps `ScanResult` values into Android-independent Yeyecatl observations.

It does not analyze RF/channel data, persist observations, export identifiers,
or perform periodic/background scanning.

## Implemented Scope

- Android-independent scan repository contract in the app domain layer.
- Immutable scan observation and scan state models.
- Android `WifiManager` adapter under `platform/wifi`.
- Dynamic `SCAN_RESULTS_AVAILABLE_ACTION` receiver owned by the platform
  repository implementation.
- Compose development UI button for explicit scan requests and a basic
  observation list.
- JVM tests for state transitions, readiness blocking, and raw observation
  mapping.

## Architecture Boundary

```text
Compose UI / MainActivity
    |
WifiScanRepository contract
    |
AndroidWifiScanRepository
    |
WifiPlatformReadinessProvider + WifiManager
    |
SCAN_RESULTS_AVAILABLE_ACTION
```

Android framework types stay inside `platform/wifi` and `MainActivity`.
Domain scan models do not expose `WifiManager`, `ScanResult`,
`BroadcastReceiver`, `Intent`, or Android permission constants.

## Scan Request Versus Result Availability

Android does not guarantee that every scan-result broadcast was caused by
Yeyecatl. On Android 10/API 29 and later, apps can receive
`SCAN_RESULTS_AVAILABLE_ACTION` for full scans performed by the system or by
another app.

Yeyecatl therefore models:

- `ApplicationRequest` when a broadcast arrives after Yeyecatl accepted a
  user-requested scan.
- `PassiveAvailability` when results become available without an in-flight
  Yeyecatl request.

## Freshness Semantics

The repository reads `WifiManager.EXTRA_RESULTS_UPDATED` from the broadcast:

| Extra value | Yeyecatl freshness |
|---|---|
| `true` | `Fresh` |
| `false` | `Cached` |
| absent | `Unknown` |

`WifiManager.getScanResults()` may return cached data. If Android reports a
cached update with no returned observations and Yeyecatl already has a previous
snapshot, the previous observations are preserved and marked cached. A rejected
scan request also preserves previous observations.

`ScanResult.timestamp` remains platform-specific metadata in the raw Phase 1C
observation. It is not treated as a portable capture time.

## Receiver Lifecycle

`AndroidWifiScanRepository.start()` registers a dynamic, not-exported receiver
against the application context using AndroidX `ContextCompat.registerReceiver`.
`stop()` unregisters it deterministically and clears in-flight request state.

`MainActivity` starts the repository in `onStart()` and stops it in `onStop()`.
No manifest-declared receiver is used because Phase 1C only supports foreground,
user-driven acquisition.

## Platform Preconditions

Before calling `WifiManager.startScan()`, the Android adapter checks the Phase
1B readiness boundary:

- Wi-Fi hardware is available.
- Wi-Fi is enabled.
- Location Services are enabled.
- The required runtime scan permission is available.

If any condition is missing, the scan is not attempted and the state becomes
`Blocked` with the relevant reason.

The manifest declares and the UI requests both `ACCESS_FINE_LOCATION` and
`ACCESS_COARSE_LOCATION` for Android 12+ permission UX compatibility, but the
readiness contract treats fine location as the required scan permission. Coarse
location alone is not considered scan-ready.

## Throttling And Rejection

`WifiManager.startScan()` returns a boolean. When it returns `false`, Yeyecatl
records `RequestRejected` and explains that Android may have rejected or
throttled the request. It does not retry automatically.

Phase 1C deliberately avoids scan loops, timers, services, foreground services,
and background polling.

## Privacy

SSID and BSSID are shown only in the local diagnostic UI. Phase 1C does not log,
persist, export, upload, group, vendor-classify, or enrich them.

SSID display text and raw SSID bytes are modeled separately where Android
exposes bytes, so unusual or non-UTF-8 SSIDs are not silently reduced to display
text.

## Deliberately Unimplemented

- RF/channel analysis.
- Channel number, band, overlap, utilization, or signal-quality calculations.
- Security normalization.
- Access-point grouping.
- Persistence, history, export, upload, telemetry, charts, maps, or CI/CD.
