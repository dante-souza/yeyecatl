# ADR-010: Nominal Spectrum Geometry Boundary

Status: Accepted

Date: 2026-10-02

## Context

After Phase 1D, Yeyecatl can interpret raw observations into band, primary
channel, width, center frequencies and Wi-Fi standard. The next useful step is
to derive nominal frequency spans and overlap geometry without claiming real
interference or channel quality.

## Decision

Add a pure domain `WifiSpectrumGeometry` layer that:

- converts `WifiRfCharacteristics` into nominal `WifiSpectrumFootprint` values;
- represents contiguous widths as one segment;
- represents 80+80 MHz as two independent 80 MHz segments;
- supports 320 MHz as one contiguous nominal segment;
- reports complete, partial, unavailable, or inconsistent geometry;
- calculates overlap using MHz interval intersections.

Do not include RSSI, channel scoring, recommendations, regulatory policy, or
interference language in this layer.

## Rationale

- Frequency spans are deterministic and JVM-testable.
- 80+80 MHz requires segment-aware geometry so the gap is not treated as
  occupied spectrum.
- Overlap geometry is a lower-level fact than interference severity.
- Keeping geometry separate lets later visualizations and analysis reuse it
  without Android dependencies.

## Consequences

- UI can show diagnostic spans, but not graph them yet.
- Future channel visualization can consume `WifiSpectrumFootprint`.
- Future interference/scoring work must explicitly add RSSI and airtime
  semantics rather than hiding them in geometry.

## Related Documents

- `docs/architecture/wifi-spectrum-geometry.md`
- `docs/architecture/wifi-rf-interpretation.md`
- `docs/adr/ADR-009-pure-rf-interpretation-layer.md`
