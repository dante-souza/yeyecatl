# Yeyecatl Phase 2D.2 — Cross-view BSSID Highlighting

## Milestone

Phase 2D.2 freezes the first accepted BSSID-focused interaction model across
Yeyecatl's analytical views.

A nearby-network card selects one BSSID as the current focus. That selection is
owned by the UI state and is propagated to the spectrum, temporal signal
history, channel occupancy/overlap context, nearby-network list and focused
detail inspector. No RF-domain semantics were changed by this phase.

## Validation date

2026-10-07

## Physical validation devices

| Device | Model | Android | Role in Phase 2D.2 |
|---|---|---:|---|
| Samsung Galaxy J8 | SM-J810M | 10 / API 29 | 2.4 GHz selection, persistence and compact-device behavior |
| Motorola Moto G41 | moto_g41 / corfu | 12 / API 31 | dual-band validation including live 5 GHz observations |

## Accepted behavior

Physical-device validation accepted the following behavior:

- selecting a nearby-network card establishes an exact BSSID focus;
- the selected card remains visibly selected;
- the focused detail inspector remains present while the user changes band,
  Strongest/Weakest scope and list controls;
- selection is not discarded simply because a view/filter changes;
- spectrum rendering draws the selected BSSID with stronger visual emphasis and
  de-emphasizes non-selected observations;
- temporal history emphasizes the selected BSSID and identifies it in the
  legend when a retained series is available;
- channel occupancy exposes selected-BSSID context without changing the
  underlying occupancy or overlap calculations;
- selecting a supported-band BSSID synchronizes the spectrum band and resets
  the spectrum scope to All so that the selected AP cannot be hidden by a
  Strongest 5 / Weakest 5 scope;
- the existing focused-detail model remains the authoritative place for
  per-BSSID detail.

The Moto G41 validation also confirmed live 5 GHz observations and list
filtering on real hardware. Example observed 5 GHz data included an 80 MHz
802.11ac BSSID on channel 157.

## Selection semantics

The selection key is the BSSID, not SSID text. This preserves distinct access
points that advertise the same SSID and keeps cross-view identity stable.

Selection remains a UI concern. The Phase 2D.2 work does not add selection
state to the RF/domain model and does not alter scan results, RF interpretation,
spectrum geometry, occupancy counts or overlap math.

## Device helper additions

The repository now has explicit serial-bound helpers for the two validation
devices:

```text
make device-info-j8
make install-debug-j8
make open-app-j8
make device-smoke-j8

make device-info-g41
make install-debug-g41
make open-app-g41
make device-smoke-g41
```

The device-specific install targets assemble the debug APK and use
`adb -s <serial> install --no-streaming -r`, which avoids ambiguity when both
phones are attached.

## Phase 2D.3 carry-over

Phase 2D.3 is intentionally a UX/interaction polish block. The accepted backlog
from physical testing is:

- remove the obsolete More / Less interaction from nearby-network cards;
- make the entire card body the only BSSID-selection target;
- remove capabilities from the user-facing card/detail presentation while
  retaining raw platform data internally where useful;
- improve card contrast, spacing, border/elevation, typography and selected
  state visibility;
- keep SSID, BSSID, RSSI, band, channel, width and Wi-Fi standard prominent;
- move useful same-SSID/multiple-BSSID context to the focused inspector;
- make filter/sort controls responsive so labels such as Signal weakest are not
  clipped on narrow screens;
- perform one automatic static scan when the app opens and platform readiness
  allows it, exactly once per launch/session rather than per recomposition;
- add a visible 30-second dynamic-scan progress indicator / next-scan countdown;
- add a dynamic-session scan-request counter, counting actual scan requests and
  preserving the distinction between requested, rejected/throttled and fresh
  result updates.

## Frozen feature head

```text
00f2240a33cfa02837bb7f8f118cbe104d42c96c
build: add explicit J8 and G41 device helpers
```

Final feature-head GitHub Actions validation:

```text
37679929306 — PASS
```
