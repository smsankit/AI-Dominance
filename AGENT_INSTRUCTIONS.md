# Agent Instructions for Logger Android Application

## Project Overview
**Project Name:** Logger  
**Type:** Android Mobile Application  
**Architecture:** MVVM with Clean Architecture  
**Primary Language:** Kotlin  
**UI Framework:** Jetpack Compose  
**Package:** com.example.logger  
**Last Updated:** January 8, 2026

## Project Description
Logger is a modern Android application built with cutting-edge technologies, following MVVM architecture with Clean Architecture principles. The application uses Jetpack Compose for UI, Hilt for dependency injection, and follows a feature-first approach with clear separation of concerns across data, domain, and presentation layers.

---

## Technology Stack

### Core Technologies
- **Language:** Kotlin 2.0.21
- **Build System:** Gradle 8.9.1 (Kotlin DSL)
- **Minimum SDK:** 24 (Android 7.0)
- **Target SDK:** 36
- **Compile SDK:** 36
- **Java Version:** 11

### Key Libraries & Frameworks

#### UI & Compose
- **Jetpack Compose BOM:** 2024.09.00
- **Compose UI:** Latest (from BOM)
- **Material3:** Latest (from BOM)
- **Material Icons Extended:** 1.7.5
- **Activity Compose:** 1.12.0
- **Compose Compiler:** Kotlin 2.0.21 (plugin)

#### Architecture Components
- **Lifecycle Runtime KTX:** 2.10.0
- **ViewModel Compose:** 2.10.0
- **Lifecycle Runtime Compose:** 2.10.0
- **Navigation Compose:** 2.8.4

#### Dependency Injection
- **Hilt:** 2.51.1
- **Hilt Navigation Compose:** 1.2.0
- **Javax Inject:** 1

#### Networking
- **Retrofit:** 2.11.0
- **Retrofit Gson Converter:** 2.11.0
- **OkHttp Logging Interceptor:** 4.12.0
- **Gson:** 2.11.0

#### Concurrency
- **Kotlin Coroutines Core:** 1.9.0
- **Kotlin Coroutines Android:** 1.9.0

#### Data Persistence
- **DataStore Preferences:** 1.1.1

#### Image Loading
- **Coil Compose:** 2.7.0

#### Testing
- **JUnit:** 4.13.2
- **AndroidX JUnit:** 1.3.0
- **Espresso Core:** 3.7.0
- **Coroutines Test:** 1.7.3
- **Turbine:** 1.0.0
- **Compose UI Test:** Latest (from BOM)

---

## Project Structure

### Package Organization
```
com.example.logger/
├── LoggerApp.kt                    # Application class with @HiltAndroidApp
├── MainActivity.kt                 # Single activity hosting Compose
│
├── core/                          # Cross-cutting concerns
│   ├── datastore/                 # DataStore implementation
│   ├── di/                        # Hilt modules
│   ├── exception/                 # Custom exceptions & error handling
│   ├── network/                   # Network configuration & interceptors
│   ├── util/                      # Utility classes & extension functions
│   └── validation/                # Input validation logic
│
├── domain/                        # Business logic layer
│   ├── model/                     # Domain models (business entities)
│   ├── repository/                # Repository interfaces
│   └── usecase/                   # Use cases (business logic)
│
├── data/                          # Data layer
│   ├── mapper/                    # DTO to domain model mappers
│   ├── remote/                    # API services & DTOs
│   └── repository/                # Repository implementations
│
├── presentation/                  # UI layer
│   ├── dashboard/                 # Dashboard feature
│   ├── export/                    # Export feature
│   ├── history/                   # History feature
│   ├── home/                      # Home feature
│   ├── missing/                   # Missing entries feature
│   ├── navigation/                # Navigation setup
│   ├── roster/                    # Roster feature
│   ├── settings/                  # Settings feature
│   ├── splash/                    # Splash screen
│   └── submitstandup/             # Submit standup feature
│
└── ui/                            # Shared UI components & theme
    ├── components/                # Reusable composables
    └── theme/                     # Material3 theme configuration
```

