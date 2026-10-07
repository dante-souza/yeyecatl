# ADR-012 — BSSID-anchored access-point detail selection

## Status

Accepted for Phase 2C.1.

## Context

Phase 2B.1 provides a compact, filterable and sortable list of current Wi-Fi observations. Phase 2C requires one access point to become a stable focus for richer inspection.

SSID cannot identify one access point because several BSSIDs may advertise the same SSID. The existing temporal model is also keyed by BSSID.

## Decision

Phase 2C.1 access-point selection SHALL be anchored to an exact non-blank BSSID.

A pure domain resolver SHALL combine:

1. the selected BSSID;
2. the matching observation from the latest scan snapshot;
3. interpreted RF characteristics;
4. nominal spectrum geometry;
5. retained Phase 2A signal samples for the same BSSID.

If the selected BSSID is absent from the latest snapshot, no current detail model is produced.

The UI SHALL not claim lifetime first-seen semantics from bounded Phase 2A history. It may expose retained-sample timestamps/count/span with explicit retained-history wording.

Observations without a BSSID remain observable but are not selectable in this phase.

## Consequences

### Positive

- selection identity matches Wi-Fi access-point identity used elsewhere in the domain;
- duplicate SSIDs cannot collide;
- detail generation stays Android-independent and testable;
- existing temporal history is reused without a new store;
- unknown fields remain explicit instead of inferred.

### Tradeoffs

- a selected access point that disappears from the latest snapshot has no current detail in 2C.1;
- selection is not persisted;
- bounded temporal history cannot represent lifetime first-seen time;
- SSID-level logical-network detail remains a separate future abstraction.

## Rejected alternatives

### SSID as selection key

Rejected because one SSID can represent multiple access points/BSSIDs.

### Compose-only detail derivation

Rejected because RF interpretation and temporal association belong downstream of stable domain contracts, not inside presentation code.

### Persisted lifecycle state in 2C.1

Rejected because persistence is a later project responsibility and is not required to establish selection/detail semantics.
