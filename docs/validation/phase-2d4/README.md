# Yeyecatl Phase 2D.4 — Polling + Rolling History Checkpoint

## Milestone

This checkpoint freezes the accepted polling-control and rolling-history behavior
before analyzer-spectrum readability polish begins.

## Validation date

2026-10-07

## Accepted behavior

- analyzer-style polling presets: 1 s / 2 s / 5 s / 10 s / 30 s;
- default polling interval: 5 s;
- selected interval persists across launches;
- changing interval while Dynamic Scan is running reschedules the next request
  without issuing an extra immediate request;
- Dynamic Scan separates Requests from Fresh updates;
- short polling intervals remain explicitly best-effort because Android may
  throttle or reject requests;
- expected polling rejection stays contained in the Dynamic Scan status card;
- Observation continues to expose the last usable result set;
- Dynamic Scan card geometry remains stable while counters/countdown change;
- branded splash remains perceptible on fast startup;
- spectrum transitions animate between accepted snapshots without creating fake
  RF observations;
- signal history uses a fixed two-minute rolling wall-clock viewport;
- long missing-data periods break history lines instead of drawing a false
  continuous bridge;
- history points still represent Fresh result updates only.

## Physical-device observations

Moto G41 testing demonstrated the distinction between polling requests and
fresh updates clearly. At aggressive 2 s / 5 s polling, request count can grow
much faster than fresh-update count because of Android scan throttling.

This behavior is intentional and remains visible in the UI.

## Frozen feature head

```text
e5b991c6b7f6414ef2d50dd4d3150c0f03e574c0
docs: record analyzer motion and rolling history model
```

## CI

```text
37693214474 — PASS
```

## Next block

Phase 2D.4b — Analyzer Spectrum Readability Polish
