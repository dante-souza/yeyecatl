# Phase 2A Validation — Temporal Observation

## Phase 2A.2 — Foreground Dynamic Scan Cadence

Status: physically validated on Samsung Galaxy J8 (SM-J810M), Android 10.

Validation date: 2026-10-06.

### Build and installation

Repository checks completed successfully before device installation:

```text
BUILD SUCCESSFUL
validated 23 agent/skill files
```

The debug APK then installed successfully on the Galaxy J8.

### Dynamic cadence evidence

With dynamic scan enabled, the device produced accepted application-requested
scans at approximately 30-second intervals:

| Request time | Result | Freshness | Observations |
|---|---|---|---:|
| 19:43:04 | accepted | Fresh | 41 |
| 19:43:34 | accepted | Fresh | 55 |
| 19:44:04 | accepted | Fresh | 51 |
| 19:44:34 | accepted | Fresh | 56 |
| 19:45:04 | accepted | Fresh | 57 |

Observed request spacing was approximately 30 seconds, matching the Phase 2A.2
cadence contract.

All mapped observations in this validation run were reported in the 2.4 GHz
band, with 20 MHz and 40 MHz channel widths. This describes only what the J8
observed during this run; it is not a statement about device-wide or environment
capability.

### Rejection and cached-result behavior

An additional request at 19:45:14 was rejected by Android. Yeyecatl then
received a passive scan-results broadcast with:

```text
resultsUpdated=false
freshness=Cached
```

The exact Android rejection cause is intentionally not asserted. The important
Phase 2A behavior is that the result remained classified as cached rather than
being treated as a new temporal observation.

This confirms the existing rejection/freshness boundary continues to work under
repeated foreground scanning.

### Privacy check

The debug log emitted structural scan diagnostics only:

- request attempted / accepted / rejected;
- result source;
- freshness;
- result count;
- observed band set;
- observed channel-width set.

No SSID or BSSID values were present in the supplied validation log.

### Phase 2A.2 exit result

PASS for:

- repository checks;
- APK installation on physical hardware;
- explicit dynamic scan start;
- repeated foreground scan requests;
- approximately 30-second cadence;
- fresh result acquisition across multiple cycles;
- Android request rejection handling;
- cached-result classification without claiming fresh history.

Phase 2A.3 may build signal-over-time visualization on the now physically
validated temporal acquisition path.
