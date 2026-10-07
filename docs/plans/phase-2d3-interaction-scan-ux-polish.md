# Phase 2D.3 — Interaction & Scan UX Polish

## Goal

Polish the nearby-network interaction model and make scan activity immediately
visible and understandable without changing the underlying RF-domain semantics.

## Card interaction cleanup

- remove the More / Less button from nearby-network cards;
- remove per-card expanded state;
- make the whole card body the only BSSID-selection target;
- remove capabilities from user-facing cards;
- preserve SSID, BSSID, RSSI, band, channel, channel width and Wi-Fi standard;
- move useful same-SSID / multiple-BSSID context into the focused inspector;
- keep non-selectable observations visually consistent when no usable BSSID is
  available.

## Card visual polish

- increase card/background separation;
- improve border/elevation and vertical spacing;
- strengthen typography hierarchy;
- make RSSI a stronger visual anchor;
- make selected state substantially easier to recognize;
- preserve Yeyecatl's dark/cyan visual language without turning every card into
  a saturated accent surface.

## Responsive controls

- prevent Signal weakest and other sort/filter labels from clipping on compact
  displays;
- prefer responsive/wrapping layout over hard-coded button widths;
- validate on Galaxy J8 and Moto G41.

## Automatic initial scan

- when the app opens and platform readiness permits discovery, issue one static
  scan automatically;
- issue it exactly once per app launch/session, never once per recomposition;
- keep manual Scan Wi-Fi available afterward;
- do not implicitly enable Dynamic Scan.

## Dynamic-scan progress

- represent the current 30-second foreground cadence with a progress indicator;
- expose a next-scan countdown such as `Next scan in 18 s`;
- reset the progress cleanly after each scheduled request;
- stop/reset the timing UI when Dynamic Scan stops;
- keep the timing presentation separate from Android scan acceptance/result
  freshness.

## Dynamic-scan counter

- expose a counter for actual dynamic scan requests issued during the current
  dynamic session;
- reset the counter when a new dynamic session starts;
- do not increment merely because a UI animation cycle elapsed;
- preserve the distinction between requested, rejected/throttled and fresh
  result updates.

## Validation

Run repository checks and validate on both lab devices:

```text
make device-smoke-j8
make device-smoke-g41
```

Acceptance requires:

- card body selection works without a secondary More interaction;
- selection remains stable while band/scope/list controls change;
- cards remain readable on both screen sizes;
- initial scan occurs once after launch when allowed;
- dynamic progress and request counter visibly advance during a live session;
- stopping and restarting Dynamic Scan resets session timing/counter state
  predictably.
