# AttentionTwin 4.0 — Daily goals, scrolling pause, all-app Smart, dark theme

Install `AttentionTwin-4.0.apk` over the previous build. Do not uninstall first: a normal update retains the encrypted phone key, journal and existing settings. Application ID `com.attentiontwin.app`, versionCode 5, Android 8+; debug-signed demonstration APK.

## Smart and Hold for all apps

Open **Alerts → Apps → Apply to every app**. Choose Smart, Hold or Allow and tap the matching Apply button. By default, Important apps keep their existing exception, and filtering resumes throughout the day. Uncheck “Keep apps I marked Important” to include those apps too. Android also uses this as the default for newly installed or previously unseen apps. Individual choices can be changed afterwards. VIP contacts, safety words, calls, alarms, navigation and protected system notifications retain their exceptions.

**Hello fix:** simple greetings such as “hello”, “hi”, “hey bro” and “kaise ho” now wait under a local Smart rule. This does not depend on Gemini, a valid key, an available model or a free classification queue. The app still checks Important/Allow roles, filtering scope and safety exceptions first. A VIP saying hello is intentionally allowed. A greeting containing extra practical/urgent content is not blindly matched to the greeting-only rule.

The listener reads the newest MessagingStyle message rather than combining older messages that could contain an expired urgent word or verification code. A content-and-timestamp fingerprint prevents a new message from accidentally using a prior conversation’s one-time release pass. Other Smart messages still use Gemini when enabled; unrecognised text without Gemini, unavailable API calls, invalid responses or queue overload leave alerts available. Exact reasons appear in History. Test → **Greeting: hello → Preview the real decision** provides a quick check. **Send test alert** additionally exercises the Android listener when permission is granted.

Android posts a notification before a listener can snooze it. A brief banner or sound can occur before removal; eliminating every initial heads-up is not guaranteed. Hold delays notifications; it does not prevent opening an app. Android controls return times for snoozed originals. These limits remain even with working permissions and a valid key.

## Visible scrolling goals and actual pause screens

Home now shows a **Scrolling goals** card near the top. Tap **Set scrolling goals & blocking**.

1. Choose separate Instagram and YouTube daily goals, or an optional combined daily goal. The 1-minute option is available for a quick enforcement check. No limit/Off means no daily enforcement for that goal.
2. Choose **When a daily goal is reached → Block the feed with a full-screen pause**. Reminder-only remains available as a separate choice.
3. Turn on **Automatic scrolling reminders**, accept the local monitoring disclosure and save goals/protection.
4. Open Android Accessibility settings and enable **AttentionTwin Scroll Guard**. Notification Access cannot enable scrolling detection. For a sideloaded APK, Android may require App info → Allow restricted settings first. The app shows Connected, Needs access, Not enabled or Paused today.
5. Return to Instagram or YouTube. At the applicable daily goal, a full-screen accessibility overlay covers the feed and intercepts interaction with it. Choose **Leave this app**, or **Add 5 minutes → Confirm 5-minute extension**. The pause returns when that extension ends, including when reopening a watched app whose goal is already reached.

The pause screen is part of the explicitly enabled Accessibility service, not an Android device-owner lock or OS-level app suspension. It can be disabled in Android settings. It does not cover other apps. Its content scrolls for smaller displays and larger text. If Android rejects the overlay, the service attempts to return to Home and records an error in diagnostics. Native device behaviour still needs confirmation.

Blocking always uses whole-app mode for reliable detection, so normal YouTube videos and other Instagram screens count too. Experimental Reels/Shorts-only matching remains available for reminder mode; resource IDs can change. Only foreground time observed by the connected, enabled service counts. Prior phone usage is not imported. Daily totals reset at local midnight. Daily counters pause while the full-screen blocking screen is shown; leaving an app does not erase its daily usage. Five-minute extensions use monotonic elapsed time while the service is alive, so changing the wall clock does not extend the allowance indefinitely. Connection and observed totals are visible in diagnostics. Manufacturer battery restrictions or force-stopping the app can stop monitoring; a disconnected service cannot enforce goals.

Use **Show a test blocking screen** to inspect the pause controls without reaching a goal, or save a 1-minute goal and use a watched app to check the complete flow. A manual one-off reminder is still available below the automatic controls.

## Dark theme and home-screen widget

- The header’s moon/sun button changes the app theme immediately. Preferences → Theme & home widget offers Light, Dark or Follow phone theme. Selection persists across navigation and reopening. Critical goal-label and timer-text contrast is checked automatically.
- Preferences → **Add home-screen widget** requests pinning through your launcher. If unsupported, long-press Home → Widgets → AttentionTwin. The widget shows local daily usage/goals and notification-rule status, with shortcuts to Focus and Scroll goals.
- The widget’s own **Light widget / Dark widget** button changes its appearance independently of the app. Native reminder and blocking overlays also support the app’s explicit dark theme.
- Launcher confirmation, placement and supported sizes are controlled by Android and the launcher. Widget creation/theme rendering is tested in Android framework simulation, not on a physical launcher here.

## Better planned breaks

Focus now includes **Plan a better break** with breathing, eye rest, guided gentle stretching or water/a little walk. The chosen activity is saved locally. During a planned break, its guide appears prominently above the timer, follows the break’s elapsed time and pauses/resumes with it. Breathing uses 4 seconds in/6 out; stretching advances through gentle steps. Follow at a comfortable pace. These are optional wellness prompts, not medical recommendations.

Study planning, focus/break persistence, daily bedtime rituals, reflections, notification audit, Gemini key encryption and local data erasure remain available.

## Privacy and verification

The Accessibility service observes foreground packages, scroll events and elapsed time. Optional feed detection reads resource identifiers only. It does not read accessibility screen text, passwords, messages, images or typing, and sends no accessibility data to Gemini. Notification classification is separate and may send the notification sample/content to Google only when Gemini is explicitly enabled. No key is included in the APK or source archive.

See `VERIFICATION.md` for exact automated checks and physical-device limits. No phone was connected to ADB in this workspace. Live user-key Gemini responses, real WhatsApp snoozing, cross-app overlays and launcher behaviour have not been observed on the user’s phone. Framework simulations and browser walkthroughs are clearly distinguished from real-device tests; no participant studies or device results have been invented.

Technical references: [Android AccessibilityService](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService), [Android widget manager](https://developer.android.com/reference/android/appwidget/AppWidgetManager), [Robolectric framework simulation](https://robolectric.org/getting-started/).
