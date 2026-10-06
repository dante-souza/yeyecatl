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
