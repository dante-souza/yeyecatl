# ADR-013 — Sticky focused access-point session

## Status

Accepted for Phase 2C.2.

## Context

Phase 2C.1 intentionally resolved selected access-point detail only while the selected BSSID was present in the latest scan snapshot.

Physical-device validation on the Galaxy J8 showed that this is too strict for a focused temporal inspector. Android Wi-Fi scans can transiently omit a BSSID that is still part of the surrounding RF environment. When selection was coupled directly to one latest snapshot, the detail panel could disappear while the user was reading it.

Filtering the nearby-network list could produce a similar interruption even though the selected BSSID remained present in the underlying snapshot.

## Decision

A selected BSSID SHALL establish a sticky in-memory focused session.

When the selected BSSID is present in a new snapshot:

- the focused detail SHALL refresh from the new normalized observation;
- `lastSeenAtMillis` SHALL advance to that snapshot;
- the detail SHALL be marked as observed in the latest snapshot.

When the selected BSSID is absent from a later snapshot:

- the focused detail SHALL remain open;
- the last known normalized observation and RF interpretation SHALL be retained;
- retained temporal history SHALL continue to be exposed;
- the detail SHALL explicitly indicate that the BSSID was not seen in the latest scan;
- the UI SHALL distinguish last-observed RSSI from a current observation.

When the BSSID appears again, the focused session SHALL resume live/current detail automatically.

List filtering and sorting SHALL NOT clear the focused session.

The focused session ends only when:

1. the user clears selection;
2. the user selects another BSSID;
3. the in-memory application/session state is reset.

## Consequences

### Positive

- transient Android scan misses no longer destroy reading context;
- temporal inspection behaves as a user-selected focus rather than a one-snapshot expansion;
- stale/current state is explicit instead of inferred;
- the existing bounded temporal history remains authoritative for retained RSSI samples.

### Tradeoffs

- the focused detail may contain last-known RF metadata while the BSSID is absent from the latest scan;
- the UI must clearly label that state to avoid implying current visibility;
- this is still in-memory behavior and is not persistence.

## Non-goals

This decision does not introduce:

- persistent favorites;
- cross-process session restoration;
- lifetime last-seen storage;
- AP disappearance alarms;
- reachability inference;
- distance estimation.
