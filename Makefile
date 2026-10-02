SHELL := /bin/sh
GRADLE ?= ./gradlew.bat
ADB ?= adb

.PHONY: help setup build assemble-debug test unit-test android-test lint check clean install-debug adb-devices agents-list skills-list agents-check

help:
	@printf '%s\n' \
	  'Yeyecatl repository helpers' \
	  '' \
	  '  make setup         Verify Gradle wrapper/tooling' \
	  '  make build         Build the debug app' \
	  '  make assemble-debug Assemble the debug APK' \
	  '  make test          Run JVM unit tests' \
	  '  make unit-test     Run JVM unit tests' \
	  '  make android-test  Run connected Android tests' \
	  '  make lint          Run Android lint' \
	  '  make check         Run build, tests, lint and agent validation' \
	  '  make clean         Remove Gradle build outputs' \
	  '  make install-debug Install debug APK with adb' \
	  '  make adb-devices   List connected adb devices' \
	  '  make agents-list   List configured agents' \
	  '  make skills-list   List configured skills' \
	  '  make agents-check  Validate agent/skill files'

setup:
	@printf 'JAVA_HOME=%s\n' "$${JAVA_HOME:-<unset>}"
	@if [ -z "$${ANDROID_HOME:-}" ] && [ -z "$${ANDROID_SDK_ROOT:-}" ]; then \
	  printf '%s\n' 'ERROR: set ANDROID_HOME or ANDROID_SDK_ROOT to a valid Android SDK.'; \
	  exit 1; \
	fi
	@printf 'ANDROID_HOME=%s\n' "$${ANDROID_HOME:-<unset>}"
	@printf 'ANDROID_SDK_ROOT=%s\n' "$${ANDROID_SDK_ROOT:-<unset>}"
	@printf 'Android Gradle Plugin='
	@sed -n 's/^agp = "\(.*\)"/\1/p' gradle/libs.versions.toml
	@if command -v $(ADB) >/dev/null 2>&1; then \
	  $(ADB) version; \
	else \
	  printf '%s\n' 'WARNING: adb not found on PATH; install-debug and adb-devices will fail.'; \
	fi
	@$(GRADLE) --version

build: assemble-debug

assemble-debug:
	@$(GRADLE) :app:assembleDebug

test: unit-test

unit-test:
	@$(GRADLE) :app:testDebugUnitTest

android-test:
	@$(GRADLE) :app:connectedDebugAndroidTest

lint:
	@$(GRADLE) :app:lintDebug

check: build unit-test lint agents-check

clean:
	@$(GRADLE) clean

install-debug:
	@$(GRADLE) :app:installDebug

adb-devices:
	@$(ADB) devices

agents-list:
	@find .github/agents -maxdepth 1 -type f -name '*.agent.md' -print | sort

skills-list:
	@find .github/skills -mindepth 2 -maxdepth 2 -type f -name 'SKILL.md' -print | sort

agents-check:
	@python3 -c "from pathlib import Path; import sys; files=list(Path('.github/agents').glob('*.agent.md'))+list(Path('.github/skills').glob('*/SKILL.md')); bad=[str(p) for p in files if not p.read_text(encoding='utf-8').startswith('---\\n')]; print(f'validated {len(files)} agent/skill files'); print('invalid frontmatter: '+', '.join(bad)) if bad else None; sys.exit(bool(bad))"
