# Yeyecatl — Phase 1G / v0 Hardware Validation Command Reference

**Project:** Yeyecatl  
**Phase:** Phase 1G — Physical-device validation / v0 hardware-validation milestone  
**Environment:** Windows PowerShell  
**Repository:** `M:\ATOL\GitProjects\yeyecatl`

This document contains only the shell, Makefile, Gradle, ADB, Git, and release-packaging commands used during the Phase 1G hardware-validation work and the immediate v0 release preparation.

---

# 1. Repository State and Validation Evidence

## `git status`

```powershell
git status
```

Shows the current Git branch, tracked modifications, staged files, and untracked files.

During the final validation work this was useful for confirming that the generated validation evidence under `docs/validation/` had not yet been committed.

---

## Inspect the validation evidence tree

```powershell
Get-ChildItem .\docs\validation -Recurse
```

Recursively lists the physical-device validation evidence stored under:

```text
docs/
└── validation/
    └── v0/
```

This verifies which evidence files were generated before staging them for the v0 hardware-validation milestone.

---

# 2. Java Runtime Diagnostics

## Show the Java version selected by the shell

```powershell
java -version
```

Displays the Java runtime currently resolved by PowerShell.

This command was important because the machine initially resolved a 32-bit Java 8 runtime:

```text
C:\Program Files (x86)\Java\jre1.8.0_503\bin\java.exe
```

That runtime could not reserve the Gradle daemon's requested heap.

---

## Show every Java executable found through `PATH`

```powershell
where.exe java
```

Lists all `java.exe` instances visible through the current Windows `PATH`, in resolution order.

This was used to identify competing Java installations such as:

- the old 32-bit Java 8 JRE;
- Android Studio's bundled JBR;
- the intended Temurin JDK 17.

---

## Inspect `JAVA_HOME`

```powershell
$env:JAVA_HOME
```

Displays the Java installation explicitly selected through the `JAVA_HOME` environment variable.

This is important because the Java used by Gradle may differ from the first `java.exe` found through `PATH`.

---

# 3. Testing the Android Studio JBR

## Confirm that Android Studio's bundled Java exists

```powershell
Test-Path "C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
```

Checks whether the Android Studio JetBrains Runtime executable exists.

A result of:

```text
True
```

means the runtime is available for testing.

---

## Temporarily select Android Studio's JBR

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

Temporarily makes Android Studio's bundled JBR the Java runtime for the current PowerShell session.

This runtime reported Java 25.0.3 and proved unsuitable for the Yeyecatl Gradle 8.13 build.

---

## Verify the selected runtime

```powershell
java -version
where.exe java
.\gradlew.bat -version
```

Checks three different layers:

1. the Java runtime selected by the shell;
2. the Java executable resolution order;
3. the JVM actually used by Gradle.

This distinction was essential during troubleshooting.

---

# 4. Selecting the Final JDK 17 Runtime

The stable build runtime used for Yeyecatl was Temurin JDK 17:

```text
C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot
```

## Set `JAVA_HOME`

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
```

Selects the Temurin JDK 17 installation for the current PowerShell session.

---

## Put JDK 17 first in `PATH`

```powershell
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

Prepends the selected JDK's `bin` directory to the current `PATH`.

This ensures that subsequent `java` invocations resolve to JDK 17 before older Java installations.

---

## Verify JDK 17

```powershell
java -version
where.exe java
```

Confirms that PowerShell now sees the intended JDK.

The validated runtime reported Java 17.0.20.1.

---

# 5. Gradle JVM Diagnostics and Reset

## Show Gradle and JVM information

```powershell
.\gradlew.bat -version
```

Displays:

- Gradle version;
- launcher JVM;
- daemon JVM;
- operating system information.

For the final successful setup, Gradle resolved to JDK 17 rather than Java 8 or Java 25.

---

## Stop existing Gradle daemons

```powershell
.\gradlew.bat --stop
```

Stops Gradle daemons already running for the project.

This is important after changing `JAVA_HOME` because an existing daemon may still be running with the previous JVM.

---

## Verify Gradle again after stopping the daemon

```powershell
.\gradlew.bat -version
```

Confirms that the newly started Gradle process will use the intended JDK 17 runtime.

---

# 6. Android SDK Configuration

The Android SDK used during validation was:

```text
C:\Users\dante\AppData\Local\Android\Sdk
```

## Set `ANDROID_HOME`

```powershell
$env:ANDROID_HOME = "C:\Users\dante\AppData\Local\Android\Sdk"
```

