---
name: "logging"
description: "Logging policy for Yeyecatl. Prevents leaking SSID/BSSID/location-like data while keeping diagnostics useful."
---

# Skill — Logging

## Default policy
Production logs must not dump raw scan results.

Avoid logging:
- SSID
- BSSID
- coordinates
- complete exported observations
- tokens/secrets

Prefer structural diagnostics:

```text
scan result count=17
bands observed=2
scan state=THROTTLED
permission state=MISSING_FINE_LOCATION
```

## Debug mode
If a developer-only diagnostic mode logs identifiers:
- make it explicit
- make it easy to disable
- never enable it by default in release builds
- document the exposure
