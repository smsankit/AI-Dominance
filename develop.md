# Development Documentation

## Copilot Usage Summary
- Total interactions: 31
- Most valuable use: Fixing build errors by updating AGP and compileSdk versions, and guiding architecture and UI changes per wireframe and project standards.
- Least valuable use: Repeated requests for continuation and preview without actionable context.

## Detailed Log

### Interactions 1
**Prompt/Context:**
You are Android development expert or Android architect creating Android application initial setup.
Create a Android application structure based on MVVM with clean architecture.
Requirement:
MVVM with clean architecture should be follow.
Use latest Android technologies like Jetpack Compose, Navigation 3 etc
Use Kotlin with coroutine, Flow etc
Use Retrofit with Gson for Network call
Use Coin for Image Loading
Use Datastore to store data
Create separate core folder to create the common packages with appropriate classes like validation, dependency injection, exception, network, util and navigation
Use hilt for dependency injection
Add .github/copilot-instructions.md file in appropriate folder
Add AGENT.md file.
**Suggestion:** Project setup partially and prompt to type continue.
**Action:** Accepted
**Reasoning:** Get result as expected.

### Interaction 2
**Prompt/Context:** continue
**Suggestion:** Project setup completed.
**Action:** Accepted
**Reasoning:** Project setup as expected.

### Interaction 3
**Prompt/Context:** Make PreferencesManager methods generic for store, delete, get and clear, so that we can use it anywhere by passing key.
**Suggestion:** Proposed making PreferencesManager methods generic for key-based operations.
**Action:** Accepted
**Reasoning:** Improves reusability and aligns with clean architecture.

### Interaction 4
**Prompt/Context:** fix the build issues
**Suggestion:** Inspected project files to identify build issues.
**Action:** Accepted
**Reasoning:** Necessary for project to compile and run.

### Interaction 5
**Prompt/Context:** project sync, clean and run again. getting same issue. fix it
**Suggestion:** Investigated and planned to update AGP and compileSdk versions.
**Action:** Accepted
**Reasoning:** Required to resolve compatibility errors.

### Interaction 6
**Prompt/Context:** fix build issues
**Suggestion:** Inspected Gradle and dependency versions.
**Action:** Accepted
**Reasoning:** Needed for successful build.

### Interaction 7
**Prompt/Context:** fix below build errors: [AGP/SDK mismatch]
**Suggestion:** Update AGP to 8.9.1 and compileSdk/targetSdk to 36 in build.gradle files.
**Action:** Accepted
**Reasoning:** Direct fix for reported errors.

### Interaction 8
**Prompt/Context:** fix below error: [kotlin-dsl cache]
**Suggestion:** Investigate Gradle cache and wrapper properties.
**Action:** Accepted
**Reasoning:** Ensures build environment is correct.

### Interaction 9
**Prompt/Context:** continue
**Suggestion:** Awaiting actionable context.
**Action:** Rejected
**Reasoning:** No new instructions.

### Interaction 10
**Prompt/Context:** show preview of this screen
**Suggestion:** Planned to use Compose preview parameters.
**Action:** Accepted
**Reasoning:** Aligns with Compose best practices.

### Interaction 11
**Prompt/Context:** Add elevation in SubmissionCard view
**Suggestion:** Add elevation property to SubmissionCard composable.
**Action:** Accepted
**Reasoning:** UI improvement per wireframe.

### Interaction 12
**Prompt/Context:** View Missing standup color should be align with the wireframe.html dashbord screen
**Suggestion:** Update color in theme/Color.kt as per wireframe.
**Action:** Accepted
**Reasoning:** Ensures UI consistency.

### Interaction 13
**Prompt/Context:** 1 team member missing text and icon should be align horizontally
**Suggestion:** Use Row composable for horizontal alignment.
**Action:** Accepted
**Reasoning:** Matches wireframe design.

### Interaction 14
**Prompt/Context:** continue
**Suggestion:** Awaiting actionable context.
**Action:** Rejected
**Reasoning:** No new instructions.

