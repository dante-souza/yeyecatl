# Phase 2D.5 — J8 validation checkpoint

Validated on Samsung Galaxy J8 (SM-J810M) before any D5.3 chart-state or auto-scale work.

## Frozen scope

- Final Phase 2D polling policy integrated:
  - 5 s and 10 s = Lab
  - 30 s = Default
  - 45 s, 60 s, 120 s = standard choices
- Nearby Signal History:
  - solid = measured scan-derived samples
  - dashed = bounded last-known presentation only
  - blank = no recent data
  - cadence-aware stale windows
  - dense All view emphasizes 8 strongest recent BSSIDs while retaining subdued background traces
- Connected AP RSSI:
  - separate foreground-only WifiInfo stream
  - 1 s reads without WifiManager.startScan()
  - separate history from nearby scan samples
  - SSID, BSSID, RSSI and frequency shown
  - rolling two-minute connected-link chart
  - history resets on BSSID change
  - repeated values are treated as Android WifiInfo reads, not guaranteed fresh radio measurements

## J8 observations

The connected-link view was observed working with:
- SSID: Te-Moana-nui-a-Kiwa
- BSSID: 04:d9:f5:73:7a:38
- frequency: 2442 MHz
- current RSSI observed around -21 to -23 dBm near the AP
- retained connected-link reads advancing past 100
- visible movement from roughly the -60/-70 dBm region back toward about -21 dBm while moving through the test environment

The nearby scan history and the connected-link stream remained separate.

## Build/CI status

Checkpoint code head before this validation note:
`ab0b7a42b62b583516a5a2ab53bf456412c8b7b2`

GitHub Actions run `37705726551` completed successfully for that code head.

## Deferred next block

D5.3 is intentionally not part of this checkpoint:
- keep the connected chart frame visible while disconnected but remove the active trace
- resume with a new segment after measurable reconnection
- Fixed / Auto RSSI Y-axis scaling
- stable quantized auto-range with hysteresis

This document marks the rollback baseline before those changes.


## D5.3 release-ready freeze — 2026-10-08

Validated on the Galaxy J8 as the release-ready connected-RSSI baseline.

### Included
- persistent Connected AP RSSI chart frame;
- no active trace while Wi-Fi/connected RSSI is unavailable;
- explicit waiting/disconnected chart state;
- connection-session IDs prevent false bridging across disconnect/reconnect;
- Fixed RSSI scale: -100 to -20 dBm;
- Auto RSSI scale:
  - uses every sample visible in the rolling two-minute chart;
  - adds padding and quantizes bounds to 10 dB steps;
  - keeps at least a 30 dB vertical span;
  - clamps to -100..-20 dBm;
  - expands immediately to avoid clipping;
  - shrinks only through hysteresis;
  - retains its scale while disconnected;
- connected-link history remains independent from nearby scan history.

### J8 observations
- Auto mode successfully followed a walk from weak signal into very strong signal near the AP.
- The chart displayed disconnect/reconnect as separate segments rather than a false continuous line.
- A prior Auto-scale edge case that flattened older weak visible samples against the lower axis boundary was corrected by deriving Auto bounds from the full visible two-minute window.
- Physical BSSID-roaming validation was not available in the test environment; automated session/BSSID break coverage remains in place.

### CI
Release-ready feature head before this validation note:
`95834a808075f0b2a6699d0154baff2d0c237896`

GitHub Actions run `37710586052` completed successfully for that exact head.

### Deferred beyond this release
Interactive history navigation, sideways panning, larger retained sessions, plot/image export, CSV/session export, annotations and other data-analysis workflows are intentionally out of scope for this Yeyecatl release.
