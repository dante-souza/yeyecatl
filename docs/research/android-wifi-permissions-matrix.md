# Android Wi-Fi Permissions Matrix

Status: Accepted for Phase 0

This document defines the Phase 1 permission contract for Yeyecatl's foreground
Wi-Fi scanner. It is scoped to `minSdk = 29`, `targetSdk = 36`, and
`compileSdk = 36` as Phase 1 decisions.

## Selected Target Matrix

| Requirement | Manifest | Runtime request | Phase 1 decision | Reason |
|---|---:|---:|---|---|
| `ACCESS_FINE_LOCATION` | Yes | Yes | Required | `startScan()` and `getScanResults()` require fine location for apps targeting API 29+. |
| `ACCESS_WIFI_STATE` | Yes | No | Required | Required for reading scan results and Wi-Fi state/capability APIs. |
| `CHANGE_WIFI_STATE` | Yes | No | Required if Yeyecatl calls `startScan()` | Required to initiate Wi-Fi scans. |
| `NEARBY_WIFI_DEVICES` | No | No | Not part of Phase 1 scanner | Android docs state `startScan()` and `getScanResults()` still require `ACCESS_FINE_LOCATION`, even for target 33+. |
| Location Services enabled | Not a manifest permission | User setting | Required platform precondition | Scan APIs fail when Location Services are disabled on modern Android. |
| `ACCESS_BACKGROUND_LOCATION` | No | No | Not allowed for Phase 1 | Phase 1 is foreground and user-triggered only. |
| `INTERNET` | No | No | Not required | No cloud upload, telemetry, or local-network feature in Phase 1. |

## Manifest Shape

The future manifest should contain only the scanner permissions needed for the
implemented feature:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
```

Do not add `NEARBY_WIFI_DEVICES` during Phase 1 unless a separate nearby Wi-Fi
feature requires it. If it is added later, do not use `neverForLocation` for any
feature that derives location-like meaning from Wi-Fi observations.

## API-Level Behavior

| API level | Scan permission behavior | Location Services | Yeyecatl handling |
|---|---|---|---|
| API 26-27 | `getScanResults()` can succeed with one of fine location, coarse location, or `CHANGE_WIFI_STATE`. | Platform scan restrictions exist. | Not Phase 1 baseline. If supported later, isolate compatibility in Android adapter. |
| API 28 | `startScan()` requires location permission, `CHANGE_WIFI_STATE`, and Location Services enabled. | Required. | Not Phase 1 baseline. |
| API 29-32 | Apps targeting 29+ need `ACCESS_FINE_LOCATION` for `startScan()` and `getScanResults()`. | Required. | Phase 1 baseline path. |
| API 33-36 | `NEARBY_WIFI_DEVICES` exists for nearby Wi-Fi APIs, but Android docs still list `startScan()` and `getScanResults()` as requiring `ACCESS_FINE_LOCATION`. | Required. | Keep fine location permission. Do not claim scan results are location-free. |
| API 37+ | Android 17 adds local-network permission behavior for LAN traffic, not core scan-result retrieval. | Required for scan APIs unless Android changes this contract. | Future review before targeting API 37. |

## NEARBY_WIFI_DEVICES Versus Location

Documented behavior:

- Android 13 introduced `NEARBY_WIFI_DEVICES` for many nearby Wi-Fi APIs.
- The permission belongs to the Nearby devices permission group.
- Android documentation explicitly lists `WifiManager.startScan()` and
  `WifiManager.getScanResults()` as examples that still require
  `ACCESS_FINE_LOCATION`.

Yeyecatl design:

- Wi-Fi scan results are location-sensitive environmental observations.
- Phase 1 does not declare `NEARBY_WIFI_DEVICES`.
- Phase 1 does not set `neverForLocation`.
- If later features use nearby Wi-Fi APIs that do not return scan observations,
  add a separate permission review before changing the manifest.

## Runtime Permission Flow

Phase 1 UI state must distinguish:

1. Wi-Fi feature unavailable.
2. Wi-Fi disabled.
3. Fine location permission missing.
4. Location Services disabled.
5. Ready to scan.
6. Scan request accepted.
7. Scan request rejected with unknown cause.
8. Results available.
9. No results.
10. Permission failure from a protected API call.

Permission checks before scanning:

1. `PackageManager.hasSystemFeature(PackageManager.FEATURE_WIFI)`.
2. `ACCESS_FINE_LOCATION` grant.
3. `ACCESS_WIFI_STATE` manifest availability.
4. Wi-Fi enabled/current Wi-Fi state.
5. Location Services enabled.
6. Call `startScan()` only after known preconditions pass.
7. Wrap `startScan()` and `getScanResults()` in defensive `SecurityException`
   handling because permissions can be revoked.

## Privacy Notes

Yeyecatl treats SSID, raw SSID bytes, BSSID, MLD MAC, MLO link metadata, capture
time, and optional future survey notes as sensitive environmental data.

Phase 1 constraints:

- No background collection.
- No automatic upload.
- No production logs containing SSID, BSSID, raw SSID bytes, MLD MAC, or full
  scan-result dumps.
- Exports require explicit user action in a later phase.
- Permission rationale must explain that Android requires location permission to
  observe nearby Wi-Fi networks.

## Sources

- Android Wi-Fi scanning overview: https://developer.android.com/develop/connectivity/wifi/wifi-scan
- Request permission to access nearby Wi-Fi devices: https://developer.android.com/develop/connectivity/wifi/wifi-permissions
- Runtime permissions best practices: https://developer.android.com/training/permissions/requesting
- Location permissions: https://developer.android.com/develop/sensors-and-location/location/permissions
- `Manifest.permission` reference: https://developer.android.com/reference/android/Manifest.permission
- Android 16 local network permission: https://developer.android.com/privacy-and-security/local-network-permission
