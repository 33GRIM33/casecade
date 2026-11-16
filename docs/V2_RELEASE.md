# AttentionTwin 2.0 — calmer screens, clearer controls

16 November 2026. This release supersedes the first wellness prototype. Earlier presentation/report files describe that earlier iteration.

## Main features

- **Home:** a daily task plan, completed-task checklist, mood shortcuts, and direct links to study, notifications, bedtime and scrolling reminders.
- **Focus:** a named study task, chosen study duration and break duration, a visible countdown, pause/resume/end, an explicit break, and completion. The same session survives screen changes, reloads and app reopening. Full blocks count once; stopping early does not inflate completed minutes.
- **Alerts:** a searchable per-app audit. Allow and Important pass through, Hold snoozes during focus, and Smart uses the optional Gemini connection or conservative local rules. Calls, alarms, navigation, possible verification codes, configured VIP contacts and urgent words bypass the smart classifier. Inbox explains decisions. The Test screen supports harmless routine, important and verification-code samples.
- **Wind down:** a bedtime and lead time, optional daily native reminders, a cancel control, and a daily checklist.
- **Mindful scrolling:** a voluntary reminder started by the user, with a countdown, acknowledgement and cancellation. It does not monitor other apps or total screen time.
- **Reflect:** completed-block minutes, a seven-day chart, voluntary check-ins, an optional mood and note, and recent reflections. There are no rankings or streak penalties.

## Main fixes

The entry point no longer imports the old Lab and its global styles. A single cream-and-green stylesheet owns the new interface. Four navigation items sit in the application grid; they do not cover a fixed scrolling page. Android applies system-bar and keyboard insets once. Background device refreshes no longer overwrite the user's settings drafts or role changes.

The session state lives above the page components. Switching pages does not destroy or reset it. The timer clamps to its intended duration while waiting for the next clock tick. Legacy journal/session data migrates into the new data structure.

App-owned global Do Not Disturb from older versions is restored when the redesigned app opens, because it could silence even notifications marked Important. Selective snoozing provides the filtering in this version. The phone owner's independent OS DND settings can still silence alerts.

## Optional Gemini setup

On the Android phone, open the gear icon → Gemini smart filter. Enter your own Gemini API key, read and select the data-sharing consent, then tap Connect & verify Gemini. Next, set selected apps to Smart in Alerts. Do not put an API key in chat or commit it to source control.

The app lists models available to the key and verifies classification using a synthetic sample before enabling it. It stores the key encrypted with an Android Keystore AES-GCM key. The interface receives connection status, never the saved key. Removing the key or erasing local data deletes it. No API key ships in the APK.

Only Smart-app notifications without a local safety exception are eligible for Google processing. The request contains the app label (up to 100 characters), title (up to 120 characters), and body (up to 500 characters). API usage may incur charges and is subject to the account's quota and provider policies. Planner/journal text is not sent to Gemini.

Only a ROUTINE result with confidence at least 0.90 can be held. Important, uncertain, malformed, timed-out, overloaded and failed requests allow the alert. Network classification runs on a bounded background queue; the listener rechecks focus, app roles, exceptions and the active notification before applying a result. If Gemini is disabled, conservative local rules hold only obvious routine updates and leave uncertain content available. No model training is involved.

Implementation references: [Google's REST generation reference](https://ai.google.dev/api/generate-content), [structured classification output](https://ai.google.dev/gemini-api/docs/structured-output), and [model availability](https://ai.google.dev/gemini-api/docs/models).

## Verification

The redesigned production web build and Android debug APK build pass. All 16 original notification decision tests, six Gemini safety-policy tests and the original example JVM test pass. Eleven TypeScript tests cover session timing, migration, linked task completion, rule-based pass-through, VIPs, verification codes, uncertainty and wind-down calculations.

The automated browser walkthrough checks task creation, session start/pause/resume, navigation and reload persistence, focus-to-break completion, daily bedtime controls, rituals, scrolling reminders, reflection save, alert role edits, settings drafts surviving refreshes, routine/OTP test routing, Gemini consent gating, deletion and layouts at 320/390/430/768px widths plus a reduced-height keyboard scenario. It reports no JavaScript runtime errors.

Live Gemini inference has not been tested with a real key. Physical phone behavior, vendor battery restrictions and TalkBack still need device testing. The isolated emulator did not complete boot because usable hardware acceleration was unavailable; this release does not claim device verification. No participant interviews or usability results are fabricated.

## Phone setup and limits

Install the debug-signed APK on Android 8+. Grant notification access in Alerts to enable filtering. Grant notification posting permission for reminders. For precise timing, open gear → Precise reminder timing and allow Alarms & reminders if Android offers it. Set background battery use appropriately on devices that restrict it.

Without precise-alarm permission, reminders use Android's inexact scheduling. Android may delay them under power restrictions; force-stopping the app can stop delivery until the app is reopened. Daily bedtime reminders reschedule after delivery, reboot, time-zone changes and app reopening. Android may display a notification before the listener snoozes it. It controls when original snoozed alerts return; an app cannot force an original alert back early. Notification importance classification can be wrong: always mark essential apps Important.

The offline demonstration supports local planner, timer and journal interactions. Native permissions, notification interception and Gemini key storage require the APK. The Gemini controls are disabled in the browser preview.
