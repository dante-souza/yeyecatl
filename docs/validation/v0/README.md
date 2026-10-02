# Yeyecatl v0 — First Physical-Device Validation

## Milestone

Yeyecatl v0 successfully completed its first real RF scan on physical
Android hardware.

The complete pipeline was validated:

Android Wi-Fi hardware
→ Android Wi-Fi framework
→ Yeyecatl scan repository
→ domain model
→ spectrum projection
→ Compose UI

## Validation device

| Property | Value |
|---|---|
| Device | Samsung Galaxy J8 |
| Model | SM-J810M |
| Device codename | j8y18lte |
| Android | 10 |
| Android API level | 29 |
| Application ID | `io.github.dante_souza.yeyecatl` |
| Installation | Successful through ADB |

## Build environment

| Component | Version |
|---|---|
| Gradle | 8.13 |
| Kotlin | 2.0.21 |
| Groovy | 3.0.22 |
| Ant | 1.10.15 |
| Java | Eclipse Temurin 17.0.20.1 |
| JVM architecture | 64-bit |
| Host OS | Windows 11 amd64 |

## Physical validation

Validated on the Galaxy J8:

- APK build completed successfully.
- APK installed successfully through ADB.
- Application launched on physical hardware.
- Android Wi-Fi scan results were acquired.
- Real SSIDs and BSSIDs were displayed.
- RSSI values were received.
- Frequency and channel information were parsed.
- Channel width information was exposed.
- 2.4 GHz spectrum geometry rendered from real RF data.
- Band selection UI operated on-device.
- Dense Wi-Fi environment was handled successfully.
- Approximately 65 networks were observed during the first validation.

## Status

**PASS**

Real end-to-end Wi-Fi acquisition and visualization has been demonstrated
on physical Android hardware.

This marks the first working Yeyecatl v0 hardware validation.

## Evidence

Screenshots in this directory document the first successful physical-device
run.
