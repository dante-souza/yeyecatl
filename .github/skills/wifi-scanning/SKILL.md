---
name: "wifi-scanning"
description: "Yeyecatl Wi-Fi scan acquisition and normalization rules. Covers ScanResult handling, freshness, BSSID/SSID identity, hidden networks, capabilities and Android scan limitations."
---

# Skill — Wi-Fi Scanning

## Observation model
A scan is a time-bounded observation returned by Android, not a guaranteed real-time RF census.

Normalize platform data into project-owned types before exposing it to UI/domain layers.

Recommended normalized fields:

```text
ssid
bssid
rssiDbm
frequencyMhz
channelNumber?
band
channelWidth?
capabilitiesRaw?
securitySummary?
observedAt
sourceTimestamp?
```

## Identity rules

- **BSSID** is the primary radio/AP observation identity when present.
- **SSID** is a network name and is not unique.
- Multiple BSSIDs with the same SSID must remain distinct observations.
- Hidden/blank SSID is valid input, not an error.

## Freshness

If Android returns timestamps or scan-age information, preserve enough metadata to distinguish a newly observed result from stale cached data.

UI wording should avoid implying “live” when the platform result may be cached/throttled.

## Unknown data

Use nullable/unknown enums instead of made-up defaults for:
- channel width
- security details
- band
- channel
- PHY capabilities

## Error/state model

A scan request should be able to surface:

```text
Ready
PermissionRequired
LocationServicesRequired
WifiDisabled
Scanning
Results(data, freshness)
NoResults
ThrottledOrUnavailable
Error(reason)
```

Exact types may differ; semantic states should not collapse into a generic exception.
