---
name: "architecture"
description: "Yeyecatl architectural boundaries and proportionality rules. Use for features that add data sources, persistence, modules, background work or cross-project contracts."
---

# Skill — Architecture

## Default layers

```text
UI / Compose
    ↓
ViewModel / presentation state
    ↓
Domain use cases + RF logic
    ↓
Repository interfaces where they create a real boundary
    ↓
Android Wi-Fi / persistence / export adapters
```

## Dependency direction
Domain code must not import Android framework classes.

## KISS gates
Before adding:

| Proposed complexity | Required justification |
|---|---|
| new Gradle module | build/ownership/dependency boundary benefit |
| DI framework | object graph complexity that manual construction cannot reasonably manage |
| Room database | persisted query/history requirements |
| WorkManager/background service | explicit background requirement |
| generic plugin architecture | at least two concrete interchangeable implementations |
| remote backend | explicit product requirement |

## Contracts
Version export schemas from the start once they are public or consumed by Ehécatl.
