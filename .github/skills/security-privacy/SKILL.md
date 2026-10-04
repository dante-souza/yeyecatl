---
name: "security-privacy"
description: "Privacy and security policy for Yeyecatl, focused on Wi-Fi identifiers, Android permissions, storage/exports, networking and non-offensive product boundaries."
---

# Skill — Security & Privacy

## Sensitive environmental data
SSID, BSSID, timestamps and any future coordinates can reveal information about places and routines.

## Rules
- collect minimally
- local-first by default
- no telemetry containing SSID/BSSID unless explicitly designed and consented
- no location coordinates by default
- redact identifiers in production logs
- use Android's safe sharing mechanisms for exports
- do not expose app components unnecessarily
- do not request Internet permission unless a feature genuinely needs it
- no hidden background collection

## Offensive boundary
Yeyecatl may analyze information the Android device legitimately exposes. It must not add deauth, credential capture, cracking, injection, permission bypass or covert surveillance features as part of the normal project.

## Permission review
Every new manifest permission requires:
1. feature justification
2. API-level rationale
3. user-visible explanation if dangerous
4. test/validation path
