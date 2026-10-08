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
