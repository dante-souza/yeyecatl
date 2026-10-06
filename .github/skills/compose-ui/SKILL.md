---
name: "compose-ui"
description: "Jetpack Compose conventions for Yeyecatl: state-driven screens, reusable components, lifecycle-aware collection, accessibility and RF visualization boundaries."
---

# Skill — Compose UI

## Principles
- UI renders state; it does not perform Wi-Fi scanning directly.
- Events flow upward; immutable state flows downward.
- Keep business/RF calculations out of composables.
- Use lifecycle-aware state collection.
- Prefer small composables with previewable inputs.

## Required states
Wi-Fi screens must visually distinguish:
- missing permission
- Wi-Fi off
- Location service requirement
- scanning/loading
- results
- empty results
- stale/throttled state
- unsupported band/capability
- recoverable error

## Visualization
Channel graphs should receive normalized view data, not raw `ScanResult` objects.

## Accessibility
- do not encode signal quality by color alone
- provide text/content descriptions where useful
- keep touch targets appropriate
- graphs should have a textual/list alternative for key facts
