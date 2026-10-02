# ADR-006: Android Application Identity

Status: Accepted

Date: 2026-10-02

## Context

Phase 1A requires an Android `namespace` and `applicationId`. The project has a
GitHub owner identity at `https://github.com/dante-souza`. Java and Kotlin
package names cannot use hyphens.

## Decision

Use the normalized GitHub owner identity for Phase 1:

| Setting | Value |
|---|---|
| `namespace` | `io.github.dante_souza.yeyecatl` |
| `applicationId` | `io.github.dante_souza.yeyecatl` |

Keep `namespace` and `applicationId` identical for Phase 1.

Use these logical subpackages inside the single app module:

- `io.github.dante_souza.yeyecatl.ui`
- `io.github.dante_souza.yeyecatl.domain`
- `io.github.dante_souza.yeyecatl.data`
- `io.github.dante_souza.yeyecatl.platform.wifi`

## Rationale

- The identity is traceable to the repository owner.
- The underscore normalization keeps the package Java/Kotlin-compatible.
- Keeping namespace and application ID identical avoids needless Phase 1
  complexity.

## Consequences

- Future package or app ID changes require a new ADR because Android
  application IDs affect installed app identity.
- Domain code must still avoid Android framework imports even though it lives in
  the same app module.
