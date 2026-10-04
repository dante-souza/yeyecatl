# Scan State Model

Status: Accepted for Phase 0

This document defines how Yeyecatl should represent Wi-Fi scan state without
overstating what Android proves.

## Principle

Model platform preconditions, scan request outcomes, and result freshness as
different concepts. Do not collapse them into a single generic error, and do not
label a scan as throttled unless Android provides evidence for that exact cause.

## Conceptual State Model

```kotlin
sealed interface ScanState {
    data object Idle : ScanState
    data object WifiFeatureUnavailable : ScanState
    data object WifiDisabled : ScanState
    data object FineLocationPermissionRequired : ScanState
    data object LocationServicesRequired : ScanState
    data object Ready : ScanState
    data object ScanRequestInFlight : ScanState
    data class ScanRequestRejected(
        val cause: RejectionCause = RejectionCause.Unknown
    ) : ScanState
    data class Results(
        val observations: List<WifiAccessPointObservation>,
        val freshness: Freshness
    ) : ScanState
    data object NoResults : ScanState
    data class PermissionFailure(val api: WifiApiCall) : ScanState
    data class Error(val kind: ScanErrorKind) : ScanState
}
```

Supporting types:

```kotlin
enum class RejectionCause {
    Unknown,
    PreconditionsChanged
}

data class Freshness(
    val capturedAt: Instant,
    val newestObservationAge: Duration?,
    val classification: FreshnessClassification,
    val diagnostic: FreshnessDiagnostic?
)

enum class FreshnessClassification {
    FreshEnough,
    Stale,
    Unknown
}

enum class FreshnessDiagnostic {
    PassivePlatformScan,
    RequestRejectedBeforeResults,
    PossiblePlatformRateLimit,
    OemOrSubsystemDelay
}
```

Diagnostics are hints for logs or developer UI. They are not user-facing proof.

## Proven Versus Inferred States

| State / field | Proven by Android API? | Evidence | Notes |
|---|---:|---|---|
| `WifiFeatureUnavailable` | Yes | `PackageManager.hasSystemFeature(FEATURE_WIFI)` | Device does not report Wi-Fi support. |
| `WifiDisabled` | Yes | `WifiManager` Wi-Fi state | State can change after check. |
| `FineLocationPermissionRequired` | Yes | Runtime permission check | Also handle `SecurityException` after checks. |
| `LocationServicesRequired` | Yes | Location Services setting | Required by scan APIs on modern Android. |
| `Ready` | Mostly | Known preconditions pass | Still not a guarantee that scan request succeeds. |
| `ScanRequestInFlight` | Yes | `startScan()` returned `true` | Means initiated, not completed. |
| `ScanRequestRejected(Unknown)` | Yes | `startScan()` returned `false` | Cause is not reliably exposed. |
| `NoResults` | Yes | Empty `getScanResults()` list | Empty result is valid. |
| `Results` | Yes | Non-empty `getScanResults()` list | Freshness is separate. |
| `Freshness.Stale` | No, Yeyecatl classification | Age-at-capture threshold | Useful diagnostic, not direct platform state. |
| `PossiblePlatformRateLimit` | No, diagnostic only | Timing pattern around rejected requests | Must not be shown as definitive throttling. |
| `PermissionFailure` | Yes | `SecurityException` from protected API | Permissions/settings may change between checks and calls. |

## State Transition Direction

```text
Idle
  -> WifiFeatureUnavailable
  -> WifiDisabled
  -> FineLocationPermissionRequired
  -> LocationServicesRequired
  -> Ready
  -> ScanRequestInFlight
  -> Results / NoResults / ScanRequestRejected / PermissionFailure / Error
```

The adapter may read latest results after a rejected scan request, but the
result must carry freshness metadata. A rejected request followed by old results
is not the same as a successful fresh scan.

## User-Facing Wording

Preferred wording:

- "Scan request was not accepted."
- "Showing latest available results."
- "Results may be stale."
- "Location permission is required by Android to scan nearby Wi-Fi networks."
- "Turn on Location Services to scan nearby Wi-Fi networks."

Avoid:

- "Throttled" unless Android exposes a definitive cause.
- "Live networks" for cached results.
- "All nearby networks" because Android scan results are observations, not a
  complete RF census.

## Logging Policy

Production logs may include structural diagnostics:

```text
scan_state=ScanRequestRejected cause=Unknown
result_count=17 freshness=Stale
permission_state=FineLocationPermissionRequired
```

Production logs must not include SSID, raw SSID bytes, BSSID, MLD MAC, full
capabilities strings, or exported observations by default.