Defines the Android SDK location for tools that use `ANDROID_HOME`.

This resolved the earlier:

```text
SDK location not found
```

build failure.

---

## Align `ANDROID_SDK_ROOT`

```powershell
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
```

Sets `ANDROID_SDK_ROOT` to the same SDK installation.

This keeps Android tooling that checks either environment variable pointed at the same location.

---

## Display the SDK environment variables

```powershell
$env:ANDROID_HOME
$env:ANDROID_SDK_ROOT
```

Verifies that both variables reference the expected Android SDK.

---

## Confirm that ADB exists

```powershell
Test-Path "$env:ANDROID_HOME\platform-tools\adb.exe"
```

Checks that the Android Debug Bridge executable exists inside the configured SDK.

Expected result:

```text
True
```

---

# 7. Direct Gradle Build Validation

## Build the debug APK

```powershell
.\gradlew.bat assembleDebug
```

Directly invokes Gradle to compile and package the Android debug application.

After the Java and Android SDK configuration was corrected, this completed successfully with:

```text
BUILD SUCCESSFUL
```

The generated APK is normally located at:

```text
app\build\outputs\apk\debug\app-debug.apk
```

---

# 8. Makefile Environment Validation

Yeyecatl uses the Makefile as the normal developer-facing entry point.

## Inspect the development environment

```powershell
make setup
```

Runs the repository's environment/setup diagnostics.

The project diagnostics expose information such as:

- `JAVA_HOME`;
- Java runtime;
- Android SDK environment;
- ADB availability/version;
- Gradle runtime;
- Android Gradle Plugin version.

---

## Build through the canonical project entry point

```powershell
make build
```

Runs the project build through the Makefile rather than invoking Gradle directly.

This is the preferred normal development workflow after low-level Gradle troubleshooting is complete.

---

# 9. Software Validation Commands

## Run JVM unit tests

```powershell
make unit-test
```

Runs the deterministic JVM-side test suite.

These tests cover logic that does not require a physical Android device.

---

## Run Android lint

```powershell
make lint
```

Runs Android/static lint checks.

This catches Android-specific code-quality, compatibility, and manifest issues.

---

## Run the complete repository validation gate

```powershell
make check
```

Runs the project's aggregate validation target.

This is the normal final software-quality gate before physical-device validation.

---

# 10. ADB Device Discovery

## List connected Android devices directly

```powershell
adb devices
```

Displays devices visible to ADB.

A connected and authorized device should appear with the state:

```text
device
```

rather than `unauthorized`, `offline`, or no entry.

---

## List devices through the Makefile

```powershell
make adb-devices
```

Runs the project's canonical ADB device-discovery target.

This keeps routine Android operations behind the Makefile-first workflow.

---

## Show extended ADB device information

```powershell
adb devices -l
```

Lists connected devices with additional information such as:

- model;
- product;
- transport ID;
- device identifier.

This was useful when confirming the physical validation handset.

---

# 11. Physical Device Identification

## Read the Android device model

```powershell
adb shell getprop ro.product.model
```

Returns the device's Android model identifier.

For the successful final validation device:

```text
SM-J810M
```

---

## Read the Android OS version

```powershell
adb shell getprop ro.build.version.release
```

Returns the Android release version installed on the connected device.

For the successful Galaxy J8 validation:

```text
10
```

---

## Read the Android API level

```powershell
adb shell getprop ro.build.version.sdk
```

Returns the device's Android SDK/API level.

For the successful Galaxy J8 validation:

```text
29
```

---

## Run the project device-information helper

```powershell
make device-info
```

Collects relevant device information through the Makefile.

This provides a reproducible project-level alternative to manually issuing multiple `adb shell getprop` commands.

---

# 12. APK Installation

## Install using the Makefile

```powershell
make install-debug
```

Installs the Yeyecatl debug APK onto the currently connected Android device.

This is the preferred project entry point.

---

## Install directly with ADB

```powershell
adb install -r .\app\build\outputs\apk\debug\app-debug.apk
```

Installs or replaces the debug APK directly through ADB.

The `-r` option tells ADB to replace an existing installation while retaining its data where possible.

This direct form is useful for diagnosing whether a failure belongs to:

- the Makefile wrapper;
- ADB;
- or the Android device's installation policy.

---

# 13. Detecting the Installed Application

## Query the installed APK path

```powershell
adb shell pm path io.github.dante_souza.yeyecatl
```

