# AttentionTwin: Digital Wellness App

Project 2 · Working Android prototype · 16 November 2026

## Status and authorship

This project extends https://github.com/rakshit-ladda/casecade. The original app supplies Android notification access, app roles, local notification history, focus rules, safety exceptions and experimental estimates. This iteration adds a study planner, planned breaks, Android wellness reminders, a bedtime checklist, voluntary scrolling check-ins and a private reflection journal. The optional original Lab remains available in Settings.

**Research status:** No interviews or participant usability sessions have been conducted in this development task. The problem statement is a design hypothesis. The supplied research sheets are blank templates, not research evidence. Automated checks do not replace testing with five to eight consenting users. The wireframe sheets are digital drawing guides; students must make their own hand drawn wireframes if the assignment requires literal drawings.

## Problem statement

Students studying with their phones need a simple way to decide what to study, protect a manageable block of time, and take a planned break without feeling punished when they pause. Our desired improvement is a clear, controllable path from choosing one task to finishing a study block and reflecting on what helped. Validate the frequency and importance of this difficulty in interviews before presenting it as a confirmed user finding.

The core problem is interrupted study and missed breaks. Bedtime planning and voluntary scrolling check-ins are supporting features, not separate claims of solving every digital wellbeing problem.

## Existing alternatives and design gap

These are conceptual alternatives to discuss with participants, not claims based on a current market survey:

| Alternative | What it can help with | Gap this design explores |
|---|---|---|
| A phone countdown timer | Tracking a block of time | Linking a named study intention with an explicit break and reflection |
| Android notification settings | Choosing alerts at the operating system level | Explaining chosen app roles alongside a study plan |
| A paper planner or journal | Planning and reflection | Providing an interactive timer and voluntary reminders |
| The original AttentionTwin app | Notification decisions and simple focus durations | A complete study-to-break flow and a journal without attention scores |

## Requirements and acceptance criteria

| ID | Requirement | Acceptance criterion |
|---|---|---|
| R1 | Plan one study task | A user enters up to 100 characters and chooses focus and break durations |
| R2 | Control the session | Start, pause, resume and end are visible; reload preserves the remaining time |
| R3 | Make breaks explicit | Completed focus reveals a break action; break completion reveals Finish session |
| R4 | Preserve user choice | Existing active focus cannot be silently overwritten by a new study plan |
| R5 | Audit notifications | Settings lists installed launcher apps and lets users choose Allow, Always allow, Block in focus or Smart estimate |
| R6 | Wind down | User chooses bedtime and lead time, schedules or cancels the next reminder, and checks daily rituals |
| R7 | Scroll intentionally | User starts or cancels a check-in timer; no cross-app usage monitoring |
| R8 | Reflect without shame | Local journal records mood and optional note; no streak loss, ranking or guilt messages |
| R9 | Protect privacy | No wellness account or cloud service; erase cancels wellness reminders and removes journal state |
| R10 | Provide accessible controls | Named form fields, visible focus, text labels, large wellness controls and reduced-motion support |
| R11 | Work offline | Android assets are bundled; journal and rules do not require a laptop backend |
| R12 | Distinguish evidence | Test and synthetic events stay separate from real device statistics |

## Core prototype flow

Open app → complete or skip optional notification setup → Wellness → Study → name a task → choose focus and break minutes → Start → Pause/Resume if needed → study block completes → Take break → break completes → Finish session → Reflect → save a mood and optional note.

Additional flows: Settings → app importance → choose a role; Wind down → choose bedtime → set next reminder → check rituals; Check-in → choose interval → start reminder → acknowledge the check-in.

The Android APK is the interactive prototype. `offline-demo.html` demonstrates the linked screens in a browser without a server. Browser preview does not generate Android notifications or control other apps. Use a one-minute study and break for demonstrations, then choose a normal duration for actual use.

## Design decisions

The study intention appears before the timer so a session has a purpose. Breaks require a deliberate action rather than automatically starting another study block. Paused sessions preserve seconds. Completing a block records its planned duration once; stopping early earns no completed-block minutes. Counts describe completed planned blocks, not measured engagement or total device usage.

The journal replaces performance judgements with an optional mood and short note. Completed blocks and check-ins are capped at 365 records each; reflections retain at most 90 days of entries with one entry per local calendar date. Saved notification previews remain off by default in the existing app. Bedtime checklist state resets on the next local date.