### Interaction 15
**Prompt/Context:** Add toolbar in the HomeRoute.kt
**Suggestion:** Add TopAppBar composable to HomeRoute screen.
**Action:** Accepted
**Reasoning:** Required for navigation and UI consistency.

### Interaction 16
**Prompt/Context:** HomeRoute come under the toolbar, fix this
**Suggestion:** Place HomeRoute content below TopAppBar.
**Action:** Accepted
**Reasoning:** Corrects UI hierarchy.

### Interaction 17
**Prompt/Context:** toolbar color should be same as wireframe.html
**Suggestion:** Update TopAppBar color in theme/Color.kt.
**Action:** Accepted
**Reasoning:** Ensures design consistency.

### Interaction 18
**Prompt/Context:** create a Splash Screen as first screen of the app available in the 'wireframe.html'
**Suggestion:** Create SplashScreen composable per wireframe.
**Action:** Accepted
**Reasoning:** Required for app entry point.

### Interaction 19
**Prompt/Context:** SplashScreen content should be center align
**Suggestion:** Use Box/Column with Alignment.Center.
**Action:** Accepted
**Reasoning:** Matches wireframe layout.

### Interaction 20
**Prompt/Context:** Buttons width should be align with the wireframe designs.
**Suggestion:** Set button width using Modifier.width.
**Action:** Accepted
**Reasoning:** Ensures UI consistency.

### Interaction 21
**Prompt/Context:** In SplashScreen button width should be align with Wireframe.html design.
**Suggestion:** Set button width as per wireframe.
**Action:** Accepted
**Reasoning:** Matches design requirements.

### Interaction 22
**Prompt/Context:** Static text on the screen should be in  `res/values/strings.xml`
**Suggestion:** Move static text to strings.xml.
**Action:** Accepted
**Reasoning:** Follows Android best practices.

### Interaction 23
**Prompt/Context:** Static Text like "Daily Standup" in SplashScreen.kt should be define in res/values/strings.xml.
**Suggestion:** Move "Daily Standup" to strings.xml and reference in code.
**Action:** Accepted
**Reasoning:** Ensures localization and maintainability.

### Interaction 24
**Prompt/Context:** Capture all our instructions in the develop.md file. Use below format to add instructions in the develop.md
**Suggestion:** Document all interactions in develop.md as per provided format.
**Action:** Accepted
**Reasoning:** Required for project documentation and traceability.


### Interaction 25
Feature: GetTeamMember
**Prompt/Context:** Integrate endpoint /standups/teams/{teamId}/members in HomeViewModel; update data/domain/presentation; cache in shared preferences.
**Suggestion:** Added DTOs, TeamApiService, TeamRepositoryImpl, domain models, use case; extended PreferencesManager to save/get team members as JSON; updated HomeViewModel to fetch and save.
**Action:** Accepted
**Reasoning:** Implements feature end-to-end per clean architecture.

### Interaction 26
Feature: GetTeamMember
**Prompt/Context:** cache in GetTeamMembersUseCase and only call API when empty?
**Suggestion:** Implemented use case to check PreferencesManager cache first; if empty, fetch from repository, save, and emit.
**Action:** Accepted
**Reasoning:** Encapsulates caching in domain layer; reduces network calls.

### Interaction 27
Feature: GetTeamMember
**Prompt/Context:** Call GetTeamMembersUseCase on page load.
**Suggestion:** Invoked use case in HomeViewModel init, collecting results.
**Action:** Accepted
**Reasoning:** Ensures members are available early and cached.

### Interaction 28
Feature: GetTeamMember
**Prompt/Context:** Create and use Mapper instead of direct conversion.
**Suggestion:** Introduced TeamMemberDtoMapper in data/mapper and used it in TeamRepositoryImpl.
**Action:** Accepted
**Reasoning:** Centralizes mapping; improves testability.


