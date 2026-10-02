# Wi-Fi RF Interpretation

Status: Accepted for Phase 1D

Phase 1D adds a pure Kotlin RF interpretation layer. It derives band, primary
channel, channel width, center-frequency metadata, and Wi-Fi standard from raw
scan observations without changing or replacing the raw observation.

## Raw Versus Derived

`WifiScanObservation` remains the raw Android-independent acquisition record.
It preserves:

- primary frequency reported by Android;
- raw channel-width meaning normalized into a project enum;
- center frequency 0 and center frequency 1 when present;
- Wi-Fi standard when Android exposes it;
- raw capabilities string without security parsing.

`WifiRfInterpreter` produces `WifiRfCharacteristics` as derived data. UI code
can show both raw and interpreted values, but later analysis must still be able
to inspect the original observation.

## Band Classification

Yeyecatl recognizes:

- 2.4 GHz;
- 5 GHz;
- 6 GHz;
- 60 GHz;
- unknown.

Unknown is a valid result for missing, non-positive, or unsupported frequencies.
Device support is not inferred from Android version; observed scan values decide
what appears in the UI.

## Channel Conversion

The interpreter follows Android's `ScanResult` channel conversion behavior,
which references IEEE 802.11 channel numbering. Yeyecatl keeps the conversion in
domain code so it is deterministic and JVM-testable on the Phase 1 baseline.

### 2.4 GHz

- Channels 1 through 13 use 5 MHz spacing from 2412 MHz.
- Channel 14 is a special case at 2484 MHz.
- Frequencies between valid channel centers do not produce a channel.

### 5 GHz

- Supported conversion range is 5160 MHz through 5885 MHz with 5 MHz spacing.
- Yeyecatl does not make regulatory claims about whether a channel is legal in a
  user's country.

### 6 GHz

- 6 GHz is modeled separately from 5 GHz.
- Channel 1 starts at 5955 MHz.
- Channel 2 has the special operating-class center frequency 5935 MHz.
- Channels otherwise use 5 MHz spacing through channel 233 at 7115 MHz.
- Preferred scanning channels are not used for filtering in Phase 1D.

### 60 GHz

Yeyecatl recognizes the Android 60 GHz band range, but channel interpretation is
deferred. Phase 1D returns `band = 60 GHz` and `channel = unknown`.

## Channel Width

Android channel-width values are mapped to:

- 20 MHz;
- 40 MHz;
- 80 MHz;
- 160 MHz;
- 80+80 MHz;
- 320 MHz;
- unknown.

`160 MHz` and `80+80 MHz` remain distinct because their RF arrangements are
different. Phase 1D does not calculate occupied spectrum or overlap.

`320 MHz` is accepted only from API 33+ platform metadata; older API levels map
that raw value to `Unknown`.

## Primary And Center Frequencies

The primary frequency is the Android-reported primary 20 MHz frequency. Center
frequency 0 and center frequency 1 are preserved separately. Wider channels must
not assume the primary frequency is the center of the full occupied channel.

Phase 1D does not compute lower/upper occupied frequencies. Phase 1E adds that
nominal geometry in `docs/architecture/wifi-spectrum-geometry.md`.

## Wi-Fi Standard

Android `ScanResult.getWifiStandard()` is available from API 30. On API 29, or
when Android reports an unknown/unrecognized value, Yeyecatl uses
`WifiStandard.Unknown`.

Wi-Fi 7 / 802.11be values are accepted only for API 33+ metadata. Yeyecatl does
not infer Wi-Fi generation from frequency, width, SSID, or capabilities.

## Unknown-Value Policy

Unknown platform values are mapped to `Unknown` or nullable fields. Yeyecatl
does not silently coerce unknown data into the closest known RF value.

## Non-Goals

- Channel overlap in Phase 1D. Phase 1E adds geometric overlap only.
- Interference scoring.
- Channel recommendations.
- Regulatory-domain enforcement.
- Security classification.
- Signal-quality percentages.
- MLO interpretation.
- Persistence, history, export, charts, maps, or CI/CD.

## Sources

- Android `ScanResult` API reference: https://developer.android.com/reference/android/net/wifi/ScanResult
- Android `ScanResult` source and channel constants: https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/heads/android17-release/framework/java/android/net/wifi/ScanResult.java
- Android Wi-Fi 7 platform notes: https://source.android.com/docs/core/connect/wifi-7
