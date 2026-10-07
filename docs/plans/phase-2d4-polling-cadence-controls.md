# Phase 2D.4 — Polling Cadence Controls + Dynamic Scan Finalization

## Goal

Finish Phase 2D by making Dynamic Scan feel like a conventional Wi-Fi analyzer
while preserving Yeyecatl's explicit distinction between a scan request and a
fresh RF observation.

## Analyzer-style polling presets

Expose a compact Polling interval control with these presets:

```text
1 s   2 s   5 s   10 s   30 s
```

Default: **5 s**.

The preset model deliberately favors quick, recognizable analyzer-style choices
rather than an arbitrary numeric text field.

## Physical-test finding — request count versus history

Moto G41 testing at a 5 s polling interval exposed an important presentation
ambiguity: the old `Scans: N` label counted timer-driven scan requests, while
Signal history only advanced when Android delivered a Fresh result update.

Phase 2D.4 therefore uses separate counters:

- **Requests** — polling requests issued by Yeyecatl in the current dynamic session;
- **Fresh updates** — fresh snapshots that actually contributed temporal samples.

The Fresh updates counter is the session-level number expected to correlate with
history growth. A particular BSSID can still have fewer plotted points when it
is absent from some fresh snapshots.

## Android throttling semantics

The selected interval controls how often Yeyecatl attempts a foreground scan
request. It does not guarantee that Android accepts every request or produces a
new RF snapshot.

Keep the existing truth model:

- request counter = requests issued by Yeyecatl;
- request rejection/throttling = platform outcome;
- Fresh / Cached / Unknown = result freshness;
- passive scan broadcasts remain valid observations when Android provides them.

Short intervals are therefore best-effort polling controls.

## Interaction

- show Polling interval near the Dynamic Scan control;
- allow choosing the cadence before Dynamic Scan starts;
- keep the selected interval visible while scanning;
- changing interval while Dynamic Scan is running resets the current countdown
  and schedules the next request using the new interval;
- changing the interval must not itself issue an extra immediate scan;
- progress bar and Next scan countdown use the selected cadence;
- restarting Dynamic Scan resets the request counter but preserves the selected
  polling interval.

## Persistence

Persist the selected polling interval as an app preference so a user choosing
5 s, 10 s or another preset gets the same cadence on the next launch.

Unknown or obsolete persisted values fall back to 5 s.

## Cadence domain behavior

Extend WifiForegroundScanCadence so its interval can be changed safely at
runtime.

Required behavior:

- interval must remain positive;
- interval changes while disabled only update configuration;
- interval changes while enabled/backgrounded update configuration without
  requesting a scan;
- interval changes while enabled/foregrounded cancel the pending timer and
  schedule the next request at the new interval;
- no extra immediate request is emitted solely because interval changed;
- enable behavior remains: first dynamic request is immediate;
- foreground leave/re-enter behavior remains intact.

## Physical-test finding — analyzer motion and temporal history

Moto G41 testing showed that request-driven redraws alone do not feel like a
conventional analyzer and that sparse Fresh results can make temporal lines look
broken or misleading.

Phase 2D.4 therefore distinguishes visual continuity from RF observation:

- spectrum envelopes animate for a short transition between accepted snapshots;
- no intermediate animation frame is recorded as an RF observation;
- signal history uses a fixed two-minute rolling wall-clock viewport;
- the history viewport advances every second even when Android does not provide
  a fresh scan;
- long gaps between fresh samples break the line instead of drawing a false
  continuous bridge;
- individual samples remain visible as points;
- history still follows the active band and signal-scope selection.

This gives the app the visual motion expected from an analyzer without
fabricating scan data.

## Physical-test finding — splash visibility and rejection presentation

Moto G41 testing on fast startup made the branded system splash effectively
imperceptible. Keep the Yeyecatl splash visible for a short minimum window so
the app identity remains visible without turning startup into a fake loading
screen.

Rapid polling also made Android request rejection copy appear as a prominent
main-screen error even though throttling is expected at short intervals. During
Dynamic Scan, expected request rejection is contained inside the stable Dynamic
Scan status card while Observation continues to expose the last usable results.
Unexpected scanner errors remain visible as errors.

## Physical-test finding — stable scan card geometry

Moto G41 testing also exposed visible vertical jitter while the Dynamic Scan card
was updating. The card must keep stable vertical geometry while request counts,
fresh-update counts and countdown values change.

The live status card therefore reserves fixed line counts for dynamic labels and
the explanatory copy so timer recomposition cannot reflow the card and push the
rest of the analyzer up/down.

## UI

Use responsive chips/buttons consistent with the Phase 2D.3 control language.

Suggested presentation:

```text
Polling interval
[ 1 s ] [ 2 s ] [ 5 s ] [ 10 s ] [ 30 s ]

Dynamic scan                         Scans: 6
██████████████░░░░░░░░░░░░░░░░░░
Next scan in 3s · 5s polling
```

Keep the existing Android throttling explanation concise and visible.

## Tests

Add or update tests for:

- default 5 s cadence;
- selecting every supported polling preset;
- live interval change reschedules without an immediate request;
- interval persistence and invalid-value fallback;
- progress/countdown presentation uses selected interval;
- request counter reset behavior remains correct;
- existing static scan, BSSID selection and nearby-network behavior are
  unaffected.

## Device validation

Validate on both physical devices:

```text
make device-smoke-j8
make device-smoke-g41
```

On the G41, test at least 1 s, 5 s and 30 s.

Acceptance should explicitly observe that short polling intervals may produce
rejected/cached behavior under Android throttling while Yeyecatl continues to
report request count and freshness honestly.

## Phase boundary

Phase 2D.4 is the final Phase 2D feature block. After physical-device acceptance,
freeze the evidence and move to release/archaeology work rather than adding
further Dynamic Scan scope.
