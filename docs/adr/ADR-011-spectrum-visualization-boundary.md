# ADR-011: Spectrum Visualization Boundary

Status: Accepted

Date: 2026-10-02

## Context

Yeyecatl now has raw observations, RF interpretation, and nominal spectrum
geometry. Phase 1F needs a graphical view that resembles a Wi-Fi channel graph
without moving RF logic into Compose or implying interference conclusions.

## Decision

Add a UI-layer spectrum projection and Compose Canvas chart.

The projection layer:

- selects band-specific viewports;
- converts frequency MHz to x coordinates;
- converts RSSI dBm to y coordinates;
- sorts observations weaker-first for deterministic drawing;
- preserves domain footprint segment counts, including 80+80 MHz.

The Canvas renderer consumes projected facts and draws presentation envelopes.
It does not calculate channel numbers, width, center frequency, overlap,
interference, scoring, or recommendations.

## Rationale

- MHz and dBm to pixel transforms are presentation concerns.
- RF facts remain owned by the domain layer.
- 80+80 MHz correctness requires preserving separate segments into the renderer.
- Deterministic projection is easy to JVM-test without brittle pixel-golden
  tests.

## Consequences

- The UI can show 2.4 GHz, 5 GHz, and 6 GHz spectrum charts.
- Text diagnostics remain the accessibility and precision fallback.
- Later phases can add richer interaction or visual polish without changing RF
  ownership.
- Any future interference/scoring feature must be explicit and separate from
  this visualization.

## Related Documents

- `docs/architecture/wifi-spectrum-visualization.md`
- `docs/architecture/wifi-spectrum-geometry.md`
- `docs/adr/ADR-010-nominal-spectrum-geometry.md`
