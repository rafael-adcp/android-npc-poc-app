# Refactoring Plan — Sandi Metz Style

Applying OO design principles from POODR (Practical Object-Oriented Design in Ruby)
to this Kotlin/Android codebase. Only changes that solve a real, current problem.

**Sandi's core lesson:** don't abstract until duplication or pain forces you.
The wrong abstraction is more expensive than no abstraction.

---

## What we already did

### Session A — Slim down MainActivity (done)
- Extracted `NfcForegroundDispatch` and `IntentRouter`
- `MainActivity` went from 80 → 47 lines, every method ≤ 3 lines
- 47/47 tests pass

### What we tried and reverted
- **Extracted interfaces** (`TagReader`, `TagApi`, `TagStore`, `TagProcessing`) at every boundary
- Reverted because: no consumer needs the abstraction. The E2E tests wire real objects.
  Faking orchestrators just tests wiring, not behavior. YAGNI.

---

## Remaining sessions

### Session 1 — Break up long methods

**Goal:** Three methods are too long. Extract till each one reads like a sentence.

**`TagRepository.processTag()`** — 19 lines. Does API call + success entity + error entity + DB insert.

Extract into:
- `private suspend fun callApi(value: String): Result<TagResponse>` — API call + error catch
- `private fun buildSuccessEntity(value: String, response: TagResponse, now: Long): TagEntity`
- `private fun buildFailureEntity(value: String, error: Throwable, now: Long): TagEntity`
- `processTag` becomes: `callApi` → `fold` → `buildEntity` → `dao.insert`

**`HistoryScreen()`** — 61 lines. Has inline dialog, inline row rendering, inline empty state.

Extract into:
- `ClearHistoryDialog` composable (the `AlertDialog` block)
- `HistoryItem` composable (single row)
- `EmptyHistoryMessage` composable
- `HistoryScreen` becomes: list + button + dialog trigger

**`AppScaffold()`** — 35 lines. Has inline tab bar + content routing.

Extract into:
- `TabBar` composable
- Content routing in a `when` block or separate composable per tab

### Verify
- `./gradlew assembleDebug assembleDebugAndroidTest` — both APKs compile
- `./gradlew pixel2api31DebugAndroidTest` — all instrumented tests pass

---

### Session 2 — State hoisting ("Tell, don't ask")

**Goal:** `AppScaffold` takes a `TagViewModel` and *asks* it for state. Composables should receive data, not pull it.

### 2.1 Hoist state out of `AppScaffold`
Change signature to receive pre-collected state:
```kotlin
@Composable
fun AppScaffold(
    uiState: ReadUiState,
    history: List<TagEntity>,
    lastRawValue: String?,
    onTagReset: () -> Unit,
    onClearHistory: () -> Unit,
    debugEnabled: Boolean,
    apiBaseUrl: String
)
```
`MainActivity` collects flows and passes data down.

### 2.2 Add `@Preview` annotations
With state hoisted, previews become trivial — pass fake data, no ViewModel needed.

### Verify
- `./gradlew assembleDebug assembleDebugAndroidTest` — both APKs compile
- `./gradlew pixel2api31DebugAndroidTest` — all instrumented tests pass

---

### Session 3 — Move `TagDtosTest` to JVM

**Goal:** One test file is pure serialization logic with zero Android dependencies. Run it on JVM for speed.

- Move to `app/src/test/java/com/example/nfcpoc/data/remote/dto/TagDtosTest.kt`
- Remove `@RunWith(AndroidJUnit4::class)`
- Same assertions, runs in milliseconds instead of minutes

**Everything else stays in `androidTest`.** The E2E tests are the contract tests.

### Verify
- `./gradlew test` — JVM DTO tests pass
- `./gradlew pixel2api31DebugAndroidTest` — E2E tests pass

---

## What we're NOT doing (and why)

| Idea | Why not |
|------|---------|
| Extract interfaces at every boundary | No consumer needs substitutability. YAGNI. |
| Fake-based unit tests for ViewModel/Repository | Tests wiring, not behavior. E2E already covers this. |
| Move `AppDatabase` singleton to DI layer | Standard Room pattern. No pain, no gain. |
| Delete `ApiClient` singleton | Used by tests to create instances with custom URLs. Leave it. |
| Test object factories | Only 2 test files build `TagEntity` inline. Not enough duplication to justify. |

---

## Summary

| Session | Focus | Principle | Risk |
|---------|-------|-----------|------|
| A (done) | Slim MainActivity | Single Responsibility | Done — 47/47 pass |
| 1 | Break up long methods | Small methods (≤ 5 lines) | Low — pure extraction |
| 2 | State hoisting | Tell, don't ask | Medium — Compose wiring |
| 3 | Move DTOs test to JVM | Fast feedback | Low — one file moves |

3 sessions. Each leaves the app green. No speculative abstractions.

---

## Running Instrumented Tests Locally

A **Gradle Managed Device** is configured in `app/build.gradle.kts` (`pixel2api31`, API 31, Google APIs).
Gradle downloads the system image, boots the emulator, runs tests, and shuts it down — no manual setup.

```bash
# Managed device (no phone or pre-created emulator needed):
./gradlew pixel2api31DebugAndroidTest

# Or if you already have a running emulator / connected device:
./gradlew connectedDebugAndroidTest
```

First run downloads the system image (~1.5 GB) and is slow. Subsequent runs reuse it.
