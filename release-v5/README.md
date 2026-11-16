# AttentionTwin 5.0 — Saved messages and Important inbox

Install `AttentionTwin-5.0.apk` over the previous build without uninstalling. Application ID `com.attentiontwin.app`, versionCode 6, versionName `5.0-inbox`, Android 8+. This is a debug-signed demonstration APK, using the same certificate as the earlier builds. A normal upgrade retains the phone's Gemini key and existing settings. No key is included in this package.

## Notification fixes

The previous design snoozed a conversation's Android notification while waiting for AI. Updates to the same conversation could remain hidden, and one notification key was mistaken for one message. Smart/Hold now save distinct message observations in an encrypted local inbox before removing ordinary notifications. They do not create new conversation snoozes. MessagingStyle payloads contribute up to 25 recent messages, deduplicated by app, title, text and timestamp. Counts describe messages actually provided by Android, not a complete chat archive.

- **Local emergency checks:** the reported “there is a urgent fire” example bypasses removal immediately without Gemini, including in Hold mode and with empty custom safety words. VIP senders and possible verification codes retain local exceptions. Calls, alarms, navigation, media/service and protected ongoing alerts are left alone. Local safety matching is best effort, not an emergency monitoring service.
- **Promotion shield:** obvious offers, coupons, percentage discounts and flash sales are saved quietly before AI. “URGENT flash sale 70% off” is a promotion rather than a generic urgency match. The shield is enabled by default and also applies to Allow/Important apps while filtering is active; emergency/VIP exceptions still bypass it. Switch it off in protection preferences if desired. It cannot recognise every advertisement or language.
- **Casual messages:** greetings, repeated/typo greetings and simple gaming invitations are handled locally. They do not consume API calls.
- **Capture first, review later:** other Smart messages are grouped by app and conversation title. After 2.5 seconds, Gemini reviews batches of up to 12, with at most two requests executing concurrently. Larger bursts drain over successive batches. Only Smart messages with Gemini explicitly enabled are sent. Hold messages stay local.
- **Important inbox:** Home → Open important inbox, or Alerts → Important. Local important matches and confident important AI results appear here; unreviewed/API-failed messages remain visible as **Not reviewed**. Important results can also create a generic AttentionTwin notification opening this page, when notification permission is granted. Original cancelled notifications are not recreated as the source app.
- **Grouped saved inbox:** Alerts → Inbox shows distinct-message counts, conversations, message contents and the latest AI batch summary with its reviewed-message count. Expand a conversation to read its saved messages. Mark read clears the unread badge. Summaries can be mistaken and describe only that reviewed batch, not every locally filtered message.
- **Failure handling:** no network, invalid responses or disabled AI leave captured content available for manual review. Retry unreviewed sends only messages whose current app role is Smart. If local capture/encryption fails, the original is left available and a diagnostic is shown. No silent discard on API failure.
- **History and tests:** History is event metadata; Inbox is actual captured content. Test messages remain separate from real activity. Preview checks local rules without calling Gemini. Send test alert in the APK exercises the listener and optional cloud batch path.

Inbox titles, text and summaries use AES-GCM with an Android Keystore key that does not require per-use device authentication. Retention is at most 500 messages for seven days. Erase local data clears the inbox too. App/package labels, timestamps and decision metadata remain ordinary local database metadata. Gemini receives app name, title and up to 500 characters per message, plus your classification preferences, only after explicit setup/consent. It is not trained from scratch.

## Keyboard layout

The native window now owns keyboard space once. Android 11+ uses explicit IME insets; older Android versions use system resizing. The WebView and scrollable content fill the remaining height, the focused field scrolls into view, bottom navigation hides while typing, and **Done typing** dismisses the keyboard. This removes the former double-resize/large blank-gap layout. Planner and settings drafts remain intact while typing and refreshing data.

## Scrolling goals and permissions

Home → Set scrolling goals & blocking. Set separate Instagram/YouTube goals or a combined goal, select **Block the feed with a full-screen pause**, turn on automatic protection, accept local monitoring once and save. Enable **AttentionTwin Scroll Guard** in Android Accessibility settings once. Notification Access and Accessibility are separate permissions. Existing consent is retained when disabling/re-enabling protection, and enabled Accessibility access no longer redirects you through setup again. The notification permission dialog is not automatically repeated after denial; retry from Preferences or Android settings.

Whole-app foreground polling backs up accessibility events every two seconds. Usage is saved every five seconds and immediately at a blocking boundary. Reopening a watched app after reaching its goal, including after service restart, returns to the pause screen. The overlay offers Leave this app or an explicitly confirmed five-minute extension. Whole-app blocking includes normal YouTube videos and other Instagram screens; experimental Reels/Shorts-only detection remains for reminders. Previously unobserved usage is not imported. Daily totals reset at local midnight.

Android controls permission grants and can revoke them or stop services. AttentionTwin cannot guarantee permanent access, block apps after force-stop, or bypass manufacturer battery restrictions. The connection diagnostic shows when monitoring is unavailable. This is an accessibility pause overlay, not device-owner app suspension. For a sideloaded app Android may require App info → Allow restricted settings before enabling Accessibility.

## Upgrade checks on your phone

1. Upgrade without uninstalling. Confirm Alerts reports Notification Access connected. Apply Smart to all apps if desired; choose whether to retain Important app exceptions. Make filtering active throughout the day or start a study session.
2. With WhatsApp set to Smart and a non-VIP sender, send hello, a promotional offer, an unknown practical request and the exact fire example. Read distinct saved messages in Inbox; fire should remain available and appear in Important. Unknown messages use Gemini if enabled, otherwise Not reviewed. Burst several messages and inspect the latest batch summary. These checks require another consenting sender or the labeled Test tools.
3. Open reflection and test fields with your actual keyboard. Verify no blank gap, visible typing and Done typing.
4. Set an Instagram one-minute goal in block mode. Confirm Scroll Guard is Connected, use Instagram for a minute, leave/reopen it and confirm the pause returns. Revisit goals: access already enabled should not open Settings again.

Android posts alerts before a notification listener acts, so a brief banner or sound may still occur. Earlier snoozes from older builds remain on Android's release schedule; the listener attempts to recover their available content on reconnect, but cannot force early restoration. Privacy settings may hide message text from Android, and notifications never posted by the source app cannot be captured.

## Included deliverables

`offline-demo.html` is a standalone UI demonstration; phone filtering and enforcement require the APK. `screenshots/` contains synthetic browser test views. `VERIFICATION.md` records completed checks and physical-device limitations. The full project archive contains source, tests and earlier assignment materials; no real participant interviews or usability sessions were fabricated.

Tech stack: React 18, TypeScript, Vite, Lucide icons, Capacitor 8, Java Android services, SQLite, Android Keystore AES-GCM, Gemini REST API, JUnit/Robolectric and Playwright/Edge.
