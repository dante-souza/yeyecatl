# Yeyecatl Phase 2B.1 — Nearby Network List Physical-Device Evidence

## Milestone

Phase 2B.1 freezes the first accepted compact/expandable presentation for the
Nearby Networks list on physical Android hardware.

The phase refines presentation only. It does not redefine scan acquisition,
filtering/sorting semantics, RF interpretation, signal ranking, or Phase 2A
temporal history.

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
| Application ID | `io.github.dante_souza.yeyecatl` |

## Accepted Phase 2B.1 state

The supplied J8 screenshot demonstrates the accepted nearby-network card state:

- multiple real nearby observations rendered as individual cards;
- SSID as the primary identity;
- BSSID preserved as secondary identity;
- RSSI retained in the accepted right-aligned accent treatment;
- compact RF summary limited to band, channel and channel width;
- `More` available on collapsed cards;
- one card expanded in place with `Less`;
- raw Android capability information visible in the expanded card;
- surrounding cards remaining compact while one card is expanded.

The accepted layout is intentionally not a final Network Detail view. Additional
per-network information may be introduced later without reopening this frozen
summary-card milestone.

## Integration record

Final Phase 2B.1 feature head before integration:

```text
f183a6a84d10e2b7fc2e15366001bb03c0aeded9
```

GitHub Actions validation:

```text
37557154168 — PASS
```

Integration into `dev`:

```text
476db8522a76e55ae4b165b2efc2439351d6f8cf
merge: integrate Phase 2B filtering and nearby-network polish
```

The integration used a normal merge commit. Individual Phase 2B implementation
commits and branch topology are preserved.

## Original screenshot evidence

The original device screenshot is committed byte-for-byte under:

```text
yeyecatl-phase-2b1-j8-nearby-networks-expandable-card.png
```

Do not resize, crop, recompress, convert, or otherwise rewrite the image before
commit.

| Property | Value |
|---|---:|
| Resolution | 900×1600 |
| Bytes | 1,295,528 |
| Format | PNG |
| SHA-256 | `b1509e9436f7e663acb70d9203c63f5924b5d63c63c149e82322b97069a8169f` |

This metadata was calculated from the original screenshot supplied during the
physical-device acceptance session. The repository blob was verified against the
preserved original and matches exactly.

Git blob SHA-1:

```text
2b811d4bfd2d148281593ce76cbc38bc024029a9
```

## Evidence branch status

The metadata, checksum manifest and original PNG are all present on
`docs/phase-2b1-j8-evidence`.

The PNG was re-verified from the repository working tree with the exact SHA-256
recorded above. The repository Git blob also matches the preserved original
byte-for-byte.

## Status

**DEVICE ACCEPTANCE: PASS**

**ARCHAEOLOGY IMAGE ARCHIVE: COMPLETE**

This record is complete and can be merged into `dev` with a normal merge commit.
After integration, the evidence branch remains frozen as an archaeological ref.
