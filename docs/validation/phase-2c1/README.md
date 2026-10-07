# Yeyecatl Phase 2C.1 — Selection + Detail Physical-Device Evidence

## Milestone

Phase 2C.1 freezes the first accepted BSSID-anchored access-point selection and
detail model on physical Android hardware.

The phase adds one focused access-point detail surface downstream of the existing
normalized scan, RF interpretation, spectrum geometry and bounded temporal
history contracts. It does not add persistence, vendor lookup, scoring,
recommendations, SSID-level identity, or inferred RF precision.

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

## Accepted Phase 2C.1 behavior

The supplied physical-device evidence demonstrates:

- selection is anchored to an exact BSSID rather than SSID;
- the selected nearby-network card receives a visible selected state;
- a dedicated `Selected access point` panel is rendered for the chosen BSSID;
- current RSSI, band, channel, primary frequency and channel width are exposed;
- center-frequency and Wi-Fi-standard values remain explicitly unavailable or
  unknown when Android does not report them;
- nominal spectrum span and geometry completeness are derived from the existing
  RF/spectrum domain model;
- the Android capability string remains available without reinterpretation;
- bounded Phase 2A temporal context is exposed as retained RSSI sample count and
  retained history span;
- the existing spectrum and signal-history views remain operational;
- the prior Phase 2B.1 `More / Less` diagnostic expansion remains an independent
  interaction.

One accepted detail capture shows BSSID `04:d9:f5:73:7a:38` selected for
`Te-Moana-nui-a-Kiwa`, with RSSI `-21 dBm`, 2.4 GHz channel 7,
2442 MHz primary frequency, 20 MHz width, 21 retained RSSI samples and a retained
history span of approximately 532.751 seconds.

The term **retained** is intentional. Phase 2A history is bounded, so this
milestone does not claim lifetime first-seen semantics.

## Integration record

Final Phase 2C.1 feature head:

```text
0ee8f76a3f0cb048dca24d6d354e0d6ea75e3340
```

Feature-branch GitHub Actions validation:

```text
37603910672 — PASS
```

Integration into `dev`:

```text
d1510fd648ef30a93103213c8b35f710786a1ce8
merge: integrate Phase 2C.1 selection detail model
```

Merged `dev` GitHub Actions validation:

```text
37607367544 — PASS
```

The integration is a normal two-parent merge commit. Individual implementation
commits and branch topology are preserved.

## Original physical-device evidence

The evidence set consists of two original JPEG photographs supplied during the
physical-device acceptance session. They must be committed byte-for-byte without
rotation, cropping, resizing, recompression or format conversion.

### Spectrum + signal-history continuity

```text
yeyecatl-phase-2c1-j8-spectrum-history.jpg
```

| Property | Value |
|---|---:|
| Resolution | 1536×1152 |
| Bytes | 187,805 |
| Format | JPEG |
| SHA-256 | `63d884838133dbe6660bdc33fdb6c9c9116c51fc42cfc71f619ca19f3ac92855` |

This photograph preserves evidence that the existing selected-band spectrum and
signal-history surfaces still render after Phase 2C.1.

### BSSID selection + detail panel

```text
yeyecatl-phase-2c1-j8-selection-detail.jpg
```

| Property | Value |
|---|---:|
| Resolution | 1536×1152 |
| Bytes | 248,202 |
| Format | JPEG |
| SHA-256 | `e76f08575eb4e5b27f2860df3e9bda690224aec157571969a87342e5163258f1` |

This photograph preserves the selected nearby-network card and the expanded
access-point detail model on the physical J8.

## Archaeological refs

Implementation branch:

```text
feature/phase-2c1-selection-detail-model
```

Evidence branch:

```text
docs/phase-2c1-j8-evidence
```

Release freeze:

```text
0ee8f76a3f0cb048dca24d6d354e0d6ea75e3340
```

Planned archaeological prerelease:

```text
v0.1.0-phase2c1-selection-detail.1
```

## Archive and release record

Original evidence commit:

```text
10b9caafc8249ae6af71125ae82fdc6cae9ee0bf
docs: archive original Phase 2C.1 J8 evidence
```

Archive workflow:

```text
37608554767 — PASS
```

Published archaeological prerelease:

```text
v0.1.0-phase2c1-selection-detail.1
Phase 2C.1 Selection + Detail Freeze — Galaxy J8
GitHub release ID: 405651356
Target: 0ee8f76a3f0cb048dca24d6d354e0d6ea75e3340
```

Release assets:

- `yeyecatl-v0.1.0-phase2c1-selection-detail.1-debug.apk` — 29,646,096 bytes
- `yeyecatl-phase-2c1-j8-spectrum-history.jpg` — 187,805 bytes
- `yeyecatl-phase-2c1-j8-selection-detail.jpg` — 248,202 bytes
- `phase-2c1-validation-record.md` — validation record
- `SHA256SUMS.txt` — release-asset checksum manifest

The archive workflow independently verified the two committed original evidence
JPEGs against `SHA256SUMS-images.txt` before building or publishing the release.

## Status

**DEVICE ACCEPTANCE: PASS**

**IMPLEMENTATION INTEGRATION: COMPLETE**

**ARCHAEOLOGY IMAGE ARCHIVE: COMPLETE**

**ARCHAEOLOGICAL PRERELEASE: PUBLISHED**

The evidence branch is now a frozen preservation ref. It can be merged into
`dev` with a normal merge commit so the validation record becomes part of the
main development history without flattening or rewriting the archaeology.