### Build Files Structure
```
Logger_workspace/
├── build.gradle.kts               # Root build configuration
├── settings.gradle.kts            # Project settings
├── gradle.properties              # Gradle properties
├── local.properties               # Local SDK paths (not in VCS)
├── gradle/
│   ├── libs.versions.toml         # Version catalog for dependencies
│   └── wrapper/
├── app/
│   ├── build.gradle.kts           # App module build configuration
│   ├── proguard-rules.pro         # ProGuard rules
│   └── src/
│       ├── main/
│       ├── test/                  # Unit tests
│       └── androidTest/           # Instrumentation tests
├── AGENT.md                       # Agent notes
├── AGENT_INSTRUCTIONS.md          # This file
└── develop.md                     # Development log
```

---

## Architecture Guidelines

### MVVM with Clean Architecture

#### Layers & Responsibilities

**1. Presentation Layer (`presentation/`)**
- **ViewModels:** Manage UI state, handle user events, orchestrate use cases
- **Composables:** UI components (screens & reusable components)
- **UI State:** Immutable data classes representing screen state
- **UI Events:** User actions/interactions
- **Navigation:** Navigation graph and routes

**2. Domain Layer (`domain/`)**
- **Models:** Business entities (pure Kotlin classes, no Android dependencies)
- **Use Cases:** Single-responsibility business logic operations
- **Repository Interfaces:** Contracts for data operations

**3. Data Layer (`data/`)**
- **DTOs:** Data Transfer Objects from API/local storage
- **Mappers:** Convert DTOs to domain models
- **Repository Implementations:** Implement domain repository interfaces
- **Data Sources:** Remote (API) and local (DataStore) data sources

**4. Core Layer (`core/`)**
- **DI Modules:** Hilt dependency injection configuration
- **Network:** Retrofit setup, interceptors
- **DataStore:** Preferences management
- **Exceptions:** Custom error handling
- **Validation:** Input validation logic
- **Utils:** Extension functions, helpers

---

## Code Conventions & Best Practices

### Kotlin Style
- Follow **Kotlin Official Coding Conventions**
- Use **camelCase** for variables and functions
- Use **PascalCase** for classes and objects
- Use **UPPER_SNAKE_CASE** for constants
- Prefer `val` over `var` for immutability
- Use meaningful names that convey intent
- Keep functions small and focused (single responsibility)

### Compose Guidelines
- Use `@Composable` functions for UI
- Extract reusable composables to `ui/components/`
- Use `remember` for state that survives recomposition
- Use `rememberSaveable` for state that survives configuration changes
- Prefer `LazyColumn`/`LazyRow` over `Column`/`Row` with scrolling
- Use `Modifier` for styling and layout
- Follow Material3 design guidelines
- Use `collectAsStateWithLifecycle()` for Flow collection in Composables

### State Management
- **ViewModels expose state as `StateFlow<UiState>`**
- UI state should be **immutable** (use data classes with `copy()`)
- Handle events through sealed classes or interfaces
- Use `viewModelScope` for coroutines in ViewModels
- Never pass ViewModels to Composables; pass state and callbacks

### Dependency Injection (Hilt)
- Annotate Application class with `@HiltAndroidApp`
- Annotate Activities with `@AndroidEntryPoint`
- Use `@HiltViewModel` for ViewModels
- Create modules in `core/di/` with appropriate annotations:
  - `@Module` + `@InstallIn(SingletonComponent::class)`
  - Use `@Provides` for third-party dependencies
  - Use `@Binds` for interface implementations
- Inject dependencies via constructor injection when possible

### Repository Pattern
- Define interfaces in `domain/repository/`
- Implement in `data/repository/`
- Return `Result<T>` or `Flow<Result<T>>` for operations
- Handle exceptions and map to domain errors
- Use mappers to convert DTOs to domain models

### Use Cases
- One use case = one business operation
- Name use cases with verb pattern: `GetUserProfileUseCase`
- Keep use cases in `domain/usecase/`
- Inject repositories through constructor
- Use operator `invoke()` for execution

