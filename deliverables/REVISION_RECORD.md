# Revision record

These revisions follow engineering checks and design inspection. They are not findings from participant interviews or usability tests.

| Decision | Before | Finding and evidence | After |
|---|---|---|---|
| Study planning | Original Today offered fixed focus-duration buttons and a list of daily break times | Source inspection showed no named intention or explicit study-to-break completion flow | Study now captures a task, focus duration and break duration, then links timer, break and reflection |
| Reflection | Original Insights primarily showed notification outcomes and estimates | Design review found that this did not support a user's qualitative reflection on a study habit | Reflect now offers optional mood and note, with counts explicitly limited to completed planned blocks |
| Accessible form names | New duration selects inherited text from nested labels and option content | Exact-name browser lookup failed during automated UI testing | Explicit accessible names added; repeat walkthrough passed |
| Navigation | Six items inherited a five-column navigation style | Mobile screenshot showed Settings wrapping onto a second row | Six-column navigation; updated screenshot shows all six items on one row |
| Real activity | Browser test events used source=real; Today used the full history | Source review found simulated activity could affect real counts and old events could appear as today's observations | Browser test events marked test; Today observations and deferred counts filter the local date; Live includes test events |
| Data removal | New wellness alarms existed separately from the original clear-data action | Integration review found clearing original data would leave reminders scheduled | Global clear-data also cancels all three wellness reminder types and removes wellness storage |

`wireframes.html` shows a reconstruction of the original Today design alongside the new Study and Reflect layouts. It is a digital drawing guide, not a claim of hand drawing. `screenshots` shows the revised working interface.

## Participant-driven revisions to complete after real testing

| Revision | Participant evidence | Before | After | Follow-up result |
|---|---|---|---|---|
| User finding 1 | Pending | Pending | Pending | Pending |
| User finding 2 | Pending | Pending | Pending | Pending |
