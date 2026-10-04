# ADR-009: Pure RF Interpretation Layer

Status: Accepted

Date: 2026-10-02

## Context

Phase 1C produces raw scan observations from Android. Phase 1D needs RF-derived
fields such as band, channel, width, and Wi-Fi standard without mixing Android
framework APIs into domain logic or overwriting raw scan data.

## Decision

Add a pure domain `WifiRfInterpreter` that accepts `WifiScanObservation` or raw
radio fields and returns immutable `WifiRfCharacteristics`.

Android-specific integer constants are normalized under `platform/wifi` before
they enter the domain model. Newer Android standard metadata is guarded by API
level and maps to `Unknown` when unavailable.

## Rationale

- RF conversion is deterministic and belongs in JVM-tested domain code.
- Raw observations remain available for later export, review, or reprocessing.
- Unknown platform values are explicit rather than guessed.
- Android API-level checks stay in the platform adapter.

## Consequences

- UI can show first derived RF diagnostics without implementing analyzer views.
- Later overlap/interference work can build on `WifiRfCharacteristics`.
- 60 GHz is recognized but channel interpretation is deliberately deferred.

## Related Documents

- `docs/architecture/wifi-rf-interpretation.md`
- `docs/architecture/wifi-observation-model.md`
- `docs/architecture/android-wifi-scan-acquisition.md`
