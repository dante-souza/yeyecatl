---
name: "rf-analysis"
description: "Pure RF-domain guidance for Yeyecatl: band/channel conversion, channel overlap, RSSI presentation and truthful interpretation of 2.4/5/6 GHz observations."
---

# Skill — RF Analysis

## Scope
Use for deterministic calculations and visualization inputs. Keep these functions Android-free where possible.

## Rules

### Frequency is source data
Use observed center frequency as the source value. Derive band/channel through one centralized, tested utility.

On supported Android APIs, platform helpers such as `ScanResult.convertFrequencyMhzToChannelIfSupported()` may be used behind the adapter; otherwise use tested project logic.

Official reference:
- https://developer.android.com/reference/android/net/wifi/ScanResult

### Bands
Yeyecatl's first-class UI bands:
- 2.4 GHz
- 5 GHz
- 6 GHz

Do not assume availability by device name or Android version.

### RSSI
RSSI is useful for relative signal presentation, but do not convert it into precise distance without a separately justified propagation model and clear caveats.

### Channel overlap
- model overlap from center frequency + channel width, not only channel number
- distinguish 20/40/80/160/etc. when data is available
- if width is unknown, label the visualization as approximate rather than inventing width

### Tests
Include boundary/representative cases for each supported band and unknown/out-of-range inputs.
