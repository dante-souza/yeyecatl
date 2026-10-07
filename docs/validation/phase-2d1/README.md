# Yeyecatl Phase 2D.1 — Channel Occupancy + Overlap Overview Physical-Device Evidence

## Milestone

Phase 2D.1 freezes the first accepted analyzer-focused channel occupancy and
geometric RF-overlap overview on physical Android hardware.

The phase builds entirely on the existing latest-scan observation model, RF
interpretation and Phase 1E spectrum geometry. It does not introduce new scan
semantics or infer platform data that Android does not expose.

In this milestone:

- **occupancy** means the count of observed access points whose primary channel
  maps to a channel in the selected band;
- **overlap** means a positive mathematical intersection between two nominal RF
  footprints derived by the existing spectrum-geometry model;
- overlap pairs remain BSSID-distinct even when SSIDs are duplicated;
- overlap summaries use all observations in the selected band rather than the
  `All / Strongest 5 / Weakest 5` spectrum display scope;
- the analyzer exposes total overlap-pair count plus average overlapping
  neighbors per observed AP;
- partial geometry is disclosed instead of silently presented as complete;
- no airtime utilization, interference severity, channel-quality score or
  channel recommendation is claimed.

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

## Accepted Phase 2D.1 behavior

Physical-device validation demonstrates:

- the selected 2.4 / 5 / 6 GHz band drives the occupancy/overlap overview;
- access-point counts are derived from the complete latest scan for that band;
- primary channels are grouped deterministically and ranked by AP count;
- strongest RSSI on each displayed primary channel is shown for context;
- pairwise overlap is derived from existing nominal RF footprints;
- the raw number of positive overlap pairs is preserved;
- average overlap neighbors/AP is computed as
  `2 × overlapping_pair_count / observed_AP_count`;
- overlap endpoints are disambiguated using short BSSID suffixes;
- ASCII-safe `vs` is used instead of the unsupported arrow glyph observed on
  the Galaxy J8;
- the largest overlap entries place the endpoint identity and overlap bandwidth
  on separate visual lines for readability;
- duplicate SSIDs remain separate access points;
- the card explicitly distinguishes observed-AP occupancy from actual airtime
  or channel-utilization measurement;
- Strongest/Weakest spectrum filtering does not alter analyzer facts;
- existing focused AP selection, temporal history and scan behavior remain
  unchanged.

## Physical observations

The first functional device capture showed:

| Metric | Value |
|---|---:|
| 2.4 GHz access points | 68 |
| Observed primary channels | 10 |
| Positive overlapping AP pairs | 1070 |
| Most occupied primary channel | ch 1 — 19 APs |

This run proved the analyzer calculations were live on hardware, while also
exposing two presentation defects: the overlap arrow rendered as a missing-glyph
square and repeated SSIDs were visually ambiguous.

After the J8-specific polish commit, the accepted scan showed:

| Metric | Value |
|---|---:|
| 2.4 GHz access points | 72 |
| Observed primary channels | 10 |
| Positive overlapping AP pairs | 1151 |
| Average overlap neighbors/AP | 32.0 |
| Most occupied primary channel | ch 1 — 21 APs, strongest -58 dBm |
| Second most occupied | ch 11 — 13 APs, strongest -64 dBm |
| ch 6 | 7 APs, strongest -62 dBm |
| ch 10 | 7 APs, strongest -63 dBm |
| ch 5 | 6 APs, strongest -63 dBm |
| ch 2 | 5 APs, strongest -68 dBm |

The displayed 32.0 figure is internally consistent with the accepted snapshot:
`2 × 1151 / 72 ≈ 31.97`, rounded to one decimal place.

## Device-validation correction before freeze

The first physical rendering was functionally correct but not ready for
archaeological freeze.

The J8 exposed:

1. **unsupported glyph rendering** — the `↔` separator appeared as a square;
2. **duplicate-SSID ambiguity** — entries such as two Deco BSSIDs appeared to be
   the same endpoint;
3. **high pair-count readability** — the raw overlap-pair total needed a more
   human-scale companion summary.

The final polish therefore:

- replaces `↔` with `vs`;
- appends the final two BSSID octets to each overlap endpoint;
- adds average overlap neighbors/AP;
- shortens section headings;
- separates overlap identity and bandwidth into two rows;
- tightens the explanatory text while preserving the no-scoring boundary.

No RF calculation was changed by this visual polish except for adding the
derived average-neighbor summary from the already-computed pair count.

## Frozen implementation

Final physically accepted Phase 2D.1 feature head:

```text
1136bb2dd861844cb16e46ac490ab25ec01a56a2
fix: polish channel occupancy overlap card on J8
```

Feature-branch GitHub Actions validation:

```text
37657767151 — PASS
```

Two-parent implementation integration commit:

```text
ac01e30eb17602dfb2923733048911813d353dc5
merge: integrate Phase 2D.1 channel occupancy overlap
```

The integration commit uses the previous `dev` head as first parent and the
frozen Phase 2D.1 feature head as second parent, preserving implementation
history and branch topology.

Merged `dev` GitHub Actions validation:

```text
37667218512 — PASS
```

## Original physical-device evidence

The original validation photographs are identified byte-for-byte in:

```text
docs/validation/phase-2d1/ORIGINAL-IMAGE-MANIFEST.md
```

That manifest records original upload identity, dimensions, byte count and
SHA-256 for:

- the first functional pre-polish card;
- the final accepted landscape overview;
- the final accepted portrait overlap-detail view.

The repository automation run available here cannot ingest conversation-image
bytes into Git directly, so no transformed copy is substituted for those
originals. The archaeological identity of each original is preserved by its
SHA-256 digest.

## Scope boundary

Phase 2D.1 remains observational.

It does **not** claim or produce:

- channel airtime utilization;
- CCA busy percentage;
- packet/traffic load;
- interference severity;
- a congestion score;
- a quality grade;
- distance estimation;
- a "best channel";
- router configuration recommendations.

Those require additional measurements or explicitly designed later models.

## Archaeological refs

Implementation branch:

```text
feature/phase-2d1-channel-occupancy-overlap
```

Evidence branch:

```text
docs/phase-2d1-j8-evidence
```

Source freeze:

```text
1136bb2dd861844cb16e46ac490ab25ec01a56a2
```

Implementation integration:

```text
ac01e30eb17602dfb2923733048911813d353dc5
```

Planned archaeological prerelease:

```text
v0.1.0-phase2d1-channel-occupancy-overlap.1
```

## Status

**DEVICE ACCEPTANCE: PASS**

**VISUAL ACCEPTANCE: PASS**

**FEATURE CI: PASS**

**IMPLEMENTATION INTEGRATION: COMPLETE**

**INTEGRATED DEV CI: PASS — 37667218512**

**IMPLEMENTATION FREEZE: COMPLETE**

**ARCHAEOLOGY TEXT: COMPLETE**
