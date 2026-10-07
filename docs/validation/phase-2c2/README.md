# Yeyecatl Phase 2C.2 — Focused AP Temporal Detail Physical-Device Evidence

## Milestone

Phase 2C.2 freezes the first accepted exact-BSSID temporal inspection surface on
physical Android hardware.

The phase extends Phase 2C.1 selection/detail with a dedicated focused RSSI
timeline, bounded retained-signal summary, adaptive single-AP chart scaling,
sticky focus across transient scan misses, stable focused-inspector placement,
and user-triggered navigation to the inspector.

It does not add persistence, lifetime first-seen semantics, scoring,
recommendations, trend prediction, distance estimation, vendor lookup, or
SSID-level identity.

## Validation date

2026-10-07

## Validation device

| Property | Value |
|---|---|
| Device | Samsung Galaxy J8 |
| Model | SM-J810M |
| Device codename | j8y18lte |
| Android | 10 |
| Android API level | 29 |
| Application ID | `io.github.dante_souza.yeyecatl` |

## Accepted Phase 2C.2 behavior

The physical-device acceptance session demonstrates:

- focused temporal identity remains anchored to one exact BSSID;
- retained latest, strongest and weakest RSSI values are internally consistent;
- retained RSSI range is computed from the bounded in-memory sample set;
- the focused chart shows up to 60 newest retained samples;
- strong signals above -30 dBm are no longer clipped by the global comparison
  viewport;
- focused vertical bounds round outward to 10 dB grid lines;
- a minimum 20 dB focused span keeps nearly-flat weak-signal histories readable;
- global multi-BSSID history keeps its separate common comparison scale;
- dynamic scanning continues to append approximately at the configured
  30-second foreground cadence when Android accepts fresh scans;
- a focused BSSID can remain selected across transient scan misses;
- stale focus is represented truthfully through Seen / Not seen and Last seen
  state rather than by pretending that the AP remains current;
- the focused inspector has one stable structural slot instead of moving between
  inline and fallback positions;
- selecting an AP deep in the nearby-network list automatically navigates to
  the focused inspector once;
- later background scan updates do not intentionally re-scroll the viewport;
- `make open-app` launches the already-installed application without rebuilding.

## Device-validation corrections before freeze

The Phase 2C.2 device session intentionally produced several corrective commits
before the freeze:

1. **Focused viewport clipping** — an AP with retained values as strong as
   approximately -22 dBm exposed the old -30 dBm ceiling. The focused chart now
   derives its own adaptive viewport.
2. **Near-flat weak histories** — weak observations around -75 to -77 dBm
   validated the minimum 20 dB display span.
3. **Transient scan misses** — real Android scans showed that a selected BSSID
   can disappear from one snapshot. Focus became a sticky in-memory session with
   truthful last-known state.
4. **Scan-driven layout movement** — the inspector previously changed physical
   position depending on latest-scan membership. It now occupies one permanent
   structural slot.
5. **Usability from deep lists** — keeping a stable slot made manual scrolling
   back to the inspector inconvenient. User-triggered selection now brings that
   slot into view once, while background updates remain passive.

These corrections are part of the accepted milestone rather than post-release
changes.

## Frozen implementation

Final physically accepted Phase 2C.2 feature head:

```text
e72b5b0d3f6a91fee057e46bce9ff9b6152e339b
feat: scroll selected AP inspector into view
```

Feature-branch GitHub Actions validation:

```text
37641913450 — PASS
```

Merged `dev` GitHub Actions validation:

```text
37642981387 — PASS
```

Two-parent integration commit:

```text
e808e8cedb10b1afac18fba7f5e2a85c60c66503
merge: integrate Phase 2C.2 focused AP temporal detail
```

The integration commit uses the previous `dev` head as first parent and the
frozen Phase 2C.2 feature head as second parent. Individual implementation
commits and branch topology are preserved.

## Original physical-device evidence

All photographs below are original JPEG files supplied during the J8 acceptance
session. The archive policy is byte-for-byte preservation: no rotation,
cropping, resizing, recompression or format conversion.

### Weak-signal adaptive history

`yeyecatl-phase-2c2-j8-weak-adaptive-history.jpg`

| Property | Value |
|---|---:|
| Resolution | 1152×1536 |
| Bytes | 229,758 |
| Format | JPEG |
| SHA-256 | `9bb43a3bf18c5e0cff95488e874c17a293e90e25f5207056b4d9a6e80825bd54` |

Shows a weak/stable selected AP around -75 to -77 dBm with an adaptive focused
viewport around -70 to -90 dBm, demonstrating readable small variation.

### Strong-signal adaptive history

`yeyecatl-phase-2c2-j8-strong-adaptive-history.jpg`

| Property | Value |
|---|---:|
| Resolution | 1152×1536 |
| Bytes | 178,002 |
| Format | JPEG |
| SHA-256 | `1cd651d9ed42a4683125e32ccaad0e8932e0b4c3ce3e42515f7cbb17f4c51a0e` |

Shows `Te-Moana-nui-a-Kiwa` with retained RSSI reaching approximately
-21/-22 dBm and an adaptive -20 to -40 dBm chart, preserving variation that was
previously clipped.

### Sticky last-known focused selection

`yeyecatl-phase-2c2-j8-sticky-last-known.jpg`

| Property | Value |
|---|---:|
| Resolution | 1536×1152 |
| Bytes | 230,647 |
| Format | JPEG |
| SHA-256 | `6107beb570f19461dd8f7d6710b516ab23ec4304fab444e79eb5f2e2a8e88eda` |

Preserves a selected AP that is absent from the latest scan while its last-known
detail remains inspectable, demonstrating the sticky focused-session behavior.

### Deep-list selection source

`yeyecatl-phase-2c2-j8-selection-source-deep-list.jpg`

| Property | Value |
|---|---:|
| Resolution | 1152×1536 |
| Bytes | 204,430 |
| Format | JPEG |
| SHA-256 | `0a59535c904cb9cfa2e5a04b5c4d7f2a75dbf7963bff41984ae1e0e33cd53bb7` |

Shows a selected BSSID deep inside a large nearby-network list, the usability
case that motivated explicit navigation to the stable inspector.

### Selection auto-navigation destination

`yeyecatl-phase-2c2-j8-selection-autoscroll.jpg`

| Property | Value |
|---|---:|
| Resolution | 1152×1536 |
| Bytes | 186,016 |
| Format | JPEG |
| SHA-256 | `03443323b3b74f8fb0ddaab2c1c96c83b7d87eca44ff9888c1b9546f76394e2b` |

Shows the stable `Focused selection` destination after selection, with
`Latest scan: Seen`, `Last seen: now` and the selected access-point detail
visible without manually scrolling back through the page.

## Archaeological refs

Implementation branch:

```text
feature/phase-2c2-focused-ap-temporal-detail
```

Evidence branch:

```text
docs/phase-2c2-j8-evidence
```

Release freeze:

```text
e72b5b0d3f6a91fee057e46bce9ff9b6152e339b
```

Planned archaeological prerelease:

```text
v0.1.0-phase2c2-focused-ap-temporal.1
```

## Status

**DEVICE ACCEPTANCE: PASS**

**FEATURE CI: PASS**

**IMPLEMENTATION INTEGRATION: COMPLETE**

**MERGED DEV CI: PASS**

**IMPLEMENTATION FREEZE: COMPLETE**

**ARCHAEOLOGY TEXT/WORKFLOW: PREPARED**

The evidence branch is the preservation line for the original J8 photographs,
checksum manifest and archaeological prerelease automation.
