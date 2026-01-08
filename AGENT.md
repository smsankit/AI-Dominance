# Project Agent Notes - Logger Android Application

## Quick Overview
**Logger** is a modern Android application built with **MVVM + Clean Architecture** using **Jetpack Compose**, **Navigation Compose**, **Hilt**, **Retrofit+Gson**, **Coil**, **Coroutines/Flow**, and **DataStore**.

**Package:** `com.example.logger`  
**Language:** Kotlin 2.0.21  
**Min SDK:** 24 | **Target SDK:** 36 | **Compile SDK:** 36

---

## Agent Behavior Expectations
- Do not refactor unrelated code.
- Do not introduce new dependencies without justification.
- Respect existing patterns and naming conventions.
- Prefer minimal, incremental changes.
- Run a full build before and after any non-trivial change.
- Keep edits localized; do not reformat files or reorder imports unless required by the change.
- Keep public APIs stable; if you must change them, update all usages and tests in the same change.
- Prefer constructor injection; avoid singletons/static state for app logic.
- Do not modify release config (minify/proguard/signing) unless explicitly requested.

## Architecture Layers

### Structure Overview
```
com.example.logger/
├── LoggerApp.kt              # @HiltAndroidApp entry point
├── MainActivity.kt           # Single Compose activity
├── core/                     # Cross-cutting concerns
│   ├── di/                   # Hilt modules (@Module + @InstallIn)
│   ├── datastore/            # Generic PreferencesManager
│   ├── network/              # Retrofit + OkHttp setup
│   ├── exception/            # Custom exceptions
│   ├── validation/           # Input validators
│   └── util/                 # Extensions & helpers
├── domain/                   # Business logic (pure Kotlin)
│   ├── model/                # Domain entities
│   ├── repository/           # Repository interfaces
│   └── usecase/              # Business use cases
├── data/                     # Data layer
│   ├── remote/               # API services & DTOs
│   ├── mapper/               # DTO ↔ Domain mappers
│   └── repository/           # Repository implementations
├── presentation/             # UI layer (Compose)
│   ├── navigation/           # Navigation graph
│   ├── splash/               # Splash screen
│   ├── home/                 # Home feature
│   ├── dashboard/            # Dashboard feature
│   ├── roster/               # Roster feature
│   ├── history/              # History feature
│   ├── submitstandup/        # Submit standup feature
│   ├── missing/              # Missing entries feature
│   ├── export/               # Export feature
│   └── settings/             # Settings feature
└── ui/                       # Shared UI
    ├── theme/                # Material3 theme
    └── components/           # Reusable composables
```

---

## Technology Stack

### Core
- **Android Gradle Plugin:** 8.9.1
- **Kotlin:** 2.0.21
- **Java:** 11
- **Compose BOM:** 2024.09.00
- **Hilt:** 2.51.1
- **Navigation Compose:** 2.8.4
- **Lifecycle:** 2.10.0

### Networking
- **Retrofit:** 2.11.0
- **Gson:** 2.11.0
- **OkHttp Logging:** 4.12.0

### Async & State
- **Coroutines:** 1.9.0
- **Flow:** Reactive streams
- **StateFlow:** UI state management

### Storage
- **DataStore Preferences:** 1.1.1

### Image Loading
- **Coil Compose:** 2.7.0

### UI
- **Material3:** Latest from BOM
- **Material Icons Extended:** 1.7.5

---

## Key Conventions

### Architecture
- ✅ **MVVM + Clean Architecture** - Strict layer separation
- ✅ **Feature-first** - Features organized by domain
- ✅ **Pure domain layer** - No Android dependencies in `domain/`
- ✅ **Explicit mapping** - DTOs → Domain models via mappers

### ViewModels
- Use `@HiltViewModel` annotation
- Inject use cases via constructor
- Expose UI state as `StateFlow<UiState>`
- Keep state **immutable** (data classes with `copy()`)
- Use `viewModelScope` for coroutines

### Repositories
- Define interfaces in `domain/repository/`
- Implement in `data/repository/`
- Return `Result<T>` or `Flow<Result<T>>`
- Handle exceptions and convert to domain errors

### Use Cases
- One use case = one business operation
- Name pattern: `VerbNounUseCase` (e.g., `GetUserProfileUseCase`)
- Use `operator fun invoke()` for execution
- Keep in `domain/usecase/`

### Compose
- Use `collectAsStateWithLifecycle()` for Flow → State
- Extract reusables to `ui/components/`
- Follow Material3 design system
- Never pass ViewModels to composables (pass state + callbacks)

### Navigation Conventions
- Use a sealed class/object to declare routes with typed args.
- Centralize route definitions in `presentation/navigation/`.
- Pass data via nav arguments or `SavedStateHandle`; avoid globals/singletons.
- Prefer lowercase kebab-case for path segments; document deep links.

