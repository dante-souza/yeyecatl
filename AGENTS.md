# Yeyecatl — Agent Operating Instructions

This repository uses specialized coding agents inspired by the Cerberus agent model.

## 1. Language and Naming

- Repository documentation, code, identifiers, tests and commits: **English**.
- User-facing conversation may follow the user's language.
- Kotlin classes/functions/variables use idiomatic English names.
- Commit messages use Conventional Commits.

## 2. Sources of Truth

Priority order:

1. user request / explicit acceptance criteria
2. `PROJECT.md`
3. accepted ADRs and project documentation
4. `AGENTS.md`
5. applicable `.github/skills/*/SKILL.md`
6. existing tested behavior

When these conflict, stop expanding scope and surface the conflict.

## 3. Core Engineering Rules

### KISS first
Do not introduce repositories, interfaces, modules, factories, DI layers, databases, background services, or abstractions merely because they are common in Android projects.

Introduce a boundary when it provides a concrete benefit: testability, platform isolation, ownership, or lifecycle safety.

### TDD for deterministic logic
For domain/RF logic, use Red → Green → Refactor.

Especially test before implementation for:

- channel/frequency calculations
- filtering/sorting
- normalization
- permission/state decision tables
- export schema transformations

### Android framework isolation
`WifiManager`, `Context`, broadcasts, permissions and other Android-specific types should remain in platform/data layers. Domain code should prefer project types.

### Never fake platform capability
If Android does not expose a value, use unknown/unsupported states. Never synthesize precision that the platform did not provide.

## 4. Wi-Fi and Android Rules

- Respect Android permissions and scan throttling.
- Never design a workaround whose purpose is to bypass OS scan limits.
- Treat scan results as observations, not a complete census of RF activity.
- Do not assume 6 GHz support merely because the OS version is recent.
- Device capability and actual scan results determine supported UI paths.
- Keep band/channel logic centralized and tested.
- Treat hidden SSIDs and duplicate SSIDs as normal cases.
- BSSID identifies a radio/AP observation more precisely than SSID; do not merge distinct BSSIDs by accident.

## 5. Privacy and Security

SSID/BSSID data may reveal sensitive environmental information.

Rules:

- no secrets in source control
- no BSSID/SSID dumps in production logs by default
- no location coordinates unless explicitly required by a feature
- no automatic network upload of observations
- exports require explicit user action
- debug logging must be easy to disable and must document what it records
- no offensive wireless functionality in the core product

## 6. Makefile-First Policy

All routine human entry points must have a Make target.

Agents may use Gradle directly for investigation, but any workflow expected to be repeated by humans must be surfaced through the Makefile.

Expected baseline targets:

```text
help
setup
build
assemble-debug
test
lint
format
check
install-debug
clean
agents-list
skills-list
agents-check
```

## 7. Dependency Policy

Before adding a dependency:

1. prove the Android/Kotlin standard library cannot reasonably solve the need
2. verify the dependency is maintained and compatible with project SDK/toolchain
3. prefer AndroidX/Jetpack or well-established libraries
4. record why it exists
5. avoid dependencies for trivial helpers

Never invent dependency versions. Verify before pinning.

## 8. Testing Policy

Preferred pyramid:

- many JVM unit tests for domain logic
- focused Android/Robolectric or instrumented tests for framework integration
- Compose UI tests for critical user flows
- a small number of end-to-end device flows

Tests are contracts. Do not weaken, skip, or delete a valid failing test just to make a build green.

## 9. Agent Roles

| Agent | Responsibility | Edits production code? |
|---|---|---:|
| `product-owner` | product scope, acceptance criteria, backlog | no |
| `architect` | boundaries, ADRs, technical direction | normally no |
| `wifi-domain` | Wi-Fi/RF and Android scanning correctness | no by default |
| `developer` | implementation and orchestration | yes |
| `task-worker` | one bounded implementation task | yes |
| `qa-engineer` | test strategy and scenarios | tests/QA artifacts only |
| `reviewer` | orchestrates code review | no |
| `architecture-reviewer` | architecture drift | no |
| `correctness-reviewer` | logic/lifecycle correctness | no |
| `security-privacy-reviewer` | privacy/security/permissions | no |
| `quality-reviewer` | KISS/SOLID/readability | no |
| `test-reviewer` | test quality/coverage | no |

## 10. Standard Workflow

```mermaid
flowchart LR
    PO[Product Owner] --> AR[Architect]
    AR --> DEV[Developer]
    AR --> RF[Wi-Fi Domain]
    RF --> DEV
    DEV --> TW[Task Worker(s)]
    DEV --> QA[QA Engineer]
    DEV --> REV[Reviewer]
    REV --> DEV
```

The Reviewer is an intentional gate, not an endless automatic loop.

## 11. Review Severity

- **Critical** — security/privacy exposure, data corruption, permission bypass, fundamentally wrong RF interpretation.
- **High** — primary user flow broken, crash/lifecycle defect, acceptance criterion missing.
- **Medium** — edge case, maintainability issue, incomplete test coverage.
- **Low** — readability or small design improvement.

Only Critical/High findings block completion by default.

## 12. Scope Discipline

Agents must implement the requested feature, not the imagined future platform.

If a task says “show scan results,” do not also add Room, cloud sync, telemetry, background services and a plugin system.
