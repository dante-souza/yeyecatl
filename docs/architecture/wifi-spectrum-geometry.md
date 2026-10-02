# Wi-Fi Spectrum Geometry

Status: Accepted for Phase 1E

Phase 1E adds a pure Kotlin nominal spectrum geometry layer. It converts
interpreted RF characteristics into nominal occupied frequency segment(s), then
calculates mathematical overlap between footprints.

Phase 1F consumes this geometry for graphical rendering in
`docs/architecture/wifi-spectrum-visualization.md`.

This is not an RF spectral-mask model. It does not model sidelobes, transmitter
leakage, receiver selectivity, adjacent-channel rejection, airtime, traffic, or
real interference severity.

## Raw, Interpreted, Geometric

```text
WifiScanObservation
    -> WifiRfInterpreter
    -> WifiRfCharacteristics
    -> WifiSpectrumGeometry
```

Android acquisition keeps raw metadata. RF interpretation derives band, channel,
width and standard. Spectrum geometry derives nominal spans from those
interpreted values. The layers remain separate so future analysis can re-check
raw data instead of trusting one derived view.

## Segment Model

`WifiSpectrumSegment` represents one nominal occupied interval:

```text
centerFrequencyMhz
widthMhz
lowerFrequencyMhz = center - width / 2
upperFrequencyMhz = center + width / 2
```

The boundaries are mathematical visualization/comparison boundaries, not exact
transmit-power mask edges.

## Completeness

`WifiSpectrumFootprint` reports:

- `Complete` when required center metadata is present and the primary frequency
  falls inside the reconstructed segment set.
- `Partial` when primary frequency is known but full-width metadata is missing.
- `Unavailable` when no valid primary frequency is available.
- `Inconsistent` when supplied metadata is internally contradictory.

Unknown channel width does not fabricate a 20 MHz footprint.

## Width Geometry

| Width | Geometry |
|---|---|
| 20 MHz | One segment centered on the primary frequency. |
| 40 MHz | One segment centered on `centerFrequency0Mhz`. |
| 80 MHz | One segment centered on `centerFrequency0Mhz`. |
| 160 MHz | One contiguous segment centered on `centerFrequency0Mhz`. |
| 80+80 MHz | Two independent 80 MHz segments from `centerFrequency0Mhz` and `centerFrequency1Mhz`. |
| 320 MHz | One contiguous segment centered on `centerFrequency0Mhz`. |
| Unknown | No fabricated width; footprint is partial when primary is known. |

The primary frequency remains a separate value and is not assumed to be the
center of a wide channel.

## 80+80 MHz

80+80 MHz is deliberately not flattened into one large interval. The gap between
the two 80 MHz segments remains unoccupied for geometry purposes.

```text
segment A                  segment B
<--------80 MHz-------->   <--------80 MHz-------->
             gap
```

## Validation

Complete footprints require:

- positive primary frequency;
- positive required center frequencies;
- positive segment width;
- `lower < center < upper`;
- primary frequency inside at least one segment;
- distinct centers for 80+80 MHz.

Invalid or contradictory metadata returns an explicit non-complete footprint
instead of throwing or silently correcting values.

## Geometric Overlap

Overlap is calculated by intersecting every segment pair:

```text
overlapLower = max(a.lower, b.lower)
overlapUpper = min(a.upper, b.upper)
```

If `overlapUpper > overlapLower`, the overlap has positive bandwidth. A shared
edge has `0 MHz` overlap and does not count as overlapping.

Multi-segment footprints, including 80+80 MHz, are compared segment-by-segment.
The implementation never compares only the minimum and maximum footprint bounds,
because that would incorrectly mark the 80+80 MHz gap as occupied.

## Overlap Is Not Interference

Phase 1E reports only nominal geometric overlap. It does not say overlapping
networks interfere. Real interference depends on RSSI, traffic, airtime,
modulation, transmit power, receiver behavior, channel access, and timing.

RSSI is intentionally not used in this phase.

## Frequency Coordinates

Geometry uses MHz frequency intervals, never channel numbers. Channel numbers
are labels and can be reused across bands; frequency is the physical coordinate.

## Non-Goals

- Interference scoring.
- Congestion scoring.
- RSSI-weighted overlap.
- Best-channel or recommendation logic.
- Regulatory-domain enforcement.
- Compose Canvas spectrum graph.
- MLO analysis.
- Persistence, history, export, maps, heatmaps, or CI/CD.

## Sources

- Android `ScanResult` API reference: https://developer.android.com/reference/android/net/wifi/ScanResult
- Android `ScanResult` channel metadata source: https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/heads/android17-release/framework/java/android/net/wifi/ScanResult.java
