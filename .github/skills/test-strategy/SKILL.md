---
name: "test-strategy"
description: "Yeyecatl Android test strategy covering JVM domain tests, Android integration/instrumentation, Compose UI tests and real-device Wi-Fi validation."
---

# Skill — Test Strategy

## Pyramid

```text
       Device/E2E     few
      Compose UI      focused
 Android integration targeted
   JVM unit tests     many
```

## What belongs where

### JVM unit tests
- frequency/band/channel helpers
- overlap calculations
- normalization from neutral fixtures
- sorting/filtering
- state reducers
- export mapping

### Android integration / Robolectric / instrumentation
- framework adapter behavior
- permission/state plumbing
- broadcast/callback integration
- Android-specific serialization/sharing

### Compose
- permission-state rendering
- filter/sort interaction
- navigation to network detail
- empty/error states

### Real device
- actual scan behavior
- throttling behavior
- 6 GHz visibility on capable hardware
- vendor-specific quirks

## Anti-patterns
- testing `WifiManager` by mocking every Android detail in domain tests
- random RSSI fixtures without a seed
- asserting only `not null`
- using emulator success as proof of RF hardware behavior
