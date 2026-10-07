SHELL := /bin/sh

ifeq ($(OS),Windows_NT)
GRADLE ?= ./gradlew.bat
else
GRADLE ?= sh ./gradlew
endif

ADB ?= adb
APP_ID := io.github.dante_souza.yeyecatl
TEST_APP_ID := $(APP_ID).test
J8_SERIAL ?= 38c19745
G41_SERIAL ?= 0077860711
DEBUG_APK := app/build/outputs/apk/debug/app-debug.apk

.PHONY: help setup build assemble-debug test unit-test android-test android-test-build android-test-install android-test-diagnostics lint check clean install-debug install-debug-j8 install-debug-g41 open-app open-app-j8 open-app-g41 adb-devices device-info device-info-j8 device-info-g41 device-smoke device-smoke-j8 device-smoke-g41 logcat app-logcat device-diagnostics agents-list skills-list agents-check

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
	  '  make android-test-build Compile the instrumentation test APK' \
	  '  make android-test-install Install app + instrumentation APKs without running tests' \
	  '  make android-test-diagnostics Inspect installed instrumentation/Compose test host' \
	  '  make lint          Run Android lint' \
	  '  make check         Run build, tests, lint and agent validation' \
	  '  make clean         Remove Gradle build outputs' \
	  '  make install-debug Install debug APK with adb' \
	  '  make install-debug-j8 Install debug APK on Galaxy J8' \
	  '  make install-debug-g41 Install debug APK on Moto G41' \
	  '  make open-app      Launch the already-installed Yeyecatl app' \
	  '  make open-app-j8   Launch Yeyecatl on Galaxy J8' \
	  '  make open-app-g41  Launch Yeyecatl on Moto G41' \
	  '  make adb-devices   List connected adb devices' \
	  '  make device-info   Show connected device/app environment' \
	  '  make device-info-j8 Show Galaxy J8 device/app environment' \
	  '  make device-info-g41 Show Moto G41 device/app environment' \
	  '  make device-smoke  Install and launch the debug app' \
	  '  make device-smoke-j8 Build, install and launch on Galaxy J8' \
	  '  make device-smoke-g41 Build, install and launch on Moto G41' \
	  '  make logcat        Stream device logcat' \
	  '  make app-logcat    Stream Yeyecatl scan diagnostics' \
	  '  make device-diagnostics Write sanitized device diagnostics' \
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

android-test-build:
	@$(GRADLE) :app:assembleDebugAndroidTest

android-test-install: install-debug android-test-build
	@printf '%s\n' 'Installing instrumentation APK with non-streaming ADB mode...'
	@$(ADB) install --no-streaming -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk

