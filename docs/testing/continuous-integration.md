# Continuous Integration Contract

Yeyecatl uses a two-gate validation model.

## Gate 1 — GitHub CI

Every push to `dev` or `feature/**`, and every pull request targeting `dev` or `main`, runs the repository's canonical automated checks on Ubuntu 24.04 with JDK 17.

The workflow executes:

```text
make setup
make check
```

`make check` is the repository contract for host-side verification and currently covers:

- debug APK assembly
- JVM unit tests
- instrumentation test APK compilation (tests are not executed in CI)
- Android lint
- agent/skill validation

GitHub Actions must use the Makefile entry points rather than duplicating Gradle commands in workflow YAML.

## Gate 2 — Physical Android Acceptance

A feature is not considered complete solely because CI passes when it changes Android framework integration, UI behavior, Wi-Fi scanning, permissions, or RF-related behavior.

After CI is green, the completed feature branch is pulled to the local Android lab and validated on the Samsung Galaxy J8. CI compiles the instrumentation tests, but connected execution remains a physical-device acceptance gate.

Typical commands include:

```text
make adb-devices
make device-info
make device-smoke
make android-test
make app-logcat
make device-diagnostics
```

Relevant validation evidence should be committed under `docs/validation/<phase>/` before the final pull request to `dev`.

## Branch Acceptance Flow

```text
feature branch
    |
    v
GitHub CI
    |
    v
physical J8 validation
    |
    v
validation evidence committed
    |
    v
PR -> dev
```

Individual implementation commits and merge topology are preserved. Feature work is not squash-merged.
