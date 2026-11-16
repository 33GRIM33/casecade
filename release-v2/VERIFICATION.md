# Version 2.0 verification

- Production React/TypeScript build: passed.
- Android `assembleDebug testDebugUnitTest`: passed using Java 21 and Gradle 8.14.3.
- JVM tests: 16 notification decision tests, six Gemini safety policy tests and one original example, all passing.
- TypeScript tests: 11 passing (sessions, pause/deadline handling, completion deduplication, migration, linked tasks, midnight scheduling, app roles, VIPs, urgent words, verification codes and safe Smart fallback).
- Browser regression: passed at 390 × 844. Covers task creation, focus/pause/navigation/reload/resume/break/finish, reflections, daily bedtime controls, rituals, scrolling check-ins, notification roles, form drafts during refresh, routine/OTP routing, Gemini setup gating, deletion confirmation and keyboard-height layout. Navigation does not overlap content at 320, 390, 430 or 768px widths. No JavaScript runtime errors.
- Packaged standalone preview: opens offline and preserves a reflection after reload.
- APK: versionName `2.0-calm`, versionCode `3`, minimum API 26; signature verified using Android `apksigner` (v2 signature).
- Native emulator testing: attempted, but unavailable. The machine lacks usable emulator hypervisor support; the isolated software emulator did not complete boot. This is not an Android installation or alarm-delivery test.
- Live Gemini: not verified with a real API key. The Android app verifies a user-supplied key using model discovery plus a synthetic classification before enabling it. No key is included in the APK.
- Still requires physical Android checks: status/navigation bars and keyboard on the user's device, native listener permissions, actual background reminder delivery, vendor battery restrictions, TalkBack, cloud classification and its quality.

The completed browser and unit tests are engineering evidence. They are not participant interview or usability-study findings.
