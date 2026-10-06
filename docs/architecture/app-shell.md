# App Shell and Splash Boundary

Phase 1I establishes Yeyecatl's visual application shell without changing Wi-Fi acquisition, RF interpretation, spectrum geometry, or scan-state semantics.

## Responsibilities

The shell owns:

- startup branding through the AndroidX SplashScreen compatibility API
- light and dark Compose color tokens
- top-level typography and surface treatment
- application identity in the persistent top bar
- spacing around the existing scanner/readiness content
- edge-to-edge window integration

The shell does **not** own:

- Wi-Fi permission decisions
- scan lifecycle
- RF calculations
- channel/spectrum interpretation
- persistence or navigation architecture
- interference scoring

## Startup flow

```text
launcher
   |
   v
Theme.Yeyecatl.Starting
   |
   v
AndroidX SplashScreen
   |
   v
Theme.Yeyecatl
   |
   v
MainActivity
   |
   v
YeyecatlTheme
   |
   v
scanner UI
```

No artificial splash delay is introduced. The compatibility API provides the Android 12 splash contract on older supported API levels as well.

## Theme policy

Yeyecatl follows the system light/dark preference while retaining a stable product palette derived from the launcher identity:

- deep blue-black background family
- cyan/teal primary accents
- blue secondary accents
- readable neutral surfaces and outlines

The Compose theme remains a UI concern. Domain and platform layers do not depend on theme types.

## Validation

Host-side CI must compile:

- the debug application APK
- JVM tests
- the instrumentation test APK

Connected instrumentation and startup/splash behavior are validated on the Galaxy J8 at the feature acceptance gate.