### Error Handling
- Use `Result<T>` sealed class or similar for error propagation
- Define custom exceptions in `core/exception/`
- Map API errors to domain errors
- Show user-friendly error messages in UI
- Log errors appropriately (don't expose sensitive data)

### Navigation
- Use Navigation Compose with type-safe routes
- Define routes in `presentation/navigation/`
- Use sealed classes or objects for destinations
- Pass data through navigation arguments or SavedStateHandle
- Handle deep links when applicable

### Network & API
- Define API interfaces in `data/remote/`
- Use DTOs for network responses (separate from domain models)
- Use `suspend` functions for API calls
- Add logging interceptor for debugging (debug builds only)
- Handle network timeouts and retries
- Use appropriate HTTP methods and status codes

### DataStore
- Create generic `PreferencesManager` in `core/datastore/`
- Use methods like `store()`, `get()`, `delete()`, `clear()` with key parameters
- Expose data as `Flow<T>` for reactive updates
- Use type-safe keys (Preferences.Key<T>)

### Testing
- Write unit tests for ViewModels, use cases, and repositories
- Use `kotlinx-coroutines-test` for testing coroutines
- Use `Turbine` for testing Flows
- Mock dependencies using interfaces
- Test edge cases and error scenarios
- Use `@Before` and `@After` for test setup/teardown
- Keep tests in `src/test/` for unit tests
- Keep UI tests in `src/androidTest/`

---

## Common Commands

### Build & Run
```powershell
# Build the project
.\gradlew.bat build

# Clean build
.\gradlew.bat clean

# Assemble debug APK
.\gradlew.bat assembleDebug

# Install debug APK
.\gradlew.bat installDebug

# Clean and build
.\gradlew.bat clean build
```

### Testing
```powershell
# Run all unit tests
.\gradlew.bat test

# Run debug unit tests
.\gradlew.bat testDebugUnitTest

# Run instrumentation tests
.\gradlew.bat connectedAndroidTest

# Generate test coverage report
.\gradlew.bat jacocoTestReport
```

### Code Quality
```powershell
# Check for dependency updates
.\gradlew.bat dependencyUpdates

# Lint checks
.\gradlew.bat lint

# Format code (if ktlint is configured)
.\gradlew.bat ktlintFormat
```

### Gradle Tasks
```powershell
# List all tasks
.\gradlew.bat tasks

# View dependencies
.\gradlew.bat dependencies

# Check for outdated dependencies
.\gradlew.bat dependencyUpdates
```

---

## Development Workflow

### Adding a New Feature
1. **Create feature package** in `presentation/` (e.g., `presentation/profile/`)
2. **Define domain models** in `domain/model/`
3. **Create repository interface** in `domain/repository/`
4. **Create use cases** in `domain/usecase/`
5. **Create DTOs and mappers** in `data/`
6. **Implement repository** in `data/repository/`
7. **Create ViewModel** with `@HiltViewModel` in `presentation/feature/`
8. **Create UI state and events** classes
9. **Create Composable screens** in `presentation/feature/`
10. **Add navigation routes** and integrate in navigation graph
11. **Write unit tests** for ViewModels and use cases
12. **Test UI** with Compose UI testing

### Feature Structure Example
```
presentation/profile/
├── ProfileScreen.kt              # Main composable
├── ProfileViewModel.kt           # ViewModel
├── ProfileUiState.kt            # UI state data class
├── ProfileUiEvent.kt            # User events sealed class
└── components/                   # Feature-specific components
    ├── ProfileHeader.kt
    └── ProfileStats.kt
```

### ViewModel Template
```kotlin
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.Refresh -> loadProfile()
            // Handle other events
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getUserProfileUseCase()
                .onSuccess { profile ->
                    _uiState.update {
                        it.copy(
                            profile = profile,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
        }
    }
}
```

### Use Case Template
```kotlin
class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<UserProfile> {
        return try {
            userRepository.getUserProfile()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## Dependency Management

### Version Catalog (`gradle/libs.versions.toml`)
- All dependency versions are centralized in `libs.versions.toml`
- Use aliases for dependencies in build files
- Keep dependencies up to date
- Check for updates regularly

### Adding New Dependencies
1. Add version in `[versions]` section
2. Add library in `[libraries]` section
3. Reference in `app/build.gradle.kts` using `libs.dependency.name`
4. Sync Gradle

Example:
```toml
[versions]
room = "2.6.1"

[libraries]
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
```

Then in `app/build.gradle.kts`:
```kotlin
dependencies {
    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
}
```

---

## ProGuard & Optimization

### Release Build Configuration
- ProGuard rules in `app/proguard-rules.pro`
- Currently `isMinifyEnabled = false` (update for production)
- Add rules for Retrofit, Gson, and other reflection-based libraries
- Test release builds thoroughly

---

## Git & Version Control

### Files to Ignore
- `local.properties` (already in .gitignore)
- `build/` directories
- `.gradle/` directory
- IDE-specific files (`.idea/` for Android Studio)
- Generated files

### Commit Guidelines
- Write clear, descriptive commit messages
- Use conventional commits format (feat:, fix:, docs:, refactor:, test:)
- Keep commits atomic and focused
- Reference issue numbers when applicable

---

## Troubleshooting

### Common Issues

**Build Failures:**
- Ensure Gradle sync is successful
- Check for dependency conflicts
- Update AGP and Kotlin versions if needed
- Clean build: `.\gradlew.bat clean build`

**Hilt Errors:**
- Ensure `@HiltAndroidApp` on Application class
- Check module annotations are correct
- Verify kapt is enabled in build.gradle.kts
- Rebuild project after adding new modules

**Compose Issues:**
- Check Compose compiler version matches Kotlin version
- Ensure stable API versions for production
- Use preview annotations for debugging
- Check Material3 theme is applied

**Network Issues:**
- Verify internet permission in AndroidManifest.xml
- Check base URL configuration
- Add network security config for HTTP (if needed)
- Inspect OkHttp logs for debugging

---

## Performance Considerations

### Best Practices
- Use `remember` and `derivedStateOf` to avoid unnecessary recompositions
- Use `LazyColumn` for lists (don't nest scrollables)
- Avoid heavy operations in Composables (move to ViewModel)
- Use `Flow` for reactive data
- Implement pagination for large data sets
- Use Coil's caching for images
- Profile app with Android Profiler

---

## Security

### Guidelines
- Store sensitive data in encrypted DataStore or Keystore
- Use HTTPS for all network calls
- Validate all user inputs
- Don't log sensitive information
- Obfuscate code in release builds
- Implement certificate pinning for APIs (if required)
- Follow OWASP Mobile Security guidelines

---

## Documentation

### Code Documentation
- Add KDoc comments for public APIs
- Document complex business logic
- Explain non-obvious code decisions
- Keep comments up to date with code changes

### Project Documentation
- **AGENT.md:** Quick project notes for AI agents
- **develop.md:** Development log and Copilot interactions
- **AGENT_INSTRUCTIONS.md:** This comprehensive guide
- Update documentation when making significant changes

---

## Resources & References

### Official Documentation
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material3 Design](https://m3.material.io/)
- [Hilt Documentation](https://dagger.dev/hilt/)
- [Retrofit](https://square.github.io/retrofit/)
- [Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)

### Architecture
- [Guide to App Architecture](https://developer.android.com/topic/architecture)
- [Clean Architecture by Uncle Bob](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

---

## Contact & Support

### Project Maintainers
- Ankit Kumar - Frontend Developer
- Paras Kochar - Frontend Developer

### Getting Help
- Check `develop.md` for development history
- Review existing code patterns in the project
- Consult Android documentation
- Search Stack Overflow for common issues

---

## Changelog

### Version 1.0 - Initial Setup (January 2026)
- Project scaffolded with MVVM + Clean Architecture
- Jetpack Compose with Material3
- Hilt for dependency injection
- Retrofit + Gson for networking
- Coil for image loading
- DataStore for preferences
- Multiple features implemented (dashboard, roster, history, etc.)

---

## Notes for AI Agents

### When Assisting with This Project:
1. **Always follow the established architecture** (MVVM + Clean Architecture)
2. **Respect the package structure** (core, domain, data, presentation)
3. **Use the existing patterns** for ViewModels, use cases, and repositories
4. **Write Kotlin idiomatic code** with coroutines and Flow
5. **Use Jetpack Compose** for all UI (no XML layouts)
6. **Inject dependencies** via Hilt constructor injection
7. **Keep domain layer pure** (no Android dependencies)
8. **Map DTOs to domain models** explicitly
9. **Expose UI state as StateFlow** from ViewModels
10. **Write unit tests** for business logic
11. **Use the version catalog** for all dependencies
12. **Follow Material3 design guidelines**
13. **Handle errors gracefully** with user-friendly messages
14. **Keep the codebase consistent** with existing patterns

### Quick Reference for Common Tasks:
- **New screen:** Create ViewModel + UI State + Screen Composable + Navigation route
- **New API call:** Create DTO + Mapper + Repository method + Use case
- **New DI module:** Create in `core/di/` with `@Module` + `@InstallIn`
- **New validation:** Add to `core/validation/`
- **New utility:** Add to `core/util/`
- **Store data:** Use PreferencesManager in `core/datastore/`

---

**Last Updated:** January 8, 2026  
**Version:** 1.0.0  
**Maintained By:** Development Team

