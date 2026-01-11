# Edge Cases and Boundary Testing Checklist

This document lists edge cases and scenarios to validate across Logger app features. Use it for manual exploratory testing and to derive automated unit/UI/integration tests. Authentication is not part of this application.

## 1) Splash & Navigation
- First launch: splash renders correctly, navigates to Home within expected time.
- App background/foreground: splash doesn’t reappear unexpectedly.
- Rapid navigation (Home ↔ History ↔ Export): no stale state or crashes.
- Back stack behavior: back from nested screens returns to expected screen.
- Dark mode: colors/contrast legible; icons visible.
- Large font/accessibility: text truncation/overflow handled.

## 2) Home Screen (Dashboard of Today’s Standups)
- Empty state: no entries for today shows friendly empty UI.
- Single entry: layout spacing/margins look correct.
- Many entries: LazyColumn performance; smooth scroll.
- Long text fields (yesterday/today/blockers): wrapping without overlap.
- Special characters/emoji/non-Latin scripts rendering.
- Team member name presence: when `teamMember` is present use `name`; fallback when null/missing.
- Slow network: loading indicator persists; no duplicate requests.
- Network error: error message visible; retry works.
- Pagination (if applicable): load more available and stops at last page.
- Date boundary: today at timezone edges; device timezone change.
- Duplicates: same member multiple entries; ensure deterministic order.

## 3) History Screen
- Title displays as "History".
- Date filter:
  - Valid ranges (single day, week, month).
  - Start after end (invalid) handled gracefully.
  - Leap day (Feb 29), month/year boundaries.
  - Timezone changes while filtering.
- Empty results for a date range show friendly empty state.
- Large ranges: pagination; load more triggers only when canLoadMore.
- Team member name display from `teamMember.name`; fallback if missing.
- Error states per page load and on subsequent pages; partial data recovery.
- Sorting order consistency (newest first/defined order).
- Null/partial fields in entries (blockers null, missing updatedAt).

## 4) Export Screen
- Initial state: selected date defaults; loads entries for date.
- Export with:
  - No entries (file created with header only or warning?).
  - Single entry.
  - Many entries.
  - Special characters/newlines in fields.
- File system constraints:
  - Permission denied from SAF.
  - Disk full / write failure.
  - Existing file name collision (overwrite vs. create new).
  - Invalid URI / resolver returns null stream.
- Markdown/CSV content:
  - Correct headers.
  - Proper escaping of commas/newlines/markdown special chars.
  - Time/editedAt formatting; nulls handled.
  - Member name sourced from `teamMember.name`.
- Long-running export cancellation (app in background, process death).

## 5) Submit Standup (SubmitStandup Feature)
- Validation:
  - Required fields empty (yesterday, today).
  - Only whitespace.
  - Max length boundaries.
- Network conditions:
  - Offline at submit; returns clear error; retry on reconnect.
  - Timeout/slow network: loading indicator; user can cancel.
  - 409 conflict (duplicate entry): maps to user-friendly message.
  - 5xx server errors and malformed JSON handled.
- After success:
  - Fields reset; success snackbar/toast shown.
  - New entry visible in Home and relevant date in History.
  - Team member link maintained.

## 6) Team Members (Roster/Caching)
- No members returned from API: empty state.
- Single vs. many members: scrolling performance.
- Missing fields: name/email/id absent; fallback rendering.
- Special characters in names/emails.
- Cache behavior:
  - Cache hit avoids network.
  - Cache cleared/corrupted: app recovers and refetches.
  - Cache updated when server returns changes.

## 7) Data & Network Robustness
- API returns partial/malformed data:
  - Missing `teamMember` object in standup entry.
  - Unexpected types (string for int).
  - Null blockers/updatedAt.
- Retrofit/OkHttp errors: SSL, DNS failure, timeout.
- Retries: ensure no duplicate UI states or duplicate items.

## 8) UI/Compose Stability
- Recomposition under rapid list updates: no flicker or index mismatches.
- Remember/rememberSaveable correctness: selected date preserved on rotation.
- LazyColumn key stability: consistent keys to avoid item reordering.
- Performance: avoid jank with long lists; profiler checks.

## 9) DataStore/Preferences Management
- Store/get/delete/clear operations:
  - Null keys or missing keys handled.
  - Type mismatch prevented.
  - Disk full exceptions; graceful error path.
- Flow updates observed by UI via `collectAsStateWithLifecycle`.

## 10) Localization & Formatting
- Different locales: date formatting of standupDate and editedAt.
- Right-to-left languages (if applicable).
- Number/time formats; 24h vs 12h.

## 11) Miscellaneous & Resilience
- App rotation on all screens: state and scroll position preserved.
- Low memory: activity recreation; ViewModel state restoration via SavedStateHandle.
- Background restrictions: long export or network during Doze.
- App upgrade: schema/model changes do not crash; migration path.
- Uninstall/reinstall: clean state; onboarding flows consistent.

---

Test Data Suggestions:
- Dates: 2026-01-08, 2026-01-10, leap day 2028-02-29.
- Members: names with emojis (e.g., "Mona 😊"), long names, missing emails.
- Text fields: very long paragraphs; lines with commas/newlines; markdown characters (*_#`).

Execution Tips:
- Use dependency injection to mock repositories/use cases in UI tests.
- For unit tests with coroutines, set/reset Dispatchers.Main and use Standard/UnconfinedTestDispatcher.
- Prefer Turbine for Flow assertions and Robolectric for Compose UI where Android main looper is required.

