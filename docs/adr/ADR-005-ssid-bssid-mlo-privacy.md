# ADR-005: SSID, BSSID, and MLO Privacy

Status: Accepted

Date: 2026-10-02

## Context

SSID, raw SSID bytes, BSSID, MLD MAC address, MLO link metadata, timestamps, and
future survey annotations can reveal sensitive environmental information.
Yeyecatl is an observation and diagnostics tool, not a surveillance or offensive
wireless tool.

## Decision

Yeyecatl treats Wi-Fi identifiers and observation snapshots as sensitive data.

Default rules:

- No production logs containing SSID, raw SSID bytes, BSSID, MLD MAC, MLO link
  identifiers, or full scan dumps.
- No automatic network upload of observations.
- No background collection in Phase 1.
- Exports require explicit user action.
- Debug logging that includes identifiers must be explicit, disabled by default,
  easy to disable, and documented.

## Rationale

- Wi-Fi identifiers can reveal homes, workplaces, routines, and movement.
- Android requires location permission for scan APIs because scan data is
  location-sensitive.
- Privacy-first collection is a core project objective.

## Consequences

- Logs should use counts and structural state instead of identifiers.
- Export and persistence features require separate privacy review.
- Any future location coordinates require explicit product approval and updated
  documentation.

## Related Documents

- `docs/research/android-wifi-permissions-matrix.md`
- `docs/architecture/wifi-observation-model.md`
