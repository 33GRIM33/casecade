# AttentionTwin 4.0 — verification

Built on 16 November 2026. Android application `com.attentiontwin.app`, versionCode 5, versionName `4.0-goals`, minSdk 26, targetSdk 36. Debug-signed demonstration build.

## Passed checks

- TypeScript compilation, Vite production build and Capacitor Android asset sync.
- Android APK assembly and 55 Android/JVM tests passed with zero failures/errors. This includes 45 pure/template tests and 10 Robolectric 4.16.1 framework/listener simulations using Android API 28.
- Android framework simulations exercise newest-message extraction from a real MessagingStyle notification; greeting classification with Gemini disabled and an unreadable stored key; global default roles/Important exceptions; applying all-app rules and resuming protection; full-screen goal overlay creation; leaving and clearing it; freezing usage while blocked; extension confirmation and reblocking after expiry; home-widget rendering and its light/dark toggle.
- Notification listener integration simulations run the real listener pipeline with a captured OS snooze call. They verify that an actual greeting fixture reaches snoozing/held history, VIPs and verification codes bypass it, and a new greeting cannot reuse a prior conversation’s release pass. Capturing the OS call does not prove that a particular phone will honour it.
- `node --experimental-strip-types --test tests/wellness.test.ts tests/notifications.test.ts`: 13 tests, all passed.
- `node tests/redesign.cjs`: existing focus/task/break, navigation/reload, reflection, daily bedtime, manual reminder, safety-draft persistence, sample routing, Gemini setup gating, erasure and responsive layout walkthrough passed.
- `node tests/protection.cjs`: protection settings, local preview/OTP, test separation, Scroll Guard consent/settings/reload/pause and breathing walkthrough passed.
- `node tests/goals.cjs`: all-app Smart with/without Important preservation; hello held without Gemini; dark-theme persistence; prominent goal entry; per-app goals/blocking configuration/consent/reload; draft safety; guided stretching steps and pause/resume; widget setup UI; layout at widths 320/390/430/768 in light and dark. Critical label/timer text contrast assertions pass.
- `node tests/offline-v4.cjs`: the packaged standalone preview worked with networking disabled and retained a saved journal after reload.
- `apksigner verify --verbose --print-certs`: APK Signature Scheme v2 verified. Certificate SHA-256 matches prior builds: `6cd76d9fa143e72b4f838d599d8a5ca93a352dc0e2b959ddfa2e357690e7b1ef`.
- `aapt dump badging`: package/version/minSdk/targetSdk match the release header. The APK hash is recorded in `BUILD_INFO.txt`.

Browser checks use Playwright/headless Microsoft Edge. Screenshots in `screenshots/` are browser UI captures, not native device screenshots. Simulated Android tests use Robolectric, not a physical device. No live Gemini key is used in these tests.

## Physical checks still required

ADB returned no connected devices. Live Gemini with the user’s key, actual WhatsApp interception, Android Accessibility delivery/window permission, force-stop/battery behaviour and the physical launcher remain unverified. The APK includes connection indicators, real-decision preview, a test notification and a test blocking screen to diagnose the phone without collecting private logs.

On the phone, upgrade without uninstalling; apply Smart to all apps; test a greeting from a non-VIP; inspect History and the notification shade. Enable Scroll Guard Accessibility, save a 1-minute daily goal in block mode, and stay in Instagram or YouTube until the pause appears. Check Leave, confirmed extension, expiry and reopening. Try the widget and theme on the actual launcher. Android may show/sound a notification before the listener acts, and an unavailable/disconnected service cannot enforce rules.
