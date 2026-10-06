# Yeyecatl — Phase 2 Analyzer Roadmap

## Status

**Current milestone:** `v0-hardware-validation` completed  
**Next major milestone:** Phase 2 — Analyzer  
**Recommended immediate phase:** **Phase 2A — Temporal Wi-Fi Observation Pipeline**

---

## 1. Context

Phase 1 established the fundamental Yeyecatl pipeline on real Android hardware:

```text
Android Wi-Fi API
        │
        ▼
Wi-Fi scan acquisition
        │
        ▼
Normalization
        │
        ▼
RF interpretation
        │
        ▼
Spectrum projection
        │
        ▼
Compose visualization
```

The application has therefore moved beyond the question:

> Can Yeyecatl scan and visualize the surrounding Wi-Fi environment?

The next question is:

> What useful analysis can Yeyecatl derive from repeated Wi-Fi observations?

Phase 2 should evolve Yeyecatl from a **snapshot-oriented scanner** into an **analyzer capable of observing change over time**.

---

# 2. Phase 2 — Analyzer

Phase 2 should be implemented incrementally.

## Phase 2A — Temporal Wi-Fi Observation Pipeline

This should be the immediate next implementation phase.

### Objective

Move from isolated scan snapshots to a bounded temporal observation model.

Yeyecatl should be able to:

- execute repeated Wi-Fi scans;
- timestamp observations;
- associate observations belonging to the same BSSID;
- maintain the current/latest state for each access point;
- retain short bounded observation history;
- identify fresh, aging, and stale observations;
- handle access points disappearing and reappearing;
- account explicitly for Android scan throttling and scan-result reuse;
- keep acquisition/history logic independent from UI presentation.

### Recommended data flow

```text
Android Wi-Fi API
        │
        ▼
Raw ScanResult
        │
        ▼
Normalized Wi-Fi Observation
        │
        ▼
Temporal Observation Store
        │
        ├── Latest AP state
        ├── Observation history
        ├── Freshness / age
        └── AP lifecycle
                │
                ▼
        Analyzer / UI consumers
```

### Key architectural rule

The temporal model should **not** be owned by the Compose UI.

The UI should consume a stable domain representation produced by the observation pipeline.

This preserves the existing architecture and allows future analysis components to operate independently of presentation.

### Suggested observation identity

The primary identity should normally be:

```text
BSSID
```

SSID alone is insufficient because multiple access points can advertise the same network name.

A future abstraction may group multiple BSSIDs into a logical network, but Phase 2A should preserve access-point-level observations.

### Suggested temporal states

A simple initial state model could include:

```text
FRESH
AGING
STALE
```

Exact thresholds should remain configurable and should not initially be interpreted as RF-quality judgments.

### Bounded history

The first implementation should deliberately avoid indefinite history retention.

Possible strategies:

- maximum number of observations per BSSID;
- maximum observation age;
- or both.

For example:

```text
BSSID
 ├── latestObservation
 └── history[N]
```

The objective is to establish the architecture, not yet create a long-term RF telemetry database.

---

# 3. Phase 2B — Filtering and Sorting

Once repeated observations are stable, add analyzer controls.

Recommended filters:

- 2.4 GHz;
- 5 GHz;
- 6 GHz;
- channel;
- SSID;
- BSSID;
- signal-strength range;
- visible / hidden SSID where Android exposes sufficient information.

Recommended sorting:

- strongest RSSI;
- weakest RSSI;
- channel;
- SSID;
- frequency;
- most recently observed;
- oldest observation.

Filtering and sorting should preferably operate on domain models rather than directly on Android `ScanResult` objects.

---

# 4. Phase 2C — Network / Access Point Detail

Add a dedicated detail view for a selected access point.

Suggested fields:

- SSID;
- BSSID;
- frequency;
- channel;
- band;
- RSSI;
- security / capabilities;
- first observed timestamp;
- last observed timestamp;
- observation age;
- observation count.

Later in this phase, the application can add a short signal-history visualization.

Example:

```text
RSSI
 -35 ┤
 -45 ┤      ╭───╮
 -55 ┤  ╭───╯   ╰──╮
 -65 ┤──╯           ╰─
     └────────────────── Time
```

This view should reflect observed values rather than attempt to infer physical distance.

---

# 5. Phase 2D — Analyzer Visualization

After the temporal and detail models are established, improve interactive visualization.

Potential additions:

- selectable networks in the spectrum chart;
- highlighting of selected BSSID;
- channel occupancy representation;
- simultaneous list + spectrum view;
- signal-over-time chart;
- per-band views;
- stronger distinction between current and stale observations.

The visualization layer should remain downstream of the RF/domain model.

---

# 6. Phase 2E — Structured Export

Add machine-readable export before building more opinionated diagnostics.

Recommended initial formats:

- JSON;
- CSV.

Possible exported information:

