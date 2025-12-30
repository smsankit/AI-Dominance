# Development Documentation

## Copilot Usage Summary
- Total interactions: 26
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

