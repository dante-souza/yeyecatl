# Android Wi-Fi Platform Contract

Status: Accepted for Phase 0

This document records the Android platform behavior Yeyecatl must respect before
Phase 1 implementation. It is research and design guidance only. It does not
create an Android application scaffold.

## Phase 1 SDK Baseline

Decision for the first Android implementation:

| Setting | Phase 1 value | Rationale |
|---|---:|---|
| `minSdk` | 29 | Keeps the scan permission model simpler: apps targeting API 29 or higher need `ACCESS_FINE_LOCATION` for scan APIs. |
| `targetSdk` | 36 | Aligns with Google Play's 2026 target API requirement for new apps and updates. |
| `compileSdk` | 36 | Lets the project target Android 16 APIs without adopting Android 17 behavior yet. |

These are Phase 1 decisions, not immutable project requirements. Revisit them
when product support, Play policy, Android tooling, or device testing changes.

## minSdk Alternatives

| Baseline | Wi-Fi API behavior | `java.time` / desugaring | Permission complexity | Channel-width support | Testability | Device support | Decision |
|---|---|---|---|---|---|---|---|
| API 26 | Android 8 introduced documented scan restrictions; `getScanResults()` permission rules differ from modern target behavior. | Requires desugaring for broad `java.time` use. | Highest branching among the options. | `channelWidth`, `centerFreq0`, and `centerFreq1` exist because they were added in API 23. | More legacy paths to test. | Broadest. | Do not choose unless old-device support becomes explicit product scope. |
| API 28 | Android 9 has the modern foreground/background scan throttling shape; `startScan()` is deprecated starting here. | Requires desugaring for broad `java.time` use. | Still pre-Android 10 target permission differences. | Same as API 26. | Moderate branching. | Broad. | Acceptable fallback, but not preferred. |
| API 29 | Android 10 scan permission behavior is a cleaner baseline for target 29+ apps. | Desugaring still useful, but toolchain support is mature. | Lowest branching among the compared options. | Same as API 26; API 33 adds 320 MHz constant. | Best balance for initial implementation. | Realistic for a modern field tool. | Recommended Phase 1 baseline. |

## Scanning Workflow

Documented Android behavior:

1. Register for scan result completion using `SCAN_RESULTS_AVAILABLE_ACTION`, or
   on API 30 and higher use `WifiManager.registerScanResultsCallback()`.
2. Call `WifiManager.startScan()` if the app needs to request a new scan.
3. Read results with `WifiManager.getScanResults()` after a completion event or
   when using cached/latest platform results.

Yeyecatl design decisions:

- Phase 1 supports foreground, user-triggered refresh only.
- Phase 1 may passively accept scan completion events caused by the platform or
  other apps, but must label freshness honestly.
- No loop may exist for the purpose of bypassing Android scan limits.
- The UI must not claim a result is live unless the adapter can support that
  claim with timing evidence.

## API-Level Contract

