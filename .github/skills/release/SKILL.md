---
name: "release"
description: "Yeyecatl Android release discipline: versioning, signing boundaries, release notes, permission review and artifact validation."
---

# Skill — Release

## Release checklist
- version code/name updated intentionally
- release build succeeds
- tests/lint/checks green
- signing secrets are external to the repository
- manifest permissions reviewed
- no debug logging or test endpoints enabled
- app name/icon/metadata correct
- release notes describe user-visible changes
- export schema/version changes documented
- smoke-tested on at least one real supported device

## Secrets
Never store keystores, signing passwords, API keys or tokens in tracked source files.
