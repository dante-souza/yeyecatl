# Wi-Fi Temporal Observation

Status: Phase 2A.2 foreground cadence baseline

Phase 2A introduces time as a domain dimension without changing the frozen
Phase 2-zero ranking semantics.

## Goal

Build deterministic, in-memory RSSI history from repeated scan snapshots before
adding automatic scan cadence, UI animation, persistence, smoothing, or trend
analysis.

## Temporal Identity

Temporal history is grouped by BSSID.

SSID is descriptive metadata and must not be used as temporal identity because
multiple BSSIDs may advertise the same SSID.

Observations without a BSSID cannot be assigned to a stable temporal series and
are therefore excluded from Phase 2A history.

## Sample Time

`WifiScanSnapshot.receivedAtMillis` is the capture boundary used for temporal
samples.

`WifiScanObservation.platformTimestampMicros` remains Android platform
metadata. It is boot-relative and must not be treated as Yeyecatl wall-clock
capture time.

## Freshness Rule

Only snapshots classified as `WifiScanFreshness.Fresh` create temporal samples.

Cached snapshots do not create new history points because doing so would turn
repeated cached data into false signal movement.

Snapshots with `Unknown` freshness are also excluded in this first Phase 2A
baseline. This is deliberately conservative and can be revisited only with
explicit platform evidence and tests.

## Sample Contract

```kotlin
data class WifiSignalSample(
    val bssid: String,
    val ssid: ObservedSsid,
    val rssiDbm: Int,
    val frequencyMhz: Int?,
    val observedAtMillis: Long
)
```

A temporal sample requires:

- BSSID;
- RSSI;
- a fresh snapshot;
- the snapshot receipt time.

Frequency remains optional because Yeyecatl must preserve unknown platform data
rather than invent a value.

## History Contract

```kotlin
data class WifiTemporalObservationHistory(
    val samplesByBssid: Map<String, List<WifiSignalSample>>
)
```

The history is immutable from the caller's perspective. The accumulator returns
a new history when fresh measurable samples are appended.

Each BSSID series retains at most 120 samples by default. This prevents a
foreground dynamic-scan session from growing each signal series without bound.
The limit is a memory-safety bound, not persistence and not a smoothing window.

## Phase 2A.1 Non-Goals

This block intentionally does not add:

- periodic or automatic scan scheduling;
- timers or background scanning;
- Compose signal-history charts;
- moving averages or smoothing;
- strongest/weakest ranking across time;
- persistence or Room;
- retention windows;
- interpolation for missing observations;
- AP/vendor grouping.

Those concerns depend on a correct temporal domain contract and belong to later
Phase 2A blocks.

## Required Tests

The first temporal block must prove that:

- fresh RSSI observations become samples;
- snapshot receipt time becomes the sample time;
- repeated observations append in chronological capture order;
- identical SSIDs with different BSSIDs remain distinct;
- missing BSSID or RSSI is excluded;
- cached snapshots do not fabricate history;
- unknown-freshness snapshots do not fabricate history.


## Phase 2A.2 Foreground Dynamic Scan Cadence

Phase 2A.2 adds an explicit user-controlled repeated scan mode.

The cadence controller is platform-neutral and owns only scheduling semantics.
The Android implementation supplies a main-thread delayed scheduler. The
existing `AndroidWifiScanRepository` remains responsible for one scan request
and one scan-result acquisition at a time.

```text
Start dynamic scan
       |
       v
WifiForegroundScanCadence
       |
       | every 30 seconds while foreground
       v
WifiScanRepository.requestScan()
       |
       v
Android WifiManager
       |
       v
fresh/cached/unknown result semantics
       |
       +--> latest snapshot
       |
       +--> fresh-only temporal history
```

### Foreground Lifecycle

Dynamic mode is opt-in. It does not start automatically.

When enabled:

- entering the Activity foreground triggers an immediate scan request;
- the next request is scheduled 30 seconds later;
- leaving the foreground cancels the pending scheduled request;
- the enabled preference remains in memory while the Activity instance exists;
- re-entering the foreground resumes with an immediate request;
- disabling dynamic mode cancels the pending request.

No background service, foreground service, WorkManager job, alarm, or hidden
polling loop is introduced.

### Android Throttling

Android documents a foreground limit of four `WifiManager.startScan()`
requests per two-minute period on Android 9, with the same limit applying to
Android 10 and later.

Reference:

- https://developer.android.com/develop/connectivity/wifi/wifi-scan

The Phase 2A cadence therefore uses a 30-second default interval and still
treats `startScan() == false` as an ordinary rejected request. Yeyecatl does not
retry aggressively, disable throttling, or attempt to bypass platform limits.

The platform may still reject individual requests. Other platform conditions
can also change between scheduled requests, so every request continues through
the existing readiness and rejection state model.

### UI Contract

The scanner screen exposes:

- `Start dynamic scan` / `Stop dynamic scan`;
- dynamic mode status;
- tracked BSSID count;
- total retained signal-sample count.

Manual `Scan Wi-Fi` is disabled while dynamic mode is enabled so the UI does
not intentionally add manual scan requests on top of the cadence.

If scan readiness becomes unavailable, dynamic mode is disabled.

### Repository History State

`WifiScanRepository` now exposes an in-memory
`StateFlow<WifiTemporalObservationHistory>`.

Fresh scan-result broadcasts append measurable BSSID/RSSI observations.
Cached and unknown-freshness snapshots remain visible through the latest scan
state but do not append temporal samples.

## Phase 2A.2 Non-Goals

This block still does not add:

- background scanning;
- scan-throttling bypasses;
- persisted history or Room;
- signal smoothing or moving averages;
- interpolation for missing observations;
- a signal-over-time chart;
- strongest/weakest ranking across a time window;
- cross-session history.

Those belong after the foreground acquisition loop has been validated on real
hardware.
