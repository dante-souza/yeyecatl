# Wi-Fi Temporal Observation

Status: Phase 2A.1 baseline

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