### Interaction 29
Feature: SubmitStandup
**Prompt/Context:** Update SubmitStandupUseCase and related classes to new endpoint /standup-entries with given request/response.
**Suggestion:** Added request/response DTOs, domain model StandupEntryData, mapper; updated LoggerApi and StandupRepository/Impl; wired SubmitStandupViewModel to call use case and handle result.
**Action:** Accepted
**Reasoning:** Matches backend contract and returns created entry.

### Interaction 30
Feature: SubmitStandup
**Prompt/Context:** Use StandupEntryRequestData in submitStandupEntry() and update related classes.
**Suggestion:** Changed StandupRepository contract and StandupRepositoryImpl to accept the request model and map to API DTO.
**Action:** Accepted
**Reasoning:** Keeps model consistent across layers.

### Interaction 31
Feature: SubmitStandup
**Prompt/Context:** Move inline request-building code into a mapper.
**Suggestion:** Created SubmitStandupUiMapper in presentation layer and used it in SubmitStandupViewModel instead of inline construction.
**Action:** Accepted
**Reasoning:** Keeps ViewModel lean; improves reusability and testability.

### Interaction 32
**Prompt/Context:** Write testcases for `SubmitStandupViewModel.kt` and get them passing.
**Suggestion:** Added unit tests using JUnit, Mockito, Turbine, and kotlinx-coroutines-test; created `SubmitStandupViewModelTest.kt` with cases for initial roster load, validation errors, success reset + event, and error event.
**Action:** Accepted
**Reasoning:** Covers core ViewModel behavior aligned with MVVM and Clean Architecture.

### Interaction 33
**Prompt/Context:** Tests failing due to missing test libraries (unresolved references).
**Suggestion:** Add test dependencies: coroutines-test, turbine, mockito-core, mockito-kotlin, and JUnit to `app/build.gradle.kts`.
**Action:** Accepted
**Reasoning:** Required to compile and run unit tests for coroutines and Flow-based ViewModel.

### Interaction 34
**Prompt/Context:** Tests failing with "Method getMainLooper not mocked" when using `viewModelScope`.
**Suggestion:** Use Robolectric runner and set/reset `Dispatchers.Main` in tests; annotate tests with `@RunWith(RobolectricTestRunner::class)`, call `Dispatchers.setMain(UnconfinedTestDispatcher())` in `@Before` and `Dispatchers.resetMain()` in `@After`.
**Action:** Accepted
**Reasoning:** Provides Android main looper and proper dispatcher for `viewModelScope` in JVM unit tests.

### Interaction 35
**Prompt/Context:** Tests hanging/not advancing due to coroutines launched in `init {}` and `submit()`.
**Suggestion:** Switch to `UnconfinedTestDispatcher` and add `advanceUntilIdle()` in tests after ViewModel creation and actions to let coroutines complete.
**Action:** Accepted
**Reasoning:** Ensures deterministic progression of coroutines started by `viewModelScope.launch`.

### Interaction 36
**Prompt/Context:** Type mismatch and mock signature mismatches for use cases.
**Suggestion:** - Return a real `StandupEntryData` from `SubmitStandupUseCase` mock for success. - Stub `GetTeamMembersUseCase.invoke(teamId, page, size, isApiCallRequired)` with 4 parameters to match ViewModel usage.
**Action:** Accepted
**Reasoning:** Aligns mocks to actual domain contracts, preventing compilation and runtime failures.

### Interaction 37
**Prompt/Context:** Document testing setup and commands for Windows PowerShell.
**Suggestion:** Added Testing section to `develop.md` detailing dependencies, commands (`./gradlew test`, `./gradlew testDebugUnitTest`, and filtering), and best practices (Robolectric, Dispatchers.Main, Turbine, `advanceUntilIdle`).
**Action:** Accepted
**Reasoning:** Provides clear guidance for contributors to run and maintain tests reliably.

### Interaction 38
**Prompt/Context:** Update ExportViewModel tests for new state handling and error mapping.
**Suggestion:** Refactored tests to match updated ViewModel logic: initial loading, date change, load more, and error states.
**Action:** Accepted
**Reasoning:** Ensures tests reflect current implementation and validates critical paths.

