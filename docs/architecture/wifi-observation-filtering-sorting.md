# Wi-Fi Observation Filtering and Sorting

Status: Phase 2B implementation baseline

Phase 2B adds deterministic filtering and sorting for the current scan snapshot.
It does not change Phase 2-zero signal-scope semantics and does not mutate Phase
2A temporal history.

## Goal

Let the user narrow and order the current nearby-network list without changing
the underlying observation snapshot.

The query boundary is pure domain logic:

```text
WifiScanSnapshot.observations
          +
WifiObservationQuery
          |
          v
WifiObservationQueryEngine
          |
          v
derived visible observation list
```

The original snapshot remains unchanged.

## Query Contract

```kotlin
data class WifiObservationQuery(
    val band: WifiBand? = null,
    val text: String = "",
    val sort: WifiObservationSort = WifiObservationSort.PlatformOrder
)
```

A null band means all observed bands.

Text filtering is case-insensitive and matches either:

- SSID display text, when available;
- BSSID text, when available.

Blank text does not filter the snapshot.

## Sort Contract

```kotlin
enum class WifiObservationSort {
    PlatformOrder,
    StrongestFirst,
    WeakestFirst,
    SsidAscending,
    ChannelAscending
}
```

### PlatformOrder

Preserves the order supplied by the current snapshot after filtering.

This is the default so Phase 2B does not silently change pre-existing list
behavior.

### StrongestFirst

Known RSSI values are ordered from stronger to weaker.

Observations without RSSI are retained and placed after measurable
observations.

Ties are deterministic and use normalized SSID display text followed by BSSID.

### WeakestFirst

Known RSSI values are ordered from weaker to stronger.

Observations without RSSI are retained and placed after measurable
observations.

### SsidAscending

SSID display text is compared case-insensitively.

Observations without displayable SSID text are retained and placed after named
observations. BSSID is the deterministic tie-breaker.

### ChannelAscending

Primary channel is derived through the existing pure RF interpreter.

Known channels are ordered numerically. Observations whose primary channel is
unknown are retained and placed last.

No channel is invented when the frequency cannot be interpreted.

## UI Independence

The nearby-network list has its own query controls:

- SSID/BSSID text filter;
- all bands / 2.4 GHz / 5 GHz / 6 GHz list filter;
- explicit sort selector.

These controls are intentionally independent from:

- the spectrum band selector;
- Phase 2-zero `All / Strongest 5 / Weakest 5` spectrum/history scope;
- the Phase 2A temporal store.

Filtering the current list therefore cannot delete temporal samples or alter
the spectrum/history selection.

The UI shows both:

- total observed networks in the snapshot;
- visible networks after the current query.

This distinction prevents a filtered list from looking like scan data loss.

## Phase 2B.1 — Nearby Network List Polish

The current-snapshot query behavior remains unchanged, but each visible observation
is now presented with a visual hierarchy instead of one long diagnostic sentence.

Each network card shows:

- SSID as the primary title;
- RSSI as the immediately visible signal measurement;
- BSSID as a secondary monospace identity;
- band, primary channel and channel width in one compact RF summary;
- observed Wi-Fi standard only when Android reports a known value;
- the raw Android capability string without reinterpreting it as a friendlier
  security label.

Hidden SSIDs are shown as `Hidden network`. A missing non-hidden display name is
shown as `SSID unavailable`. Missing RF values remain explicit rather than being
inferred.

Primary frequency, center frequency, RF span and geometry completeness are
deliberately omitted from the nearby-network summary because band + channel already
identify the operating frequency context and channel width is the useful MHz value
for this view. Those RF details remain available in the underlying observation
model for later network-detail presentation.

Unknown Wi-Fi standard is also omitted instead of rendering a placeholder.

The presentation mapper is pure Kotlin and uses the existing
`WifiRfInterpreter` boundary. The Compose card only renders the already-derived
presentation model.

This is a presentation-only refinement. It does not change filtering, sorting,
signal ranking, temporal history, observation identity, RF interpretation or the
underlying scan snapshot.

### Physical-device acceptance

Phase 2B.1 was accepted on the Samsung Galaxy J8 (SM-J810M, Android 10) after
device testing of the compact and expanded nearby-network cards.

The accepted device state demonstrates:

- multiple real nearby observations rendered as compact cards;
- SSID and BSSID remaining visually distinct;
- RSSI retaining its established position, size and accent color;
- compact RF summaries limited to band, channel and channel width;
- `More` expanding an individual card vertically;
- `Less` returning it to the compact state;
- the raw Android capability string fitting in the expanded card;
- other cards remaining compact while one card is expanded.

The accepted layout is deliberately not treated as the final network-detail
surface. More information can be added later without reopening the Phase 2B.1
summary-card contract.

### Expandable observation card

The compact card is the default state so a crowded scan remains easy to skim.
Its visible summary is limited to:

- SSID;
- RSSI;
- BSSID;
- band, channel, channel width and a known Wi-Fi standard.

A `More` action expands the same card vertically. The expanded state reveals:

- the full raw Android capability string without line-count truncation;
- the count of distinct BSSIDs in the latest unfiltered snapshot advertising the
  exact same display SSID, when more than one exists.

The SSID/BSSID count is intentionally exact and case-sensitive. Hidden or
unavailable SSIDs are not grouped together, and duplicate occurrences of the same
BSSID count once.

`Less` collapses the card back to the compact summary. Expansion is presentation
state only and does not modify filtering, sorting, the snapshot, or temporal
history.

## Required Tests

Deterministic JVM tests cover:

- band filtering;
- case-insensitive SSID matching;
- BSSID text matching;
- strongest-first ordering;
- weakest-first ordering;
- unknown RSSI retention;
- case-insensitive SSID sorting;
- unknown SSID retention;
- channel ordering through RF interpretation;
- unknown channel retention;
- preservation of the original input list.

Compose coverage verifies that current-list filtering does not alter the
spectrum selection.

## Non-Goals

Phase 2B does not add:

- network-detail navigation;
- saved filters;
- persistent user preferences;
- temporal filtering of retained history;
- signal smoothing;
- interference/recommendation scoring;
- persistence or Room;
- export.

Network detail remains Phase 2C.
