# Android Device Validation

Status: Accepted for Phase 1G

This document defines the reusable manual validation workflow for Yeyecatl on
physical Android hardware. It records procedures only; do not commit private
SSID, BSSID, account, contact, file, phone-number, advertising-ID, or installed
application inventory data.

## Prerequisites

- Android SDK platform tools available on `PATH`.
- USB debugging enabled on the Android device.
- Device authorized for `adb`.
- Root repository commands run through `make`.

## Make Targets

```text
make adb-devices
make setup
make assemble-debug
make install-debug
make device-info
make device-smoke
make app-logcat
make device-diagnostics
make android-test
```

`make device-diagnostics` writes sanitized output under
`build/device-diagnostics/`, which is ignored with normal build artifacts.

## Device Information

Run:

```text
make adb-devices
make device-info
```

Record only:

- manufacturer;
- model;
- Android release;
- API level;
- ABI;
- screen size/density;
- Wi-Fi feature availability;
- Yeyecatl package version when installed.

## Install And Launch

```text
make assemble-debug
make install-debug
make device-smoke
```

Expected:

- Yeyecatl launches without crashing.
- No permission request appears automatically.
- Readiness UI is visible.
- Scan state is coherent before any user-triggered scan.

## Permission Flow

1. Start with location permission not granted where practical.
2. Launch Yeyecatl.
3. Confirm no permission dialog appears automatically.
4. Tap the explicit permission action.
5. Confirm Android permission UI appears.
6. Test deny and grant flows.
7. Confirm Yeyecatl readiness updates after the result.
8. Where practical, test "do not ask again" / settings-required behavior.

## Wi-Fi Disabled

1. Disable Wi-Fi in Android settings.
2. Return to Yeyecatl.
3. Confirm the app remains running.
4. Confirm Wi-Fi disabled readiness is shown.
5. Confirm scan action is prevented or rejected coherently.
6. Re-enable Wi-Fi and confirm readiness recovers.

## Location Services

1. Disable Location Services.
2. Confirm Yeyecatl reports Location Services unavailable where Android
   requires it.
3. Re-enable Location Services.
4. Confirm readiness recovers without app restart where practical.

## Real Scan

1. Grant required permission.
2. Enable Wi-Fi and Location Services.
3. Tap `Scan Wi-Fi`.
4. Observe `app-logcat` for:
   - scan request attempted;
   - accepted or rejected request;
   - scan-result broadcast;
   - `EXTRA_RESULTS_UPDATED` state;
   - mapped observation count;
   - observed bands and widths.
5. Confirm UI shows observations or an explicit empty/cached/rejected state.

Do not assume visible observations are fresh. Use freshness state and
`EXTRA_RESULTS_UPDATED`.

## Cached Or Rejected Scan Behavior

Manually tap scan multiple times close together without trying to bypass Android
limits.

Expected:

- no crash;
- no automatic retry loop;
- previous observations remain visible if Android returns cached data;
- UI does not relabel cached data as fresh;
- request rejection remains factual and does not claim definite throttling.

## RSSI And Frequency Checks

For several observed access points, confirm:

- RSSI remains in dBm;
- no percentage/bar/quality label is shown;
- stronger RSSI appears higher on the chart;
- raw frequency appears in diagnostics;
- RF band/channel are plausible for the frequency.

If unexpected frequencies appear, sanitize the data, identify the owning layer,
and add a regression test before changing behavior.

## Channel Width And Center Frequencies

Validate only widths present in the local RF environment. Absence of 40, 80,
160, 80+80, 320 MHz, or 6 GHz observations is not a failure.

Preview and JVM fixtures remain the deterministic coverage for uncommon widths.

## Spectrum Chart

Check:

- 2.4 GHz chart when observations are present;
- 5 GHz chart when observations are present;
- 6 GHz chart when observations are present;
- empty band messages;
- band selector operation;
- axes and labels readability;
- duplicate SSIDs remain separate in diagnostics;
- refresh updates the chart when scan results change.

The chart shows nominal geometry, not interference or channel quality.

## Screen And Theme

Where practical:

- test portrait orientation;
- rotate to landscape;
- inspect light and dark modes;
- confirm content remains scrollable;
- confirm labels remain legible.

## Validation Checklist

```text
[ ] adb recognizes device
[ ] debug build installs
[ ] application launches
[ ] no automatic permission popup
[ ] explicit permission request works
[ ] denied state works
[ ] granted state works
[ ] Wi-Fi disabled state works
[ ] Location Services state works
[ ] scan request works
[ ] scan results appear or empty state is explicit
[ ] RSSI values appear in dBm
[ ] RF channels are plausible
[ ] spectrum footprints are plausible
[ ] 2.4 GHz chart works
[ ] 5 GHz chart checked if available
[ ] 6 GHz chart checked if available
[ ] cached/throttled behavior checked
[ ] portrait checked
[ ] landscape checked where practical
[ ] light/dark theme checked
[ ] no crashes observed
```

## Evidence Template

Use sanitized summaries:

```text
Device:
Android:
API:

Observed AP count:
Observed bands:
Observed channel widths:

Scan request:
Fresh results:
Cached behavior:
Permission flow:
Wi-Fi disabled recovery:
Location disabled recovery:

2.4 GHz chart:
5 GHz chart:
6 GHz chart:
```

## Phase 1G Current Status

Implementation validation can pass without hardware. Physical-device validation
is complete only after this checklist is run on an attached Android device.
