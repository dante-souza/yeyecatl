# ADR-002: Wi-Fi Scan Permission Contract

Status: Accepted

Date: 2026-10-02

## Context

Yeyecatl's core feature observes nearby Wi-Fi access points. Android treats Wi-Fi
scan results as location-sensitive because nearby networks can reveal physical
environment and routines.

Android 13 introduced `NEARBY_WIFI_DEVICES`, but Android documentation still
lists `WifiManager.startScan()` and `WifiManager.getScanResults()` as requiring
`ACCESS_FINE_LOCATION`.

## Decision

Phase 1 declares:

- `ACCESS_COARSE_LOCATION`
- `ACCESS_FINE_LOCATION`
- `ACCESS_WIFI_STATE`
- `CHANGE_WIFI_STATE`

Phase 1 does not declare:

- `NEARBY_WIFI_DEVICES`
- `ACCESS_BACKGROUND_LOCATION`
- `INTERNET`

The scanner requires Location Services to be enabled and models that as a
separate precondition from permission grant state.

## Rationale

- `ACCESS_FINE_LOCATION` is required for the selected target SDK and scan APIs.
- `ACCESS_COARSE_LOCATION` is declared and requested with fine location for
  Android 12+ permission UX/lint compatibility, but coarse alone is not
  considered sufficient for scan readiness.
- `ACCESS_WIFI_STATE` is required for valid scan results and state reads.
- `CHANGE_WIFI_STATE` is required to request scans with `startScan()`.
- `NEARBY_WIFI_DEVICES` is not a replacement for scan-result location
  permission.
- Background collection is outside Phase 1 and conflicts with the privacy-first
  product direction.

## Consequences

- Runtime permission UI must explain that Android requires location permission
  for nearby Wi-Fi scanning.
- Yeyecatl must not claim its scan feature is location-free.
- If future features need `NEARBY_WIFI_DEVICES`, they require a separate
  manifest and privacy review.

## Related Documents

- `docs/research/android-wifi-permissions-matrix.md`
- `docs/architecture/scan-state-model.md`