Notifications use Android's inexact alarm API, which avoids an exact-alarm permission. The user may deny notification permission and still use timers and journals. A persistent notice explains that notifications are off. Future pending wellness alarms are restored after reboot or an app update; missed alarms are not replayed. The bedtime control schedules one reminder; it does not imply an automatic daily schedule.

## Interview procedure: 3–5 participants

Use participant IDs P01–P05. Obtain consent; explain that participation is voluntary, participants may skip questions or stop, and the tool is being tested rather than the person. Do not ask for names, message contents, grades, passwords or detailed device histories. Avoid recording audio unless separately agreed. Use `interview-summary.csv` for anonymized notes.

Ask about existing behaviour before showing the concept:

1. Tell me about the last time phone interruptions affected a study session.
2. What were you trying to do, and what did you do when interrupted?
3. How do you currently plan breaks? What happens when you miss one?
4. Which tools or settings have you tried? What worked or felt frustrating?
5. How do you decide which alerts matter while studying?
6. What would make a study aid feel supportive rather than controlling?

Afterwards, group repeated behaviours and exceptions, quote only with permission, count participants supporting each theme, and revise the hypothesis. Do not fill blank rows with invented responses.

## Usability procedure: 5–8 consenting users

Use IDs U01–U08, obtain consent, and let users stop without explanation. Use demo text and invented study tasks. Observe rather than coach. Stop after three minutes per task or when a participant asks to stop. Record completion as independent, assisted or not completed; record confusion and optional feedback. Do not collect actual notification contents or personal browsing histories.

| Task | Scenario | Success criterion |
|---|---|---|
| T1 | You want to revise chapter 3, then rest. Create a one-minute study session with a one-minute break. Pause, resume, finish the block and take the break. | Participant completes the focus-to-break flow independently |
| T2 | You want to go to bed at 23:00 and put your phone away beforehand. Plan a reminder 30 minutes before and check one ritual. | Correct bedtime and lead time, visible reminder confirmation, checked ritual |
| T3 | After some scrolling, you want a reminder to check in and to note what helped today. Start a one-minute check-in, acknowledge it, and save a reflection. | Check-in acknowledged and reflection visible in recent entries |

Use `usability-results.csv` for actual observations. Calculate independent task completion as independent completions divided by attempts for that task. Report the denominator. Do not treat assistance as independent completion. Add a second round after changes if possible.

## Verified engineering results

The Android build and the original 16 notification decision tests passed during development. Three new model tests cover deadline recovery and pause state, completion deduplication, and midnight wind-down scheduling. A headless browser walkthrough at 390 × 844 checked study start/pause/reload/resume/completion/break, reflection save, bedtime configuration and checklist, mindful check-in, deletion, horizontal overflow and JavaScript runtime errors. These checks passed. Screenshots are in the submission pack.

The automated browser initially could not locate select controls by their exact accessible names. Explicit accessible names resolved this. Screenshot review also found that six navigation items inherited a five-column layout; the layout now has six columns. See `REVISION_RECORD.md` for the changes and limits of this evidence.

**Participant results: pending.** No completion percentage, interview theme or user quote is asserted. **Physical Android verification: pending.** An APK build and JVM unit tests do not verify notification permissions, alarm delivery, TalkBack, background restrictions or installation on a real phone.

## Limitations and next steps

Android may delay reminders under power restrictions. Force-stopping an app can prevent alarms until it is reopened. Notification access is optional for wellness but necessary for notification handling. The original listener reacts after a notification posts, so a banner can briefly appear; Android controls when snoozed originals return. Smart estimates are local heuristics, not validated causal or clinical claims. Optional Lab connectivity is separate from the offline wellness flow.

Test on a real Android 8+ phone: deny and grant notifications, run all reminders with the screen off, reboot with a future reminder, verify cancellation, review focus exceptions, use large text and TalkBack, and test app updates and data deletion. There is no claim of WCAG conformance from the present checks.

Complete the interviews, draw the wireframes on paper, recruit five to eight participants, record the actual observations, then make at least two revisions based on those findings. Keep this engineering revision record separate from participant-driven revisions.

## Build and provenance

Source base: https://github.com/rakshit-ladda/casecade. See `BUILD_INFO.txt` in the submission pack for the cloned commit, APK checksum and build details. This is a debug-signed demonstration APK, not a store release. Installing it over a differently signed AttentionTwin build may fail; uninstalling the previous build erases its local data, so only do that if you choose to discard it.
