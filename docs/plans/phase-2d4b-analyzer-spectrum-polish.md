# Phase 2D.4b — Analyzer Spectrum Readability Polish

## Goal

Refine Yeyecatl's spectrum view toward the readability of conventional Wi-Fi
analyzer apps while preserving Yeyecatl's RF-domain accuracy and cross-view
BSSID selection model.

## Reference behavior

Use classic analyzer-style spectrum presentation as a readability benchmark:

- strong channel grid;
- clear channel anchors;
- readable AP envelopes;
- labels that remain understandable in crowded channel groups;
- useful RSSI vertical range;
- selected BSSID remains visually dominant.

Do not copy another app's visual design literally.

## Scope

- extend the displayed RSSI floor to approximately -100 dBm;
- strengthen grid/axis readability without overpowering AP envelopes;
- improve label placement and reduce label collisions;
- give selected BSSID label/envelope highest visual priority;
- preserve current footprint geometry for 20/40/80/160/80+80/320 MHz;
- preserve the current animated transition between accepted snapshots;
- do not invent interpolated RF samples;
- retain Yeyecatl dark/cyan visual identity.

## Label collision strategy

Prefer deterministic placement over random displacement.

Priority order:

1. selected BSSID;
2. strongest visible APs;
3. remaining APs.

When labels collide heavily:

- offset labels vertically/horizontally where possible;
- de-emphasize or omit low-priority labels rather than drawing unreadable text;
- never hide the selected BSSID label.

## 2.4 GHz focus

Crowded 2.4 GHz channel groups are the primary acceptance target because they
produce the worst label collisions and are easy to validate on both J8 and G41.

## Acceptance

- channel 1 / 6 / 11 clusters remain visually distinguishable;
- labels no longer pile into an unreadable block;
- selected BSSID remains obvious;
- spectrum remains fluid when fresh snapshots arrive;
- RSSI range includes weak APs down to roughly -100 dBm;
- no changes to RF interpretation, occupancy math, history semantics or polling
  cadence behavior.

## Device validation

```text
make device-smoke-j8
make device-smoke-g41
```

Validate first with All, then Strongest 5, then a selected BSSID.
