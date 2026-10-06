# ADR-004: Scan Freshness and State Semantics

Status: Accepted

Date: 2026-10-02

## Context

Android scan APIs can return latest available results even when a new scan did
not run. `startScan()` can return `false`, but Android does not reliably expose
the exact rejection cause. Android also documents scan frequency limits, which
can tempt UI code to label any rejection as throttling.

## Decision

Yeyecatl separates:

- platform preconditions;
- scan request acceptance or rejection;
- result availability;
- freshness classification;
- diagnostic hints.

Yeyecatl will not expose a definitive `Throttled` scan state unless Android
provides evidence for that cause. Staleness is a Yeyecatl freshness
classification based on age-at-capture.

## Rationale

- `startScan() == false` does not prove throttling.
- Cached results can still be useful when labeled honestly.
- Users need clear recovery actions for permission, Location Services, and Wi-Fi
  state.
- Reviewers and tests need exact state semantics.

## Consequences

- UI copy must use careful wording such as "scan request was not accepted" and
  "results may be stale."
- Diagnostics may include possible platform rate limiting only as a hint.
- Adapter tests must cover permission changes between precheck and API call.

## Related Documents

- `docs/architecture/scan-state-model.md`
- `docs/research/android-wifi-platform-contract.md`
