# Wi-Fi Spectrum Visualization

Status: Accepted for Phase 1F

Phase 1F adds the first graphical Wi-Fi spectrum view. The chart consumes
domain RF interpretation and spectrum geometry, then projects MHz and RSSI into
screen coordinates for a Compose Canvas renderer.

The graph is a presentation tool. It is not an interference analyzer, spectrum
analyzer, simulated power spectral density plot, or channel recommendation
engine.

## Architecture

```text
WifiScanObservation
    -> WifiRfInterpreter
    -> WifiSpectrumGeometry
    -> WifiSpectrumProjection
    -> WifiSpectrumChart
    -> Compose Canvas
```

The UI projection may convert MHz to pixels and RSSI dBm to pixels. It does not
derive channel numbers, channel widths, center frequencies, overlap, scoring, or
recommendations.

## Band Views

The UI exposes separate views for:

- 2.4 GHz;
- 5 GHz;
- 6 GHz.

The bands are not combined on one axis because their frequency ranges differ too
much for a useful single view. 60 GHz observations remain visible in textual
diagnostics but are outside the Phase 1F graph.

## Viewports

Each viewport defines:

- minimum and maximum frequency in MHz;
- displayed RSSI range;
- deterministic major tick labels.

RSSI is displayed from `-30 dBm` to `-90 dBm`. Values outside that range are
clamped only for drawing position; the raw RSSI value remains unchanged.

2.4 GHz includes channel 14 in the coordinate model. 5 GHz includes the middle
and upper ranges understood by the RF interpreter. 6 GHz includes the 5935 MHz
special channel and the higher channel progression.

## Axis Strategy

The horizontal axis is frequency in MHz. Channel numbers are labels only and are
not used as geometric coordinates.

Tick density is intentionally thinned, especially for 5 GHz and 6 GHz, to keep
labels readable. This does not remove or alter underlying observations.

## Visual Envelopes

Complete footprints render as deterministic trapezoid-like envelopes:

- horizontal bounds come from domain `WifiSpectrumSegment` lower/upper MHz;
- vertical peak comes from RSSI dBm;
- color identifies observations but does not encode quality;
- the shape is an intuitive presentation envelope, not an RF spectral mask.

80+80 MHz observations render as two independent envelopes with the same style.
The gap remains empty. 320 MHz observations render as one contiguous envelope
when the domain footprint is complete.

Partial footprints render as dashed primary-frequency markers. Unavailable or
inconsistent footprints are not drawn, but they remain in textual diagnostics.

## Rendering Order And Color

Observations are drawn weaker-first and stronger-last so stronger envelopes
remain visible when shapes overlap. This is visual layering, not scoring.

Colors are deterministic from stable observation data such as BSSID. They are
for visual distinction only; they do not mean good, bad, congested, or
recommended.

## Accessibility And Diagnostics

The Canvas is not the only representation. Yeyecatl keeps the textual diagnostic
list with SSID, BSSID, RSSI, band, channel, primary frequency, width, center,
span, geometry completeness, and Wi-Fi standard.

The chart also exposes a content description with selected band and observed AP
count.

## Preview Data

Compose preview fixtures are deterministic and include 2.4 GHz, 5 GHz, 6 GHz,
80+80 MHz, and 320 MHz examples. These fixtures are preview-only and are not
injected into normal runtime scan state.

## Non-Goals

- Interference scoring.
- Congestion scoring.
- Channel recommendations.
- Automatic or periodic scanning.
- Historical graphs.
- MLO analysis.
- Persistence, export, maps, heatmaps, or CI/CD.
