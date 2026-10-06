# Yeyecatl Phase 2-zero — Physical-Device Validation and Archaeological Record

## Milestone

Phase 2-zero establishes Yeyecatl's first static RSSI ranking and spectrum-display filtering milestone.

The phase remains deliberately snapshot-based. It does not introduce repeated scanning, observation history, moving averages, temporal trends, persistence, or Phase 2A temporal semantics.

## Validation date

2026-10-06

## Validation device

| Property | Value |
|---|---|
| Device | Samsung Galaxy J8 |
| Model | SM-J810M |
| Device codename | j8y18lte |
| Android | 10 |
| Android API level | 29 |
| ADB serial used for validation | `38c19745` |
| Application ID | `io.github.dante_souza.yeyecatl` |

## Host validation

The feature branch completed the repository host gate successfully:

```text
make check   PASS
make build   PASS
```

GitHub Actions CI also completed successfully for the accepted Phase 2-zero branch state.

Final pre-archaeology implementation commit:

```text
7dc22784298c269497660cc77aa9184cbaab9bef
```

CI run:

```text
37532476704 — PASS
```

## Physical validation

Validated on the Galaxy J8:

- debug APK installed successfully through ADB;
- application launched successfully on physical hardware;
- a real Android Wi-Fi scan populated the Yeyecatl snapshot;
- the validation capture contained 71 observed networks;
- the static `Signal ranking` card rendered the five strongest observations;
- the static `Signal ranking` card rendered the five weakest observations;
- SSID and BSSID remained visible as separate observation identity/context;
- RSSI ordering matched the intended strongest/weakest semantics;
- the existing observed-network count and diagnostics remained available;
- the spectrum-display selector was accepted on-device with the mutually exclusive modes:
  - `All`
  - `Strongest 5`
  - `Weakest 5`
- strongest/weakest spectrum selection operates within the currently selected Wi-Fi band;
- changing the spectrum selector does not trigger another Wi-Fi scan;
- the complete underlying scan snapshot is preserved while only the spectrum projection is filtered.

## Architectural boundary

```text
Android Wi-Fi scan
        |
        v
WifiScanSnapshot ----------------------> full diagnostics
        |
        +------------------------------> Signal ranking card
        |
        v
selected band
        |
        v
All / Strongest 5 / Weakest 5
        |
        v
WifiSpectrumChart
```

The selector is a presentation/projection choice over one already-acquired snapshot. It is not a scan-acquisition control.

## Static ranking semantics

For measurable RSSI observations:

```text
Strongest 5
  RSSI descending
  values nearest zero first

Weakest 5
  RSSI ascending
  most negative values first
```

Observations without RSSI are excluded from strongest/weakest ranking. Duplicate SSIDs are not collapsed: distinct BSSIDs remain distinct observations.

## Evidence

`yeyecatl-phase-2zero-j8-ranking.jpg`

The photograph captures the first accepted physical-device rendering of the Phase 2-zero ranking card. The screen shows:

- Yeyecatl running on the Galaxy J8;
- five strongest signals;
- five weakest signals;
- real SSID/BSSID data;
- real RSSI values;
- 71 observed networks.

The photograph predates the final spectrum selector addition, so it is intentionally not cited as visual proof of the selector UI. The selector was subsequently tested and accepted on the same physical-device validation path.

Image SHA-256:

```text
bbe4ac9954aee573e4a050cd09fc8ae53d95e811bdca1f90ffea1b54c602ade3
```

## Status

**PASS**

Phase 2-zero has demonstrated its intended static ranking and display-filter behavior on physical Android hardware.

This record freezes the milestone for software archaeology before subsequent analyzer or temporal-observation work changes the application.
