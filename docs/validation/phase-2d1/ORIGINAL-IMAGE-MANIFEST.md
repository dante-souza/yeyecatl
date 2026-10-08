# Phase 2D.1 original J8 image manifest

Validation date: 2026-10-07

The following photographs were supplied during the physical Galaxy J8 acceptance session.
Hashes were calculated from the original uploaded JPEG bytes before any transformation.

| Archaeological name | Original upload | Resolution | Bytes | SHA-256 | Meaning |
|---|---|---:|---:|---|---|
| `yeyecatl-phase-2d1-j8-initial-overlap-card.jpg` | `image-1791391043337.jpg` | 1152×1536 | 260,610 | `d71083ab693407d1a53d8eef8454d39fc6055be84924190bcd122a60b1871963` | Initial functional 2D.1 card before J8 readability polish; exposed unsupported overlap-arrow glyphs and duplicate-SSID ambiguity. |
| `yeyecatl-phase-2d1-j8-final-overview-landscape.jpg` | `image-1791394282541.jpg` | 1536×1152 | 233,821 | `5fa2c36aef2f94692398fcdcb6813c28b57499b82d83a5bd082cdededf322eb2` | Final accepted summary showing 72 APs, 10 primary channels, 1151 overlap pairs and 32.0 average overlap neighbors/AP. |
| `yeyecatl-phase-2d1-j8-final-overlap-detail.jpg` | `image-1791394294818.jpg` | 1152×1536 | 209,378 | `078e73d06f9ad368285a62747c5bdf599454ab5d948903c9cb169818536ccdd2` | Final accepted overlap-detail presentation with ASCII-safe `vs`, short BSSID suffixes and split overlap-bandwidth rows. |

## Binary-preservation note

The GitHub connector available to this archaeology run can commit UTF-8 repository
content and Git objects, but it cannot ingest the raw bytes of conversation image
attachments directly. The original JPEGs therefore remain identified by the
byte-level SHA-256 hashes above, while the repository/release preserves the source
freeze, validation record and APK. No substituted or regenerated image is presented
as the original evidence.
