# Yeyecatl v0.1.0-phase2d5.3-connected-rssi.1

## Complete functional analyzer baseline

This release freezes the accepted Phase 2D.5.3 Yeyecatl Android field-analyzer baseline.

### Release identity

- Release tag: `v0.1.0-phase2d5.3-connected-rssi.1`
- Frozen source commit: `28f60adc6a067555416db4e992040dffcfd7845f`
- Validated code commit: `95834a808075f0b2a6699d0154baff2d0c237896`
- Feature CI: `37760664643` — PASS
- Validation device: Samsung Galaxy J8 / SM-J810M / Android 10
- Application id: `io.github.dante_souza.yeyecatl`

## Accepted product scope

Yeyecatl is now a complete mobile Wi-Fi observation and field-diagnostics baseline within its chosen scope.

Included capabilities:

- Android Wi-Fi discovery with explicit readiness and permission handling;
- SSID/BSSID, RSSI, band, channel, channel width and platform capability inspection;
- 2.4 / 5 / 6 GHz RF interpretation where exposed by the device;
- spectrum geometry and analyzer-style channel visualization;
- All / Strongest 5 / Weakest 5 spectrum filtering;
- selected-BSSID cross-view highlighting and access-point detail;
- channel occupancy and geometric-overlap overview;
- foreground dynamic scanning with explicit request cadence;
- throttling-aware polling policy:
  - 5 s / 10 s Lab modes;
  - 30 s Default;
  - 45 s / 60 s / 120 s standard choices;
- truthful separation of scan requests from fresh Android snapshots;
- rolling nearby-network signal history with measured / held / missing semantics;
- dense-history readability hierarchy;
- independent connected-AP RSSI reads via Android `WifiInfo`, without requesting nearby scans;
- rolling connected-AP RSSI chart;
- disconnect/reconnect session breaks;
- Fixed RSSI scale for repeatable field comparison;
- Auto RSSI scale using every sample visible in the two-minute window with quantization and hysteresis.

## Measurement semantics

Yeyecatl does not fabricate Wi-Fi observations to make graphs look active.

Nearby-network history advances from fresh Android scan results. Presentation-only held segments are explicitly differentiated from measured samples.

Connected-AP RSSI is a separate Android link-information stream. Repeated one-second `WifiInfo` values are reads of Android's current link state and are not claimed to be independently refreshed radio measurements.

## Physical validation

The Phase 2D.5.x connected-link work was exercised on the Galaxy J8 while moving around a household test environment.

Observed behavior included:

- connected-link reads advancing independently from nearby dynamic scans;
- RSSI movement from weak regions around the -60/-70 dBm range toward very strong values near -20 dBm;
- disconnect/reconnect represented as separate chart segments;
- Auto scaling adapting to the visible signal range without intentionally hiding weak visible samples;
- Fixed mode retaining a stable comparison scale.

Physical BSSID-roaming validation remains pending an environment where roaming between AP BSSIDs can be triggered conveniently. Automated tests cover BSSID/session history separation.

## Deliberately out of scope

This release intentionally does not turn Yeyecatl into a general data-analysis workstation.

Deferred or separate-tool territory includes:

- sideways historical browsing/panning;
- arbitrary timeline zoom;
- long-term persisted scan/session databases;
- chart PNG/report export;
- CSV/session export;
- annotations and room labels;
- cross-session statistical comparison;
- heatmaps and spatial survey workflows.

The product boundary is deliberate: **Yeyecatl observes and diagnoses Wi-Fi in the field.**

## Release artifacts

The release workflow builds the frozen debug APK, runs the repository checks against the frozen source, and publishes:

- `yeyecatl-v0.1.0-phase2d5.3-connected-rssi.1-debug.apk`
- `phase-2d5-d53-validation-record.md`
- `SHA256SUMS.txt`

