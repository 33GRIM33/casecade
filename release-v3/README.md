# AttentionTwin 3.0 — Scroll Guard & clearer notification protection

Install `AttentionTwin-3.0.apk` over version 2.0. The application ID and signing certificate are unchanged, so existing preferences and the encrypted Gemini key remain on a normal upgrade. Do not uninstall first. This is a debug-signed demonstration build for Android 8 or later.

## Fix WhatsApp filtering

1. Open Alerts → Apps. Leave WhatsApp set to **Smart**.
2. In “When to protect your attention”, choose **Throughout the day** if you want rules to run without starting a study session. Otherwise, start a study session or use your scheduled focus windows. Save protection preferences. The filtering badge describes whether your rules are active; the connection card shows whether Android is connected.
3. Choose **Focus-first** to hold casual conversations and unclear/locked summaries. Urgent results and confident important results pass. **Balanced** lets uncertain results through and holds confidently casual/routine messages. Add optional importance preferences to tell Gemini what matters to you.
4. Keep necessary VIPs and safety words in “Your safety net”. A matching word or VIP overrides AI. An app marked Important or Allow always passes through. A Hold app delays everything except safety exceptions. Calls, alarms and navigation are untouched.
5. In Test, select WhatsApp, enter a harmless sample such as “hey, watch this meme”, then choose **Preview the real decision**. On Android, this uses the saved Gemini connection if enabled. A preview assumes filtering is active and never posts a notification. **Send test alert** additionally exercises the Android listener. Wait for its result under Test; real messages appear in History or Held.

The original prompt treated almost all personal requests as important and required 90% confidence before holding only ROUTINE alerts. The revised schema distinguishes URGENT, IMPORTANT, CASUAL, ROUTINE and UNCERTAIN. Focus-first holds low-confidence IMPORTANT (<75%) and successful unclear results; Balanced holds CASUAL/ROUTINE only at 60%+ confidence. Invalid responses, connectivity failures, quota errors and timeouts leave alerts available. The log now distinguishes those failures from classification decisions. No model retraining is required.

MessagingStyle, expanded text and inbox lines are read so WhatsApp is classified using available message content rather than just a generic collapsed summary. Smart mode reclassifies conversation updates even when earlier messages were held, allowing an important new reply through. Repeated updates reuse the held entry instead of multiplying the waiting count.

Android delivers a notification before a listener can snooze it. A banner or sound may occur before a decision, especially while waiting for Gemini. This application cannot promise to eliminate every initial heads-up. Snoozed originals return on Android’s schedule; “Request release now” creates a release digest and allows a returning original, but cannot pull an Android snooze forward. Rules do not override Do Not Disturb settings that you enabled yourself.

## Enable automatic Instagram / YouTube reminders

1. Home → **Scroll Guard**. Turn on automatic scrolling reminders, choose apps and an interval, read the local monitoring disclosure and agree, then save.
2. Choose **Open Android Accessibility settings** → **AttentionTwin Scroll Guard**, and enable it. This is separate from Notification Access and reminder notification permission. For a sideloaded APK, Android may first require App info → menu → Allow restricted settings. Enable this only if you trust this installed build.
3. Return to Scroll Guard and check **Connected**. Use **Show a test reminder card** to check the overlay. For an actual detection check, save a 10-second interval, open Instagram or YouTube and remain there for more than 10 seconds with the screen on.
4. Use **Whole apps** for reliable detection, including Reels and Shorts. This also counts normal videos and other Instagram screens. **Only Reels / Shorts** is experimental: it checks screen resource IDs, which Instagram/YouTube can change. If it fails to identify a feed, use whole-app mode. No claim of universal Shorts-only detection is made.

Automatic reminder cards provide Take a break, 5 more minutes, and Pause for today. Take a break returns to Home only when you tap it. An optional combined daily time reminder does not lock you out. Leaving watched apps for 30 seconds resets the continuous session. The daily totals, reminder count and observed scroll count appear in diagnostics. The service counts foreground time, so it also catches feeds whose scroll events are unavailable. The overlay does not require the separate “display over other apps” permission; it uses the explicitly enabled Accessibility service. If overlays are switched off or cannot be shown, a notification is attempted and needs notification permission. Manufacturer background restrictions can disconnect services; use the connection status and background battery settings to diagnose this.

The service observes foreground package transitions, scroll events and elapsed time. The optional feed matcher reads resource IDs only, with a bounded traversal. It does not read accessibility screen text, passwords, images, typed input or message bodies. Only aggregate daily watched time and counts persist locally. No accessibility data goes to Gemini. The accessibility permission can be revoked in Android settings at any time. Data erasure disables monitoring and removes its configuration and totals.

## Other features retained / added

- Study planner, linked tasks, focus/break countdowns, pause/resume and deadline persistence.
- Notification audit, per-app roles, VIPs and custom safety words; optional Gemini key encrypted with Android Keystore.
- Separate Held, History and Test views with explicit decision reasons. Notification scope and maximum hold duration outside timed sessions are configurable.
- Daily bedtime wind-down reminder and daily resetting ritual checklist.
- Optional one-off manual scrolling timer, retained separately from automatic monitoring.
- New one-minute guided breathing exercise: 4 seconds in, 6 seconds out, with reduced-motion support.
- Mood journal, weekly focus reflection, local data erasure, responsive cream/green interface.

## Verification and limits

See `VERIFICATION.md` for exact checks. Build success, JVM unit tests and browser UI walkthroughs are verified. The offline HTML is an interactive UI demonstration; it cannot monitor Android apps or call Gemini. A working key on your phone is not available in this development workspace, so live Gemini inference and physical WhatsApp/Instagram/YouTube behaviour have not been verified here. Device-specific confirmation remains necessary; automated UI results are not real-user usability studies.

## Technology

React 18 + TypeScript + Vite, CSS and Lucide icons; Capacitor 8 Android bridge; native Java NotificationListenerService, AccessibilityService, AlarmManager and SQLite; SharedPreferences and Android Keystore AES-GCM; direct Gemini REST API. The mobile wellness flows do not require the repository’s optional legacy backend.

Official API references: [Android AccessibilityService](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService), [Android notification listener](https://developer.android.com/reference/android/service/notification/NotificationListenerService), [Gemini thinking configuration](https://firebase.google.com/docs/ai-logic/thinking).