| Area | Android API | Minimum / difference | Documented behavior | Yeyecatl decision |
|---|---|---|---|---|
| Scan request | `WifiManager.startScan()` | Added API 1; deprecated API 28. | Returns immediately. `true` means the scan request was initiated. Results arrive asynchronously. | Treat `false` as scan-request rejection with unknown cause unless Android gives more evidence. |
| Scan completion | `SCAN_RESULTS_AVAILABLE_ACTION` | API 1. On API 29+, broadcast can be sent for any full scan on device. | Broadcast includes success/failure status. | Use it as a result-availability signal, not proof that Yeyecatl's own scan ran. |
| Scan callback | `WifiManager.ScanResultsCallback` | API 30. | Callback fires when scan results are available. | Prefer on API 30+ if implementation remains simpler than broadcast handling. |
| Results retrieval | `WifiManager.getScanResults()` | API 1. | Returns latest AP scan results. Requires permissions and Location Services on modern Android. | Normalize immediately into project-owned types. Android `ScanResult` never leaves adapter/data layer. |
| Scan throttling | Platform behavior | API 26 introduced restrictions; API 28 tightened them. | API 28+: foreground apps can request four scans per two minutes; all background apps combined get one scan per 30 minutes. API 29+ keeps those limits. | Do not expose a definitive `Throttled` state unless the platform exposes that cause. Represent request rejection and stale results separately. |
| Freshness timestamp | `ScanResult.timestamp` | API 17. | Monotonic microseconds since boot when this result was last seen. | Keep raw value only in Android-specific metadata. Portable model exposes age-at-capture, not boot-relative time. |
| Primary frequency | `ScanResult.frequency` | API 1. | Primary 20 MHz frequency in MHz. | Store as `primaryFrequencyMhz`; use as source data for band/channel derivation. |
| Channel width | `ScanResult.channelWidth` | API 23; `CHANNEL_WIDTH_320MHZ` added API 33. | Reports AP channel bandwidth. | Store normalized width with `UNKNOWN` fallback. |
| Segment centers | `ScanResult.centerFreq0`, `centerFreq1` | API 23. | `centerFreq0` is used for 40/80/160/320 or first 80+80 segment; `centerFreq1` is used for second 80+80 segment. | Store both for channel occupancy, 80+80, 160 MHz, and 320 MHz analysis. |
| SSID | `ScanResult.SSID`, `getWifiSsid()` | `SSID` API 1, deprecated API 33; `getWifiSsid()` API 33. | Network name may be hidden, empty, or not faithfully represent raw bytes. | Store display text separately from raw SSID bytes when bytes are available. |
| BSSID | `ScanResult.BSSID` | API 1. | BSSID address reported for the observed BSS. Multi-BSSID support can return separate results for transmitted and non-transmitted BSSIDs on capable devices. | Treat as BSS/BSSID identity, not physical AP identity. Do not merge by SSID. |
| Security raw data | `ScanResult.capabilities` | API 1. | Raw authentication/key management/encryption description. | Preserve as `capabilitiesRaw`. |
| Security types | `ScanResult.getSecurityTypes()` | API 33. | Returns an array of `WifiInfo.SECURITY_TYPE_*`; multiple values are possible. | Normalize to `Set<SecurityType>` while preserving raw capabilities. |
| Wi-Fi standard | `ScanResult.getWifiStandard()` | API 30. | Reports legacy, 11n, 11ac, 11ax, 11ad, 11be, or unknown. | Store only observed values. Do not infer unsupported standards. |
| Wi-Fi 7 MLO | `getApMldMacAddress()`, `getApMloLinkId()`, `getAffiliatedMloLinks()` | API 33. | Exposes MLO metadata for Wi-Fi 7 APs when available. | Model MLD identity and MLO link identity separately. |
| Band capability | `is5GHzBandSupported()`, `is6GHzBandSupported()`, `is24GHzBandSupported()` | 5 GHz API 21; 6 GHz API 30; 2.4 GHz API 31. | Reports chipset band support. | Represent support as supported, unsupported, or unknown. Observed scan results may also prove observed band visibility. |
| General Wi-Fi feature | `PackageManager.FEATURE_WIFI` | API 8. | Reports device Wi-Fi networking support. | If absent, expose Wi-Fi feature unavailable. |

## Scan Request Rejection Is Not Throttling

Android documents scan frequency limits, but `startScan()` returning `false` does
not itself identify why the request was rejected. Possible causes include scan
limits, Wi-Fi state, platform policy, OEM behavior, or transient subsystem state.

Yeyecatl therefore uses:

- `ScanRequestRejected(Unknown)` for an unclassified `false` return.
- `Freshness.Stale` when age-at-capture exceeds Yeyecatl's chosen threshold.
- diagnostic notes such as "possible platform rate limit" only when the timing
  pattern supports that hint.

The product must not display "throttled" as a proven state unless Android
provides a specific signal in a future API.

## Freshness and Android Timestamps

`ScanResult.timestamp` is Android monotonic time since boot. It is useful inside
the Android adapter, but it is not portable across devices, operating systems,
exports, or later Ehecatl comparison.

Adapter behavior:

1. Read Android elapsed realtime near capture.
2. Compare elapsed realtime with `ScanResult.timestamp` when available.
3. Store portable `ageAtCapture` on the observation.
4. Keep raw Android timestamp only in Android-specific metadata.

## Identity Semantics

Yeyecatl must not treat BSSID as "physical access point identity."

The model distinguishes:

- observation identity: one normalized record captured by Yeyecatl;
- BSS/BSSID identity: the reported MAC-like identity of one basic service set;
- MLO link identity: the reported link ID for a Wi-Fi 7 MLO link;
- MLD identity: the reported multi-link device MAC address for a Wi-Fi 7 AP.

This keeps duplicate SSIDs, multi-BSSID deployments, mesh systems, and Wi-Fi 7
MLO from being accidentally collapsed.

## Device and OEM Uncertainty

Android APIs define the contract, but OEM implementations can vary. Phase 1 must
expect:

- empty results after a successful request;
- stale cached results after a rejected request;
- missing or zero channel center frequencies;
- unknown channel width or security data;
- absent 6 GHz observations on devices where the OS version is recent;
- vendor differences in multi-BSSID and Wi-Fi 7 MLO reporting.

## Sources

- Android Wi-Fi scanning overview: https://developer.android.com/develop/connectivity/wifi/wifi-scan
- Request permission to access nearby Wi-Fi devices: https://developer.android.com/develop/connectivity/wifi/wifi-permissions
- `WifiManager` reference: https://developer.android.com/reference/android/net/wifi/WifiManager
- `ScanResult` reference: https://developer.android.com/reference/android/net/wifi/ScanResult
- `PackageManager` feature reference: https://developer.android.com/reference/android/content/pm/PackageManager
- Google Play target API requirement: https://developer.android.com/google/play/requirements/target-sdk
- Android Gradle Plugin API support: https://developer.android.com/build/releases/about-agp
