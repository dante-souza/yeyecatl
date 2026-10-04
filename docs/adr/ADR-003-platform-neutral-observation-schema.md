# ADR-003: Platform-Neutral Observation Schema

Status: Accepted

Date: 2026-10-02

## Context

Yeyecatl is the Android sibling of Ehecatl. The two projects should share
concepts and future export compatibility, not implementation code. Android
`ScanResult` contains platform-specific fields such as a monotonic timestamp
since boot that should not define a portable schema.

## Decision

Define Yeyecatl observations and field snapshots with project-owned,
platform-neutral types.

The portable observation exposes:

- capture time;
- age at capture;
- SSID display text and raw SSID bytes when available;
- BSS/BSSID identity;
- MLO link identity and MLD identity when available;
- frequency, channel, band, channel width, and center frequencies;
- raw capabilities and normalized security types;
- observed Wi-Fi standard.

Android raw fields such as `ScanResult.timestamp` are retained only in optional
platform metadata.

## Rationale

- A portable schema makes later Ehecatl comparison possible.
- Android boot-relative timestamps do not work outside one device uptime.
- SSID display strings alone can destroy non-UTF-8 SSIDs.
- BSSID, MLD, and MLO link identifiers are distinct concepts.

## Consequences

- Normalization code is required at the Android adapter boundary.
- Tests must verify byte-preserving SSID handling.
- Export schema versioning is required once snapshots become public or consumed
  outside Yeyecatl.

## Related Documents

- `docs/architecture/wifi-observation-model.md`
- `docs/research/android-wifi-platform-contract.md`