Asks Android's package manager for the installed package path associated with:

```text
io.github.dante_souza.yeyecatl
```

If the application is installed, Android returns a package path.

If no path is returned, the package is not currently installed for the queried user/context.

---

# 14. Device Smoke Validation

## Run the Makefile smoke test

```powershell
make device-smoke
```

Runs the repository's physical-device smoke validation.

This is intended to provide a quick confirmation that the built application can be exercised on the connected handset.

---

# 15. Instrumentation / Android Device Tests

## Run Android instrumentation tests

```powershell
make android-test
```

Runs tests that require an Android device or emulator.

Earlier phases skipped this when no device was attached. During Phase 1G it became part of the hardware-validation workflow where the connected device permitted it.

---

# 16. Runtime Logging

## Follow Yeyecatl application logs

```powershell
make app-logcat
```

Runs the repository's application-focused Logcat helper.

This is used to inspect runtime events from the application without manually constructing a long Logcat filter.

During Phase 1G, debug-only structured scan lifecycle logging was added specifically to make this validation easier.

---

## Run broader device diagnostics

```powershell
make device-diagnostics
```

Runs the Makefile's broader physical-device diagnostic workflow.

Use it when a problem is not limited to a single application log stream.

---

## Follow the general project Logcat target

```powershell
make logcat
```

Runs the project's general Logcat view.

This is useful when the issue may involve Android framework messages in addition to Yeyecatl-specific logging.

---

# 17. Device Installation Policy Failure Seen During Validation

The initial Xiaomi/Redmi validation attempt reached ADB successfully but failed during APK installation with:

```text
INSTALL_FAILED_USER_RESTRICTED: Install canceled by user
```

The relevant diagnostic sequence was:

```powershell
adb devices -l
make device-info
make install-debug
```

The first two commands established that ADB communication and device detection were working.

The failure therefore occurred at the Android/OEM installation-policy layer rather than during compilation or ADB discovery.

---

# 18. Final Successful Hardware Validation Device

The successful validation device was confirmed with:

```powershell
adb devices -l
adb shell getprop ro.product.model
adb shell getprop ro.build.version.release
adb shell getprop ro.build.version.sdk
adb shell pm path io.github.dante_souza.yeyecatl
```

These commands collectively confirmed:

```text
Device model: SM-J810M
Android:      10
API level:    29
Package:      io.github.dante_souza.yeyecatl
```

---

# 19. Staging the v0 Validation Evidence

## Stage the validation directory

```powershell
git add .\docs\validation
```

Adds the generated `docs/validation/` evidence to Git's staging area.

---

## Review the staged file summary

```powershell
git diff --cached --stat
```

Shows a compact summary of files and line counts currently staged for commit.

This is useful before reviewing the complete patch.

---

## Review the full staged changes

```powershell
git diff --cached
```

Displays the actual staged content that will be included in the next commit.

This provides a final review of the validation evidence before recording the milestone.

---

# 20. Commit the Hardware-Validation Milestone

```powershell
git commit -m "Yeyecatl v0 — first real RF scan on physical hardware" -m "Physical-device validation PASS..."
```

Creates the Git commit recording the successful physical-device validation milestone.

The first `-m` supplies the commit subject.

The second `-m` supplies the longer commit body.

---

# 21. Push the Development Branch

```powershell
git push origin dev
```

Pushes the committed Phase 1G/v0 validation work from the local `dev` branch to the remote repository.

---

# 22. Create the Hardware-Validation Tag

```powershell
git tag -a v0-hardware-validation -m "Yeyecatl v0 — first successful real RF scan on physical hardware"
```

Creates an **annotated Git tag** named:

```text
v0-hardware-validation
```

Annotated tags store additional metadata and a tag message and are preferable for significant repository milestones.

---

# 23. Push the Release Tag

```powershell
git push origin v0-hardware-validation
```

Publishes the annotated hardware-validation tag to the remote GitHub repository.

Once pushed, GitHub can use this tag as the basis for the corresponding release.

---

# 24. Prepare the Debug APK as a GitHub Release Asset

## Create a local distribution directory

```powershell
New-Item -ItemType Directory -Force .\dist
```

Creates a `dist` directory if it does not already exist.

The `-Force` option prevents failure when the directory is already present.

---

## Copy and rename the validated APK

```powershell
Copy-Item `
  .\app\build\outputs\apk\debug\app-debug.apk `
  .\dist\yeyecatl-v0-hardware-validation-debug.apk
```