### Interaction 39
**Prompt/Context:** Write test cases for ExportStandupEntryViewModel covering all branches.
**Suggestion:** Added tests for ExportStandupEntryViewModel: initial state, export success, failure, and CSV content verification.
**Action:** Accepted
**Reasoning:** Confirms ViewModel behavior for export feature, ensuring reliability.

### Interaction 40
**Prompt/Context:** Write test cases for HistoryViewModel covering all branches.
**Suggestion:** Added tests for HistoryViewModel: initial state, date filter change, pagination, and error handling.
**Action:** Accepted
**Reasoning:** Ensures HistoryViewModel functions correctly across scenarios.

### Interaction 41
**Prompt/Context:** Write test cases for HomeViewModel covering all branches.
**Suggestion:** Added tests for HomeViewModel: initial state, cached data use, API fetch, error handling, and refresh behavior.
**Action:** Accepted
**Reasoning:** Confirms HomeViewModel operates as expected in all situations.

### Interaction 42
**Prompt/Context:** Write test cases for StandupEntryViewModel covering all branches.
**Suggestion:** Added tests for StandupEntryViewModel: initial state, form validation, submission success, and error handling.
**Action:** Accepted
**Reasoning:** Ensures StandupEntryViewModel behaves correctly for entry submission.

### Interaction 43
**Prompt/Context:** Write test cases for TeamMemberItemViewModel covering all branches.
**Suggestion:** Added tests for TeamMemberItemViewModel: initial state, roster loading, and error handling.
**Action:** Accepted
**Reasoning:** Confirms TeamMemberItemViewModel functions properly for roster management.

### Interaction 44
**Prompt/Context:** Update today’s failing ExportViewModel tests and align with SubmitStandupViewModel test setup.
**Suggestion:** Set Dispatchers.Main via UnconfinedTestDispatcher, use Robolectric runner, advance coroutines with advanceUntilIdle, and stub use cases to emit success/error results covered by the ViewModel’s when branches.
**Action:** Accepted
**Reasoning:** Establishes deterministic coroutine execution and ensures branches are exercised, resolving NoWhenBranchMatchedException and assertion mismatches.

### Interaction 45
**Prompt/Context:** Fix four failing cases in ExportViewModelTest: initial load, date change, loadMore append, and error state.
**Suggestion:** Mock use case to return paged results for the selected date, toggle canLoadMore based on page size, clear state on date change before reloading, and surface error messages from Result.Error to uiState.error.
**Action:** Accepted
**Reasoning:** Aligns tests with expected ViewModel behavior, validates pagination and error propagation.

### Interaction 46
**Prompt/Context:** Write and stabilize testcases for SubmitStandupViewModel with success + reset fields + snackbar event, 409 conflict mapping, and roster count update.
**Suggestion:** Add tests verifying Submitted event triggers snackbar message, text fields cleared on success, map 409 error JSON to user-friendly message, and roster reflects team member size after getTeamMembers call.
**Action:** Accepted
**Reasoning:** Covers critical UX flows and domain error handling.

### Interaction 47
**Prompt/Context:** Add full coverage tests for HistoryViewModel and HomeViewModel mirroring ExportViewModel patterns.
**Suggestion:** HistoryViewModel: initial load, pagination, date filtering, empty and error states. HomeViewModel: cached vs API fetch, success state update, error propagation, and refresh behavior.
**Action:** Accepted
**Reasoning:** Ensures reliable behavior across view models with clean architecture flows.

### Interaction 48
**Prompt/Context:** Document Windows PowerShell test commands and best practices in develop.md.
**Suggestion:** Include commands for running targeted tests and global suites, and note using Robolectric, Dispatchers.setMain/resetMain, and advanceUntilIdle in tests.
**Action:** Accepted
**Reasoning:** Provides repeatable guidance for contributors to run and debug tests locally.
