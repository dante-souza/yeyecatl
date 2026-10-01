---
name: "makefile-workflow"
description: "Yeyecatl Makefile-first repository workflow. Use whenever adding a repeatable build, test, lint, install, tooling or agent-validation command."
---

# Skill — Makefile-First Workflow

## Contract
Humans should not need to remember long Gradle or adb commands for routine project work.

Every repeatable workflow gets a Make target that delegates to the correct underlying tool.

Examples:

```make
build:
	./gradlew build

test:
	./gradlew test

install-debug:
	./gradlew installDebug
```

Exact Gradle tasks depend on the eventual project structure.

## Rules
- targets should be composable
- `make help` documents public targets
- do not hide destructive behavior behind innocent target names
- CI may call Gradle directly, but local documented workflows remain available through Make
