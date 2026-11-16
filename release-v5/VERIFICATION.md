# Verification — AttentionTwin 5.0-inbox

Completed in this Windows workspace on 16 November 2026. APK application ID `com.attentiontwin.app`, versionCode 6, Android API 26 minimum, target 36. This is a debug-signed demonstration build.

## Passed

- `npm run build`: TypeScript compilation and Vite production bundling.
- `npx cap sync android`: production UI copied into the Android project.
- `gradlew assembleDebug testDebugUnitTest --no-daemon`: APK build and **72 JVM/Android-framework tests**, zero failures/errors. Includes 71 functional tests and the repository's one template assertion.
- 15 Node tests covering planner deadlines, journal migration, bedtime scheduling, local safety, greetings, promotions and capture-first routing.
- `tests/inbox.cjs`: keyboard-height layout at widths 320/390/430; focused field visible, no unused bottom gap, navigation hidden while typing, Done typing and text retention. Grouped 4-message/1-conversation counts, Important and Not reviewed visibility, real/test separation, unread/read updates, five tabs at four widths and dark Important inbox.
- `tests/redesign.cjs`: planner, navigation/reload persistence, focus-to-break flow, reflections, bedtime, manual scrolling check-in, settings drafts, notification audit, local routing, Gemini setup gating, four widths, keyboard-height layout and deletion.
- `tests/protection.cjs`: throughout-day filtering, editable preferences, local test decisions, OTP exception, real/test separation, scrolling consent/settings/reload/pause, breathing phases, responsive layout and local erasure.
- `tests/goals.cjs`: all-app Smart and preservation of Important exceptions, local greeting handling without Gemini, visible Instagram/YouTube daily goals, block-mode configuration, saved consent/drafts, guided break steps/pause/resume, dark theme persistence, widget setup controls and both themes at four widths.
- `tests/offline-v5.cjs`: packaged standalone HTML opens with networking disabled and retains a reflection across reload.
- APK Signature Scheme v2 verification; certificate SHA256 `6cd76d9fa143e72b4f838d599d8a5ca93a352dc0e2b959ddfa2e357690e7b1ef`, matching earlier releases. Android SDK metadata confirms version 5.0-inbox / code 6. Artifact hash is in BUILD_INFO.txt.
- `git diff --check`: no whitespace errors.

## Android tests relevant to the reported bugs

`NotificationFlowTest` runs the real Java listener with simulated Android services and an injected software encryption key. It verifies capture before cancellation, no new conversation snoozing, the exact urgent-fire sentence without custom words in Hold mode, VIP/OTP bypasses, obsolete release-pass isolation, local promotion handling for Allow apps, VIP substring protection, MessagingStyle message retention/deduplication, eight messages captured before one model batch, **14 messages drained over two batches of at most 12**, API failure retaining Not reviewed content, and expired content never removed without capture. The model provider is a deterministic test fake, not live Gemini.

`InboxAndKeyboardTest` verifies encrypted stored payloads, duplicate counting, test separation/clearing and modern keyboard inset application exactly once. Its key is a software test key: hardware Android Keystore behaviour still requires a phone. The keyboard test exercises native sizing logic, while browser tests exercise the UI at a reduced viewport; neither reproduces a physical phone keyboard.

`AndroidWellnessTest` includes accessibility grant parsing with abbreviated component names and a quota test that restarts the service and detects Instagram foreground without a new window event. The blocking overlay reappears and no permission settings intent is launched in this simulation. Other tests cover restored settings/totals, notification extraction and overlay behaviour. Pure ScrollTracker/ScrollLimitPolicy tests exercise counters, midnight resets, extensions and limit selection. Existing DecisionEngine and GeminiPolicy tests remain passing; the new live path uses capture-first local rules plus batch review instead of the legacy single-alert policy.

## Limits still requiring a phone

No physical-device installation, actual WhatsApp/Instagram/YouTube interaction, real IME interaction, hardware Keystore verification, manufacturer background survival or live user-key Gemini classification was performed for this release. Browser screenshots use synthetic fixtures and are not evidence of real personal messages. No participant interviews or usability research were invented.

Notification listeners act after Android posts an alert, so initial banners/sounds can occur. Local rules cannot guarantee catching every emergency, advertisement or language. The inbox can only capture notification content Android actually provides. Earlier Android snoozes cannot be forcibly restored early. Accessibility access must be granted by the user; Android can revoke access or stop the service. A disconnected/force-stopped service cannot enforce a quota. See README.md for phone verification steps and setup.