### Dependency Injection
- Hilt modules live in `core/di/` and use `@Module` + `@InstallIn(SingletonComponent::class)`.
- Use `@Provides` for third-party deps (Retrofit, OkHttp, Gson, DataStore) and scope as `@Singleton`.
- Use `@Binds` to wire domain repository interfaces to data implementations.
- Prefer constructor injection across layers.

### Networking Guidelines
- Configure OkHttp timeouts (connect/read/write) to reasonable defaults (e.g., ~15s).
- Add `HttpLoggingInterceptor` only for debug builds.
- Keep the base URL in a single source of truth (network module or BuildConfig).
- Map API/network errors to domain errors in repositories; never leak DTOs to UI.

### DataStore Usage
- Use type-safe `Preferences.Key<T>` keys for all entries.
- Expose preferences as `Flow<T>` and collect with `collectAsStateWithLifecycle()` in UI.
- Access DataStore from ViewModels/use cases; avoid I/O from Composables.

---

## Build & Run Commands

```powershell
# Clean and build
.\gradlew.bat clean build

# Install debug APK
.\gradlew.bat installDebug

# Run unit tests
.\gradlew.bat test

# Run instrumentation tests
.\gradlew.bat connectedAndroidTest

# Lint check
.\gradlew.bat lint
```

---

## Quality Gates (run locally before submitting changes)
- Build: `./gradlew.bat build` must succeed without new warnings.
- Lint: `./gradlew.bat lint` must report no new issues on changed files.
- Tests: `./gradlew.bat test` must be green; add/adjust tests if behavior changes.
- Compose previews should compile; fix missing previews only if relevant to the change.
- Hilt aggregation errors must be resolved (covered by build).

---

## Adding New Features

### Step-by-Step
1. Create feature package in `presentation/featurename/`
2. Define domain models in `domain/model/`
3. Create repository interface in `domain/repository/`
4. Create use cases in `domain/usecase/`
5. Create DTOs in `data/remote/`
6. Create mappers in `data/mapper/`
7. Implement repository in `data/repository/`
8. Create ViewModel with `@HiltViewModel`
9. Create UI state & events classes
10. Create Composable screens
11. Add navigation routes
12. Write unit tests

### File Naming
- **ViewModel:** `FeatureViewModel.kt`
- **Screen:** `FeatureScreen.kt`
- **UI State:** `FeatureUiState.kt`
- **Events:** `FeatureUiEvent.kt`
- **Use Case:** `VerbFeatureUseCase.kt`

---

## Testing Guidelines
- Write unit tests for ViewModels, use cases, and repositories.
- Use `kotlinx-coroutines-test` (`runTest`) for coroutine code; use Turbine for Flow.
- Avoid real network access in tests; mock via interfaces.
- Minimum policy: add at least 1 happy path + 1 error path test for new public behavior.

---

## Quick Reference

### ViewModel Pattern
```kotlin
@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val useCase: GetDataUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeatureUiState())
    val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()
    
    fun onEvent(event: FeatureUiEvent) { /* handle events */ }
}
```

### Use Case Pattern
```kotlin
class GetDataUseCase @Inject constructor(
    private val repository: DataRepository
) {
    suspend operator fun invoke(): Result<Data> {
        return repository.getData()
    }
}
```

### Repository Pattern
```kotlin
// Interface in domain/repository/
interface DataRepository {
    suspend fun getData(): Result<Data>
}

// Implementation in data/repository/
class DataRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val mapper: DataMapper
) : DataRepository {
    override suspend fun getData(): Result<Data> = runCatching {
        val dto = apiService.getData()
        mapper.toDomain(dto)
    }
}
```

---

## Error Handling Policy
- Map exceptions and HTTP errors to domain-friendly error types.
- Surface user-friendly messages in UI; log technical details internally.
- Never log or display sensitive information (tokens, PII).
- Prefer `Result<T>` or sealed error types for propagation.

---

## Security & Secrets
- Never hardcode secrets or tokens; prefer Keystore/Encrypted DataStore.
- Use HTTPS for all network calls; if HTTP is required, document a network security config.
- Keep environment-specific configuration and signing info out of VCS.

---

## Dependency Boundaries
- presentation → depends on domain (+ core utilities/DI)
- data → depends on domain (+ core)
- domain → depends on nothing app-specific (pure Kotlin; no Android APIs)
- core → cross-cutting only (no business logic)

---

## Change Protocol for Agents
- Keep edits minimal and localized to the feature or concern.
- If a change spans layers (DTO → mapper → domain → UI), complete the whole chain in one PR.
- Update `AGENT.md` and `AGENT_INSTRUCTIONS.md` when introducing new patterns or conventions.

---

## Important Notes

- 📖 **Full documentation:** See `AGENT_INSTRUCTIONS.md`
- 📝 **Development log:** See `develop.md`
- 🔧 **Version catalog:** Dependencies in `gradle/libs.versions.toml`
- 🎯 **Always follow** existing patterns and architecture
- ✅ **Test your code** - Unit tests for business logic
- 🚀 **Keep it clean** - Single responsibility principle

---

**Last Updated:** January 8, 2026

