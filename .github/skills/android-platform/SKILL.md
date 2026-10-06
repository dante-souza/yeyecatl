---
name: "android-platform"
description: "Android platform rules for Yeyecatl: Wi-Fi permissions, scan lifecycle, capability detection, background restrictions and API-level behavior. Use for any code touching Android framework Wi-Fi or permissions."
---

# Skill — Android Platform

## Purpose
Keep Yeyecatl correct across Android versions and avoid assuming permissions or device capabilities.

## Current platform facts to preserve

For classic Wi-Fi scanning, Android documentation still requires careful handling of location-related access. In particular, `WifiManager.getScanResults()` and `startScan()` require location permission conditions and Location services enabled on relevant Android versions. Android 13+ also introduced `NEARBY_WIFI_DEVICES` for many nearby Wi-Fi APIs, but it must not be treated as a blanket replacement for the permissions required by scan-result APIs.

Official references:
- https://developer.android.com/develop/connectivity/wifi/wifi-scan
- https://developer.android.com/develop/connectivity/wifi/wifi-permissions
- https://developer.android.com/reference/android/net/wifi/ScanResult

## Mandatory rules

1. Before writing platform code, identify:
   - `minSdk`
   - `targetSdk`
   - API levels where behavior changes
   - exact API being called
2. Request only permissions required for the implemented feature.
3. Handle `SecurityException` defensively around permission-sensitive APIs.
4. Model these states separately:
   - permission not granted
   - Location services disabled when required
   - Wi-Fi disabled
   - scan throttled/unavailable
   - results empty
   - device/band unsupported
5. Never implement loops intended to defeat scan throttling.
6. Do not claim `neverForLocation` if the app actually derives physical location from Wi-Fi information.
7. Keep runtime permission UI understandable and tied to a user action.

## Capability policy

Do not infer hardware support from OS version alone.

Prefer:
- platform capability APIs when available
- observed scan results
- explicit unsupported/unknown UI states

## Design pattern

Keep framework calls behind an adapter boundary, e.g.:

```text
Domain use case
   ↓
WifiObservationRepository
   ↓
AndroidWifiScanner
   ↓
WifiManager / permissions / broadcasts
```

Domain code should not depend on `ScanResult` directly.
