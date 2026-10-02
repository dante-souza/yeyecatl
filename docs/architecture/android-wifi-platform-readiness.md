# Android Wi-Fi Platform Readiness

Status: Accepted for Phase 1B; superseded in part by Phase 1C scanning

Phase 1B establishes the Android boundary needed before Yeyecatl implements
Wi-Fi discovery. It does not start scans, collect scan results, parse RF data, or
persist observations.

Phase 1C keeps this readiness boundary and adds actual foreground scan
acquisition in `docs/architecture/android-wifi-scan-acquisition.md`.

## Implemented Scope

- Manifest declarations for Wi-Fi state inspection and future foreground Wi-Fi
  discovery permission flow.
- Android platform readiness provider under `platform/wifi`.
- Framework-independent readiness models for UI consumption.
- User-triggered runtime permission request for future scan discovery.
- Compose readiness screen that clearly says the scanner is not implemented.
- JVM tests for API-level permission decisions and readiness blocking logic.

## Manifest Declarations

| Declaration | Why it exists |
|---|---|
| `android.permission.ACCESS_WIFI_STATE` | Lets Yeyecatl inspect Wi-Fi availability and state without scanning. |
| `android.permission.ACCESS_FINE_LOCATION` | Required by Android scan-result APIs for future nearby Wi-Fi discovery on the Phase 1 SDK baseline. |
| `android.permission.ACCESS_COARSE_LOCATION` | Declared and requested with fine location for Android 12+ location-permission UX and lint compatibility. Coarse alone is not treated as sufficient for Wi-Fi scans. |
| `android.permission.CHANGE_WIFI_STATE` | Required when a later phase asks `WifiManager.startScan()` to initiate foreground scans. Phase 1B does not call it. |
| `android.hardware.wifi` with `required=false` | Allows the app to install and explain unsupported Wi-Fi state instead of being hidden from non-Wi-Fi devices. |

Yeyecatl does not declare `NEARBY_WIFI_DEVICES`, `ACCESS_BACKGROUND_LOCATION`, or
`INTERNET` in Phase 1B/1C.

## API-Level Permission Model

For the selected Phase 1 baseline (`minSdk = 29`, `targetSdk = 36`), nearby
Wi-Fi discovery requires a runtime fine-location permission before scan-result
APIs can be used.

The platform-independent policy still models older Android behavior:

| API level | Runtime permission requirement |
|---:|---|
| `< 23` | Not required by Android's runtime permission system. |
| `23+` | Required for nearby Wi-Fi discovery permission flow. |

Android 13's `NEARBY_WIFI_DEVICES` is intentionally not used here because
Android documentation still requires `ACCESS_FINE_LOCATION` for
`WifiManager.startScan()` and `WifiManager.getScanResults()`.

## Architecture Boundary

```text
MainActivity / Compose UI
    |
WifiPlatformReadiness models
    |
WifiPlatformReadinessProvider
    |
AndroidWifiPlatformReadinessProvider
    |
Context / PackageManager / WifiManager / LocationManager / permissions
```

Only `AndroidWifiPlatformReadinessProvider` imports Android Wi-Fi, permission,
SDK, and service APIs. Domain packages remain Android-free.

## Runtime Permission Interaction

Permission is not requested on startup. The readiness UI exposes an explicit
user action when the app can request the future scan permission from inside the
app.

The model can represent:

- granted;
- not granted;
- requires app settings action;
- not required for the current API level.

Android does not expose a perfect permanent-denial flag. Yeyecatl classifies
"requires app settings" only after a request was attempted and Android no longer
shows rationale for the permission.

## Deliberately Unimplemented In Phase 1B

- `WifiManager.startScan()`;
- scan-result collection;
- scan repositories;
- periodic or background scanning;
- RF, band, channel, RSSI, SSID, or BSSID processing;
- persistence;
- export;
- maps, charts, or history.

## Validation

Use:

```text
make setup
make build
make unit-test
make lint
make check
```

`make android-test` is available for connected instrumentation tests when an
emulator or physical device is already present.
