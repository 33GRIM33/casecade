# Verification — AttentionTwin 3.0-scrollguard

Built in this workspace on 16 November 2026. Android versionCode 4; same application ID as version 2. Debug-signed demonstration APK.

## Automated checks passed

- `npm run build`: TypeScript compilation and Vite production build.
- `npx cap sync android`: packaged the current web assets into Android.
- `gradlew assembleDebug testDebugUnitTest --no-daemon`: APK assembly and 35 JVM tests, zero failures/errors. Includes 16 existing notification decision tests, 11 Gemini policy tests, 7 new ScrollTracker tests and 1 template test. ScrollTracker covers foreground accounting, interval/repeat, screen-off and clock jumps, brief interruptions, reset after 30 seconds away, snooze, and daily reset/restoration.
- `node --experimental-strip-types --test tests/wellness.test.ts tests/notifications.test.ts`: 11 tests, all passed. Focus deadline, pause-related bounds, persisted migration, completion idempotence, next wind-down, roles, VIP/OTP/critical exceptions and local routine fallback.
- `node tests/redesign.cjs`: browser walkthrough passed for planner tasks, focus/pause/navigation/reload/resume/completion/breaks, journaling, daily bedtime, manual reminder, settings drafts across refresh, sample routing, Gemini setup gating, deletion and layout. Tested at widths 320/390/430/768 and a short keyboard-height viewport.
- `node tests/protection.cjs`: browser walkthrough passed for throughout-day settings, drafts surviving background refresh, local preview/OTP, test versus real views, Scroll Guard disclosure/consent, saved settings and reload, pause/resume today, breathing phases/completion, data erasure and responsive layout.
- `node tests/offline-v3.cjs`: packaged inline HTML worked with network disabled and retained a saved journal entry after reload.
- Android `apksigner verify --verbose --print-certs`: signature valid (APK Signature Scheme v2); the certificate SHA-256 matches version 2: `6cd76d9fa143e72b4f838d599d8a5ca93a352dc0e2b959ddfa2e357690e7b1ef`.
- Android `aapt dump badging`: application ID `com.attentiontwin.app`, versionCode 4, versionName `3.0-scrollguard`, minSdk 26, targetSdk 36.

Browser walkthroughs run in headless Microsoft Edge with Playwright. Screenshots are in `screenshots/`. The browser uses a clearly marked local fallback, not Gemini. These are automated functional checks, not consenting participant usability sessions.

## Physical Android checks still required

No connected physical device was available for live WhatsApp or Instagram/YouTube testing. The earlier software emulator attempt did not boot to a usable device; no native runtime success is claimed. The user’s private API key stays on their phone and was not available in the workspace. The successful JVM tests do not execute Android notification snoozing, accessibility delivery, overlays, system permissions or live Gemini inference.

Use the setup in README and confirm on the phone:

1. Upgrade without uninstalling. Existing Gemini configuration and planner should remain. Check notification access and listener connection.
2. Set WhatsApp to Smart, choose throughout the day, choose Focus-first, save. Run a casual sample preview and an urgent sample preview. They should show distinct classifications/reasons when Gemini responds. A successful preview is not proof that Android granted notification interception.
3. Receive actual WhatsApp casual, time-sensitive and VIP messages. Compare History with the notification shade. Update a previously held conversation with an important message and confirm it is re-evaluated. Check held originals return at their scheduled time.
4. Enable Scroll Guard after consenting; enable its Android Accessibility service. Check connection, show a test card, then use a 10-second interval in whole-app mode and stay in Instagram or YouTube. Confirm the reminder appears over that app; test all three actions. Check that leaving watched apps pauses accounting and 30 seconds away resets the continuous interval. Test screen-off, pause-for-today, reconnection and normal daily intervals.
5. Treat Shorts-only mode as experimental, and fall back to whole-app mode if resource IDs are unavailable. Check battery/autostart behaviour on the specific phone.

Android may briefly show/sound a notification before snoozing; AI can misclassify or fail; device battery restrictions can stop services. The release does not promise universal device behaviour or perfect importance detection. No interviews, device observations or participant results have been invented.