android-test-diagnostics: android-test-install
	@printf '%s\n' '=== Installed APK paths ==='
	@$(ADB) shell pm path $(APP_ID) || true
	@$(ADB) shell pm path $(TEST_APP_ID) || true
	@printf '%s\n' '' '=== Registered instrumentation ==='
	@$(ADB) shell pm list instrumentation | grep -F '$(APP_ID)' || true
	@printf '%s\n' '' '=== Test package summary ==='
	@$(ADB) shell dumpsys package $(TEST_APP_ID) | grep -E 'Package \[|versionCode|versionName|targetSdk|instrumentation|AndroidJUnitRunner' || true
	@printf '%s\n' '' '=== Compose host activity in target package ==='
	@$(ADB) shell dumpsys package $(APP_ID) | grep -E 'androidx\.activity\.ComponentActivity|MainActivity' || true
	@printf '%s\n' '' '=== Generated merged manifests ==='
	@$(GRADLE) :app:processDebugMainManifest :app:processDebugAndroidTestManifest >/dev/null
	@for manifest in app/build/intermediates/*/debug/*/AndroidManifest.xml; do \
	  if [ -f "$manifest" ]; then printf '%s\n' "$manifest"; fi; \
	done
	@printf '%s\n' '' '=== Host/instrumentation declarations in generated manifests ==='
	@grep -R -n -E 'androidx\.activity\.ComponentActivity|<instrumentation|AndroidJUnitRunner' app/build/intermediates/*/debug 2>/dev/null || true

lint:
	@$(GRADLE) :app:lintDebug

check: build unit-test android-test-build lint agents-check

clean:
	@$(GRADLE) clean

install-debug:
	@$(GRADLE) :app:installDebug

install-debug-j8: assemble-debug
	@$(ADB) -s $(J8_SERIAL) install --no-streaming -r $(DEBUG_APK)

install-debug-g41: assemble-debug
	@$(ADB) -s $(G41_SERIAL) install --no-streaming -r $(DEBUG_APK)

open-app:
	@$(ADB) shell am start -n $(APP_ID)/.MainActivity

open-app-j8:
	@$(ADB) -s $(J8_SERIAL) shell am force-stop $(APP_ID)
	@$(ADB) -s $(J8_SERIAL) shell am start -n $(APP_ID)/.MainActivity

open-app-g41:
	@$(ADB) -s $(G41_SERIAL) shell am force-stop $(APP_ID)
	@$(ADB) -s $(G41_SERIAL) shell am start -n $(APP_ID)/.MainActivity

adb-devices:
	@$(ADB) devices

device-info:
	@printf 'manufacturer='; $(ADB) shell getprop ro.product.manufacturer
	@printf 'model='; $(ADB) shell getprop ro.product.model
	@printf 'android_release='; $(ADB) shell getprop ro.build.version.release
	@printf 'api_level='; $(ADB) shell getprop ro.build.version.sdk
	@printf 'abi='; $(ADB) shell getprop ro.product.cpu.abi
	@$(ADB) shell wm size
	@$(ADB) shell wm density
	@$(ADB) shell pm list features | grep 'android.hardware.wifi' || true
	@$(ADB) shell dumpsys package $(APP_ID) | grep -E 'versionName|versionCode' || true

device-info-j8:
	@printf 'serial=$(J8_SERIAL)\n'
	@printf 'manufacturer='; $(ADB) -s $(J8_SERIAL) shell getprop ro.product.manufacturer
	@printf 'model='; $(ADB) -s $(J8_SERIAL) shell getprop ro.product.model
	@printf 'android_release='; $(ADB) -s $(J8_SERIAL) shell getprop ro.build.version.release
	@printf 'api_level='; $(ADB) -s $(J8_SERIAL) shell getprop ro.build.version.sdk
	@printf 'abi='; $(ADB) -s $(J8_SERIAL) shell getprop ro.product.cpu.abi
	@$(ADB) -s $(J8_SERIAL) shell wm size
	@$(ADB) -s $(J8_SERIAL) shell wm density
	@$(ADB) -s $(J8_SERIAL) shell pm list features | grep 'android.hardware.wifi' || true
	@$(ADB) -s $(J8_SERIAL) shell dumpsys package $(APP_ID) | grep -E 'versionName|versionCode' || true

device-info-g41:
	@printf 'serial=$(G41_SERIAL)\n'
	@printf 'manufacturer='; $(ADB) -s $(G41_SERIAL) shell getprop ro.product.manufacturer
	@printf 'model='; $(ADB) -s $(G41_SERIAL) shell getprop ro.product.model
	@printf 'android_release='; $(ADB) -s $(G41_SERIAL) shell getprop ro.build.version.release
	@printf 'api_level='; $(ADB) -s $(G41_SERIAL) shell getprop ro.build.version.sdk
	@printf 'abi='; $(ADB) -s $(G41_SERIAL) shell getprop ro.product.cpu.abi
	@$(ADB) -s $(G41_SERIAL) shell wm size
	@$(ADB) -s $(G41_SERIAL) shell wm density
	@$(ADB) -s $(G41_SERIAL) shell pm list features | grep 'android.hardware.wifi' || true
	@$(ADB) -s $(G41_SERIAL) shell dumpsys package $(APP_ID) | grep -E 'versionName|versionCode' || true

device-smoke: install-debug open-app

device-smoke-j8: install-debug-j8 open-app-j8

device-smoke-g41: install-debug-g41 open-app-g41

logcat:
	@$(ADB) logcat

app-logcat:
	@$(ADB) logcat -s YeyecatlWifiScan:D AndroidRuntime:E '*:S'

device-diagnostics:
	@mkdir -p build/device-diagnostics
	@{ \
	  printf 'Yeyecatl device diagnostics\n'; \
	  printf 'manufacturer='; $(ADB) shell getprop ro.product.manufacturer; \
	  printf 'model='; $(ADB) shell getprop ro.product.model; \
	  printf 'android_release='; $(ADB) shell getprop ro.build.version.release; \
	  printf 'api_level='; $(ADB) shell getprop ro.build.version.sdk; \
	  printf 'abi='; $(ADB) shell getprop ro.product.cpu.abi; \
	  $(ADB) shell wm size; \
	  $(ADB) shell wm density; \
	  $(ADB) shell pm list features | grep 'android.hardware.wifi' || true; \
	  $(ADB) shell dumpsys package $(APP_ID) | grep -E 'versionName|versionCode' || true; \
	} > build/device-diagnostics/device-info.txt
	@printf '%s\n' 'Wrote build/device-diagnostics/device-info.txt'

agents-list:
	@find .github/agents -maxdepth 1 -type f -name '*.agent.md' -print | sort

skills-list:
	@find .github/skills -mindepth 2 -maxdepth 2 -type f -name 'SKILL.md' -print | sort

agents-check:
	@python3 -c "from pathlib import Path; import sys; files=list(Path('.github/agents').glob('*.agent.md'))+list(Path('.github/skills').glob('*/SKILL.md')); bad=[str(p) for p in files if not p.read_text(encoding='utf-8').startswith('---\\n')]; print(f'validated {len(files)} agent/skill files'); print('invalid frontmatter: '+', '.join(bad)) if bad else None; sys.exit(bool(bad))"
