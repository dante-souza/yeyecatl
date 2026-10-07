# Yeyecatl Phase 2D.3 — Interaction & Scan UX Polish

## Milestone

Phase 2D.3 freezes the interaction and scan-UX polish that followed the
cross-view BSSID selection work.

Physical validation on the Moto G41 accepted the redesigned nearby-network
cards, whole-card selection model, automatic first scan behavior, dynamic-scan
progress/countdown and request counter. The device-specific Make helpers were
also validated on Windows with both lab phones visible through ADB.

## Validation date

2026-10-07

## Accepted behavior

- one automatic static scan is requested after app launch when discovery is
  allowed;
- that initial request is single-shot across normal activity recreation;
- manual Scan Wi-Fi remains available;
- Dynamic Scan remains opt-in;
- Dynamic Scan shows a visible progress bar and next-request countdown;
- the dynamic counter records requests issued by Yeyecatl, not guaranteed fresh
  RF observations;
- Android rejection/throttling and snapshot freshness remain separate concepts;
- nearby-network cards use the entire card body as the BSSID selection target;
- More / Less expansion is removed;
- capabilities are no longer shown in the end-user card/detail presentation;
- SSID, BSSID, RSSI, band, channel, width and Wi-Fi standard remain prominent;
- RSSI is promoted to a visual badge;
- selected cards receive stronger border/surface emphasis;
- same-SSID / multiple-BSSID context is moved to the focused inspector;
- filter and sort chips wrap instead of clipping on compact screens;
- the cross-view BSSID focus from Phase 2D.2 remains stable while changing
  band, signal scope, filtering and sorting.

## Device helpers

Validated workflow:

```text
make device-info-j8
make device-smoke-j8

make device-info-g41
make device-smoke-g41
```

The helpers discover devices from `adb devices -l` model tokens and preserve
an explicit serial override when needed.

## Frozen feature head

```text
a37b526352ad4df3513d5c63b1470d3dbba43430
fix: preserve awk fields in Make device helpers
```

## CI

Final feature-head validation run:

```text
37687355225 — PASS
```

The Android build, JVM tests, instrumentation APK build, lint, agent checks and
portable device-helper regression guard all passed on the frozen feature head.

## Next and final Phase 2D block

Phase 2D.4 is reserved for configurable polling cadence and final Dynamic Scan
behavior.
