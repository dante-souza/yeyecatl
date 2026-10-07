# Phase 2D.5 — Signal History Continuity + Freshness Visualization

## Goal
Keep the timeline moving with honest freshness semantics despite Android scan throttling.

## Block 1 (implemented; awaiting J8 validation)
- Existing rolling 120-second viewport and one-second UI clock are retained.
- Solid history segments continue to represent genuine stored RF samples.
- The latest measured RSSI may be extended visually with a **dashed, faded guide for up to 15 seconds**.
- Once older than 15 seconds, the guide disappears and an empty gap remains.
- The legend identifies measured, held and missing data.
- The domain accumulator, polling cadence and raw observations are unchanged.
- A unit test checks the hold boundary and nonmutation of the input list.

## Later blocks
- Reduce visual congestion in All history mode with intentional density/focus hierarchy.
- Observe the connected network's RSSI independently using Android connection APIs, clearly labeled and never conflated with nearby AP scan results.
- Add observation age/freshness readouts and suitable tests.

## J8 validation
Run `make check` and `make device-smoke-j8` after pulling this branch.
In All and Strongest 5, watch a measured line extend as a subtle dashed guide,
then disappear after 15 seconds without a fresh result. Fresh samples must remain
the only source of points and lines in the retained history.

## Evidence
Capture screenshots at fresh, held and expired states, plus commands/build logs.
Do not freeze or release until physical validation and acceptance.

## Integrated Phase 2D polling policy
- Standard/default request cadence: 30 seconds.
- Lab cadences: 5 and 10 seconds, explicitly labeled experimental.
- Additional standard choices: 45, 60 and 120 seconds.
- Legacy 1 and 2 second persisted choices sanitize to the 30-second default.
- Polling remains a request cadence; it never guarantees a fresh Android scan result.