Copies the Gradle-generated debug APK into the `dist` directory and gives it a release-specific filename.

The `debug` suffix is intentionally preserved so the binary is not mistaken for a production-signed release APK.

Result:

```text
dist\yeyecatl-v0-hardware-validation-debug.apk
```

---

# 25. Calculate the APK SHA-256 Hash

```powershell
Get-FileHash `
  .\dist\yeyecatl-v0-hardware-validation-debug.apk `
  -Algorithm SHA256
```

Calculates the SHA-256 digest of the release APK.

This gives users a way to verify the integrity of the downloaded binary.

---

# 26. Create the SHA-256 Checksum File

```powershell
$hash = (
    Get-FileHash `
      .\dist\yeyecatl-v0-hardware-validation-debug.apk `
      -Algorithm SHA256
).Hash.ToLower()

"$hash  yeyecatl-v0-hardware-validation-debug.apk" |
    Set-Content `
      .\dist\yeyecatl-v0-hardware-validation-debug.apk.sha256
```

Calculates the APK's SHA-256 digest, converts it to lowercase, and writes a conventional checksum file.

Result:

```text
dist\
├── yeyecatl-v0-hardware-validation-debug.apk
└── yeyecatl-v0-hardware-validation-debug.apk.sha256
```

Both files can then be attached to the GitHub release.

---

# 27. Compact Phase 1G Command Sequence

The principal commands used across troubleshooting, validation, and release preparation can be summarized as:

```powershell
# Repository / evidence
git status
Get-ChildItem .\docs\validation -Recurse

# Java diagnostics
java -version
where.exe java
$env:JAVA_HOME

# Select stable JDK
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

# Gradle
.\gradlew.bat --stop
.\gradlew.bat -version

# Android SDK
$env:ANDROID_HOME = "C:\Users\dante\AppData\Local\Android\Sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
Test-Path "$env:ANDROID_HOME\platform-tools\adb.exe"

# Direct build
.\gradlew.bat assembleDebug

# Makefile validation
make setup
make build
make unit-test
make lint
make check

# Device discovery / information
adb devices
adb devices -l
make adb-devices
make device-info
adb shell getprop ro.product.model
adb shell getprop ro.build.version.release
adb shell getprop ro.build.version.sdk

# APK installation / package verification
make install-debug
adb install -r .\app\build\outputs\apk\debug\app-debug.apk
adb shell pm path io.github.dante_souza.yeyecatl

# Runtime/device validation
make device-smoke
make android-test
make app-logcat
make device-diagnostics
make logcat

# Validation evidence / Git milestone
git add .\docs\validation
git diff --cached --stat
git diff --cached
git commit -m "Yeyecatl v0 — first real RF scan on physical hardware" -m "Physical-device validation PASS..."
git push origin dev
git tag -a v0-hardware-validation -m "Yeyecatl v0 — first successful real RF scan on physical hardware"
git push origin v0-hardware-validation

# GitHub release APK
New-Item -ItemType Directory -Force .\dist

Copy-Item `
  .\app\build\outputs\apk\debug\app-debug.apk `
  .\dist\yeyecatl-v0-hardware-validation-debug.apk

Get-FileHash `
  .\dist\yeyecatl-v0-hardware-validation-debug.apk `
  -Algorithm SHA256

$hash = (
    Get-FileHash `
      .\dist\yeyecatl-v0-hardware-validation-debug.apk `
      -Algorithm SHA256
).Hash.ToLower()

"$hash  yeyecatl-v0-hardware-validation-debug.apk" |
    Set-Content `
      .\dist\yeyecatl-v0-hardware-validation-debug.apk.sha256
```

---

# 28. Troubleshooting Flow

```text
java -version / where.exe java
              |
              v
      Wrong Java selected?
              |
             yes
              |
              v
Set JAVA_HOME + PATH to Temurin JDK 17
              |
              v
      gradlew.bat --stop
              |
              v
      gradlew.bat -version
              |
              v
 Configure ANDROID_HOME / SDK
              |
              v
      gradlew.bat assembleDebug
              |
              v
     make build / make check
              |
              v
          adb devices
              |
              v
       make install-debug
              |
              v
    device-smoke / logcat
              |
              v
      validation evidence
              |
              v
      Git commit + tag
              |
              v
 APK + SHA-256 release assets
```

This sequence separates Java/Gradle problems, Android SDK problems, compilation problems, ADB transport problems, device installation-policy problems, runtime behavior, and release packaging into independently diagnosable stages.