```text
timestamp
ssid
bssid
band
frequency_mhz
channel
rssi_dbm
security
observation_age
```

The exported structure should represent normalized/domain observations, not Compose/UI state.

This will make exported data useful for:

- offline analysis;
- regression testing;
- comparison with desktop tooling;
- future interoperability with Ehécatl;
- reproducible RF experiments.

---

# 7. What Phase 2 Should Not Do Yet

Phase 2 should avoid prematurely implementing subjective or recommendation-oriented logic such as:

- "best Wi-Fi channel";
- automatic router configuration recommendations;
- interference severity scores;
- quality ratings;
- distance estimation from RSSI;
- generalized network-health scoring.

Those features depend on assumptions that should be introduced only after Yeyecatl has a reliable temporal observation model.

---

# 8. Future Evolution

The proposed project progression is:

```text
Phase 1
Acquisition + RF interpretation + visualization
                    │
                    ▼
Phase 2
Repeated observations + analyzer
                    │
                    ▼
Phase 3
Interference / congestion models
                    │
                    ▼
Phase 4
Recommendations / diagnostics
```

## Phase 3 — Interference and Congestion Models

Potential future capabilities:

- channel overlap analysis;
- co-channel occupancy;
- adjacent-channel interference indicators;
- AP density;
- temporal stability;
- signal variance;
- competing-BSSID behavior.

## Phase 4 — Recommendations and Diagnostics

Only after the underlying measurements and models are validated should Yeyecatl begin producing recommendations such as:

- potentially less congested channels;
- problematic overlap conditions;
- unstable access points;
- unusual signal behavior;
- configuration observations.

Recommendations should remain explainable and traceable to measurable RF data.

---

# 9. Phase 2A Success Criterion

Phase 2A can be considered complete when:

> Yeyecatl can repeatedly scan on a physical Android device, associate observations belonging to the same access point, distinguish fresh from stale data, and expose a bounded signal history without coupling the scanner to the UI.

A practical validation should demonstrate:

1. repeated scans running on physical hardware;
2. the same BSSID accumulating multiple observations;
3. RSSI changes being preserved;
4. disappearing access points aging correctly;
5. reappearing BSSIDs being associated with the existing AP identity;
6. bounded history behaving as designed;
7. build, unit tests, lint, and Android validation continuing to pass.

---

# 10. Recommended Repository Placement

The preferred location is:

```text
docs/roadmap/phase-2-analyzer.md
```

Recommended repository structure:

```text
docs/
├── adr/
├── architecture/
├── testing/
├── validation/
└── roadmap/
    └── phase-2-analyzer.md
```

Why `docs/roadmap/`:

- this document describes planned development rather than an implemented architecture;
- it contains multiple future phases;
- it should remain separate from ADRs, which record architectural decisions;
- it should remain separate from validation reports, which document completed testing;
- individual architectural decisions discovered during implementation can later receive their own ADRs.

---

# 11. Repository Integration

## PROJECT.md

Add Phase 2 to the project roadmap/status section.

Suggested entry:

```markdown
### Phase 2 — Analyzer

- [ ] Phase 2A — Temporal Wi-Fi Observation Pipeline
- [ ] Phase 2B — Filtering and Sorting
- [ ] Phase 2C — Network / Access Point Detail
- [ ] Phase 2D — Analyzer Visualization
- [ ] Phase 2E — Structured Export

See [`docs/roadmap/phase-2-analyzer.md`](docs/roadmap/phase-2-analyzer.md).
```

`PROJECT.md` should remain the concise source of project status.

The detailed reasoning and scope belong in the roadmap document.

## README.md

The README only needs a short link.

Suggested form:

```markdown
## Roadmap

Current development is moving into **Phase 2 — Analyzer**, beginning with a
temporal Wi-Fi observation pipeline.

See the [Phase 2 Analyzer Roadmap](docs/roadmap/phase-2-analyzer.md).
```

Avoid copying the entire roadmap into the README.

---

# 12. Suggested Git Commit

After adding the roadmap and references:

```powershell
git add docs/roadmap/phase-2-analyzer.md PROJECT.md README.md
git commit -m "docs: define Phase 2 analyzer roadmap"
git push origin dev
```

---

# 13. Recommended Immediate Next Step

The next implementation block should be:

> **Phase 2A — Temporal Wi-Fi Observation Pipeline**

The first design work should define:

1. the normalized temporal observation model;
2. BSSID-based identity;
3. observation timestamps;
4. bounded history policy;
5. freshness/staleness semantics;
6. repository/store boundary;
7. tests for AP appearance, disappearance, and reappearance.

This establishes the foundation required by essentially every higher-level analyzer feature.

---

## Summary

`v0-hardware-validation` demonstrated that Yeyecatl can **see** the surrounding Wi-Fi environment.

Phase 2 should teach Yeyecatl to **observe how that environment changes**.

That transition—from isolated RF snapshots to temporal observations—is the natural next architectural milestone for the project.
